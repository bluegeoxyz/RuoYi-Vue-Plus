package org.dromara.property.customer.access.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Customer Grant、撤销即时生效和审计表的真实 MySQL 8 验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class CustomerAccessGrantMySqlTest {
    private Connection connection;
    private long userId;
    private long roleId;

    @BeforeEach void setUp() throws SQLException {
        connection = openConnection();
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        userId = positiveTestId(); roleId = positiveTestId();
    }

    @AfterEach void cleanUp() throws SQLException {
        if (connection == null) return;
        try (PreparedStatement deleteAudit = connection.prepareStatement("DELETE FROM pm_customer_management_grant_audit WHERE principal_id IN (?, ?)");
             PreparedStatement deleteGrant = connection.prepareStatement("DELETE FROM pm_customer_management_grant WHERE principal_id IN (?, ?)");
             PreparedStatement deleteSensitive = connection.prepareStatement("DELETE FROM pm_customer_sensitive_access_audit WHERE operator_id IN (?, ?)")) {
            for (PreparedStatement statement : List.of(deleteAudit, deleteGrant, deleteSensitive)) {
                statement.setLong(1, userId); statement.setLong(2, roleId); statement.executeUpdate();
            }
        } finally { connection.close(); }
    }

    @Test void migrationMustExposeGeneratedKeyChecksAndPrivacySafeAuditColumns() throws SQLException {
        assertTrue(columnExtra("pm_customer_management_grant", "active_scope_key").contains("STORED GENERATED"));
        assertEquals("principal_type,principal_id,active_scope_key", indexColumns("uk_pm_customer_grant_active"));
        Set<String> columns = tableColumns("pm_customer_sensitive_access_audit");
        assertTrue(columns.containsAll(Set.of("operator_id", "customer_id", "access_action", "access_purpose", "field_level", "decision", "trace_id", "project_id")));
        assertFalse(columns.stream().anyMatch(name -> name.contains("id_no") || name.contains("mobile") || name.contains("fingerprint") || name.contains("key")));
    }

    @Test void duplicateActiveGrantMustFailButRevokedGrantMayBeRecreated() throws SQLException {
        long first = insertGrant("USER", userId, "0", "0");
        assertThrows(SQLException.class, () -> insertGrant("USER", userId, "0", "0"));
        try (PreparedStatement statement = connection.prepareStatement("UPDATE pm_customer_management_grant SET del_flag='1' WHERE id=?")) {
            statement.setLong(1, first); assertEquals(1, statement.executeUpdate());
        }
        assertDoesNotThrow(() -> insertGrant("USER", userId, "0", "0"));
    }

    @Test void userAndRoleGrantMustMergeAndDisabledGrantMustDisappearImmediately() throws SQLException {
        long direct = insertGrant("USER", userId, "0", "0");
        long role = insertGrant("ROLE", roleId, "0", "0");
        assertEquals(2, countEffective(userId, roleId));
        try (PreparedStatement statement = connection.prepareStatement("UPDATE pm_customer_management_grant SET status='1' WHERE id=?")) {
            statement.setLong(1, direct); statement.executeUpdate();
        }
        assertEquals(1, countEffective(userId, roleId));
        try (PreparedStatement statement = connection.prepareStatement("UPDATE pm_customer_management_grant SET del_flag='1' WHERE id=?")) {
            statement.setLong(1, role); statement.executeUpdate();
        }
        try (Connection freshRequest = openConnection()) {
            assertEquals(0, countEffective(freshRequest, userId, roleId));
        }
    }

    @Test void rollbackMustNotLeaveAnEffectiveGrant() throws SQLException {
        connection.setAutoCommit(false);
        insertGrant("USER", userId, "0", "0");
        connection.rollback();
        connection.setAutoCommit(true);
        try (Connection freshRequest = openConnection()) {
            assertEquals(0, countEffective(freshRequest, userId, roleId));
        }
    }

    @Test void auditRowsMustStoreMetadataWithoutSensitiveValues() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO pm_customer_sensitive_access_audit (id,operator_id,customer_id,access_action,access_purpose,field_level,decision,trace_id,project_id,remark) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
            statement.setLong(1, positiveTestId()); statement.setLong(2, userId); statement.setLong(3, positiveTestId());
            statement.setString(4, "PII_VIEW"); statement.setString(5, "CUSTOMER_DETAIL"); statement.setString(6, "IDENTITY");
            statement.setString(7, "ALLOW"); statement.setString(8, "trace-test"); statement.setNull(9, Types.BIGINT); statement.setString(10, "AUTHORIZED");
            assertEquals(1, statement.executeUpdate());
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT access_purpose,field_level,decision,remark FROM pm_customer_sensitive_access_audit WHERE operator_id=?")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) { assertTrue(result.next()); assertEquals("CUSTOMER_DETAIL", result.getString(1)); assertEquals("IDENTITY", result.getString(2)); assertEquals("ALLOW", result.getString(3)); assertEquals("AUTHORIZED", result.getString(4)); }
        }
    }

    private long insertGrant(String type, long principalId, String status, String deleted) throws SQLException {
        long id = positiveTestId();
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO pm_customer_management_grant (id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES (?, ?, ?, 'ENTERPRISE', ?, 0, ?)")) {
            statement.setLong(1, id); statement.setString(2, type); statement.setLong(3, principalId); statement.setString(4, status); statement.setString(5, deleted); statement.executeUpdate();
        }
        return id;
    }
    private int countEffective(long user, long role) throws SQLException { return countEffective(connection, user, role); }
    private int countEffective(Connection target, long user, long role) throws SQLException {
        try (PreparedStatement statement = target.prepareStatement("SELECT COUNT(*) FROM pm_customer_management_grant WHERE status='0' AND del_flag='0' AND scope_type='ENTERPRISE' AND ((principal_type='USER' AND principal_id=?) OR (principal_type='ROLE' AND principal_id=?))")) {
            statement.setLong(1, user); statement.setLong(2, role); try (ResultSet result = statement.executeQuery()) { result.next(); return result.getInt(1); }
        }
    }
    private String columnExtra(String table, String column) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("SELECT EXTRA FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) { statement.setString(1, table); statement.setString(2, column); try (ResultSet result = statement.executeQuery()) { assertTrue(result.next()); return result.getString(1).toUpperCase(Locale.ROOT); } } }
    private String indexColumns(String index) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("SELECT GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='pm_customer_management_grant' AND INDEX_NAME=?")) { statement.setString(1, index); try (ResultSet result = statement.executeQuery()) { result.next(); return result.getString(1); } } }
    private Set<String> tableColumns(String table) throws SQLException { Set<String> columns = new HashSet<>(); try (PreparedStatement statement = connection.prepareStatement("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) { statement.setString(1, table); try (ResultSet result = statement.executeQuery()) { while (result.next()) columns.add(result.getString(1)); } } return columns; }
    private Connection openConnection() throws SQLException { return DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"), System.getenv("ZBM_TEST_DB_USER"), System.getenv("ZBM_TEST_DB_PASSWORD")); }
    private static long positiveTestId() { return Math.abs(UUID.randomUUID().getMostSignificantBits() >>> 1); }
}
