package org.dromara.property.customer.security.mysql;

import org.dromara.common.core.constant.Constants;
import org.dromara.common.encrypt.utils.EncryptUtils;
import org.dromara.property.customer.security.CustomerIdentityFingerprintService;
import org.dromara.property.customer.security.HmacSha256CustomerIdentityFingerprintService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Customer 加密字段和有效身份唯一性的真实 MySQL 8 验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class CustomerSecurityMySqlTest {
    private Connection connection;
    private CustomerIdentityFingerprintService fingerprints;
    private String aesKey;
    private String prefix;

    @BeforeEach void setUp() throws SQLException {
        connection = openConnection();
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        aesKey = requiredEnvironment("ZBM_TEST_AES_KEY");
        fingerprints = new HmacSha256CustomerIdentityFingerprintService(requiredEnvironment("ZBM_TEST_HMAC_KEY_BASE64"));
        prefix = "C12_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @AfterEach void cleanUp() throws SQLException {
        if (connection == null) return;
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM pm_customer WHERE customer_no LIKE ?")) {
            statement.setString(1, prefix + "%"); statement.executeUpdate();
        } finally { connection.close(); }
    }

    @Test void migrationMustExposeStoredGeneratedColumnAndUniqueIndex() throws SQLException {
        try (PreparedStatement column = connection.prepareStatement("SELECT DATA_TYPE, EXTRA, GENERATION_EXPRESSION FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pm_customer' AND COLUMN_NAME = 'active_identity_key'");
             PreparedStatement index = connection.prepareStatement("SELECT GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pm_customer' AND INDEX_NAME = 'uk_pm_customer_active_identity'")) {
            try (ResultSet result = column.executeQuery()) { assertTrue(result.next()); assertEquals("binary", result.getString("DATA_TYPE")); assertTrue(result.getString("EXTRA").toUpperCase(Locale.ROOT).contains("STORED GENERATED")); assertTrue(result.getString("GENERATION_EXPRESSION").contains("del_flag")); }
            try (ResultSet result = index.executeQuery()) { assertTrue(result.next()); assertEquals("id_type,active_identity_key", result.getString(1)); }
        }
    }

    @Test void aesCiphertextMustRoundTripWithoutTruncation() throws SQLException {
        String maximumLegalIdNo = "证".repeat(64); long id = positiveTestId();
        insertCustomer(id, "ID_CARD", maximumLegalIdNo, "ACTIVE", "0");
        try (PreparedStatement statement = connection.prepareStatement("SELECT id_no, OCTET_LENGTH(id_no) FROM pm_customer WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next()); String ciphertext = result.getString(1);
                assertTrue(ciphertext.startsWith(Constants.ENCRYPT_HEADER)); assertNotEquals(maximumLegalIdNo, ciphertext); assertTrue(result.getInt(2) <= 512);
                assertEquals(maximumLegalIdNo, EncryptUtils.decryptByAes(ciphertext.substring(Constants.ENCRYPT_HEADER.length()), aesKey));
            }
        }
    }

    @Test void encryptionFailureMustNotInsertPlaintext() throws SQLException {
        long id = positiveTestId();
        assertThrows(RuntimeException.class, () -> EncryptUtils.encryptByAes("sensitive-value", "bad-key"));
        assertEquals(0, countCustomer(id));
    }

    @Test void fingerprintDomainsAndActiveUniquenessMustBeEnforced() throws SQLException {
        assertArrayEquals(fingerprints.fingerprint(" id_card ", "１２３ ４x"), fingerprints.fingerprint("ID_CARD", "1234X"));
        assertFalse(java.security.MessageDigest.isEqual(fingerprints.fingerprint("ID_CARD", "1234X"), fingerprints.fingerprint("PASSPORT", "1234X")));
        insertCustomer(positiveTestId(), "ID_CARD", "1234X", "ACTIVE", "0");
        assertThrows(SQLException.class, () -> insertCustomer(positiveTestId(), "ID_CARD", "１２３ ４x", "ACTIVE", "0"));
        assertDoesNotThrow(() -> insertCustomer(positiveTestId(), "PASSPORT", "1234X", "ACTIVE", "0"));
    }

    @Test void onlyLogicalDeletionMayReleaseIdentity() throws SQLException {
        long inactive = positiveTestId(); insertCustomer(inactive, "ID_CARD", "INACTIVE-1", "INACTIVE", "0");
        assertThrows(SQLException.class, () -> insertCustomer(positiveTestId(), "ID_CARD", "INACTIVE-1", "ACTIVE", "0"));
        long cancelled = positiveTestId(); insertCustomer(cancelled, "ID_CARD", "CANCELLED-1", "CANCELLED", "0");
        assertThrows(SQLException.class, () -> insertCustomer(positiveTestId(), "ID_CARD", "CANCELLED-1", "ACTIVE", "0"));
        long deleted = positiveTestId(); insertCustomer(deleted, "ID_CARD", "REUSABLE-1", "ACTIVE", "0"); logicallyDelete(deleted);
        assertDoesNotThrow(() -> insertCustomer(positiveTestId(), "ID_CARD", "REUSABLE-1", "ACTIVE", "0"));
    }

    @Test void concurrentInsertMustAllowAtMostOneActiveIdentity() throws Exception {
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1); AtomicInteger success = new AtomicInteger();
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        Runnable insertion = () -> { try (Connection concurrent = openConnection()) { ready.countDown(); start.await(); insertCustomer(concurrent, positiveTestId(), "ID_CARD", "RACE-IDENTITY", "ACTIVE", "0"); success.incrementAndGet(); } catch (Throwable error) { failures.add(error); } };
        Thread first = Thread.ofPlatform().start(insertion), second = Thread.ofPlatform().start(insertion); ready.await(); start.countDown(); first.join(); second.join();
        assertEquals(1, success.get()); assertEquals(1, failures.size()); assertInstanceOf(SQLException.class, failures.getFirst());
    }

    @Test void customersWithoutIdentityMayCoexist() throws SQLException {
        long first = positiveTestId(), second = positiveTestId(); insertCustomer(first, null, null, "ACTIVE", "0"); insertCustomer(second, null, null, "ACTIVE", "0");
        try (PreparedStatement statement = connection.prepareStatement("UPDATE pm_customer SET mobile='13900001111' WHERE id IN (?,?)")) {
            statement.setLong(1, first); statement.setLong(2, second); assertEquals(2, statement.executeUpdate());
        }
        assertEquals(1, countCustomer(first)); assertEquals(1, countCustomer(second));
    }

    private void insertCustomer(long id, String type, String number, String lifecycle, String deleted) throws SQLException { insertCustomer(connection, id, type, number, lifecycle, deleted); }
    private void insertCustomer(Connection target, long id, String type, String number, String lifecycle, String deleted) throws SQLException {
        String encrypted = number == null ? null : Constants.ENCRYPT_HEADER + EncryptUtils.encryptByAes(number, aesKey);
        byte[] fingerprint = number == null ? null : fingerprints.fingerprint(type, number);
        try (PreparedStatement statement = target.prepareStatement("INSERT INTO pm_customer (id, customer_no, customer_type, customer_name, id_type, id_no, identity_fingerprint, pii_key_version, lifecycle_status, status, del_flag) VALUES (?, ?, 'PERSON', 'Customer Security Test', ?, ?, ?, 'test-v1', ?, '0', ?)")) {
            statement.setLong(1, id); statement.setString(2, prefix + "_" + id); statement.setString(3, type == null ? null : fingerprints.normalizeIdType(type)); statement.setString(4, encrypted); statement.setBytes(5, fingerprint); statement.setString(6, lifecycle); statement.setString(7, deleted); statement.executeUpdate();
        }
    }
    private void logicallyDelete(long id) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("UPDATE pm_customer SET del_flag = '1' WHERE id = ?")) { statement.setLong(1, id); assertEquals(1, statement.executeUpdate()); } }
    private int countCustomer(long id) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM pm_customer WHERE id = ?")) { statement.setLong(1, id); try (ResultSet result = statement.executeQuery()) { assertTrue(result.next()); return result.getInt(1); } } }
    private Connection openConnection() throws SQLException { return DriverManager.getConnection(requiredEnvironment("ZBM_TEST_DB_URL"), requiredEnvironment("ZBM_TEST_DB_USER"), requiredEnvironment("ZBM_TEST_DB_PASSWORD")); }
    private static String requiredEnvironment(String name) { String value = System.getenv(name); if (value == null || value.isBlank()) throw new IllegalStateException(name + " must be configured for the isolated MySQL test"); return value; }
    private static long positiveTestId() { return Math.abs(UUID.randomUUID().getMostSignificantBits() >>> 1); }
}
