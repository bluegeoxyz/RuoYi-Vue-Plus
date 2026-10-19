package org.dromara.property.charge.payment.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** TASK-020A 收款与核销模型、约束和迁移的真实 MySQL 验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentAllocationSchemaMySqlTest {
    private static Connection connection;
    private static long projectId;
    private static String prefix;

    @BeforeAll
    static void setUpSchema() throws Exception {
        connection = DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),
            System.getenv("ZBM_TEST_DB_USER"), System.getenv("ZBM_TEST_DB_PASSWORD"));
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        boolean paymentExists = tableExists("pm_payment");
        boolean allocationExists = tableExists("pm_payment_allocation");
        assertEquals(paymentExists, allocationExists, "收款主表与核销明细表必须成对存在");
        if (!paymentExists) {
            executeMigration(repositoryRoot().resolve(
                "script/sql/zbm/archive/legacy-development-migrations/V2.0.21__payment_allocation_schema_dict_menu.sql"));
        }
        projectId = id();
        prefix = "T20A" + Long.toUnsignedString(projectId);
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (connection == null) return;
        deleteByProject("pm_payment_allocation");
        deleteByProject("pm_payment");
        connection.close();
    }

    @Test @Order(1)
    void migrationAndBaselineExposeEquivalentFinalStructure() throws Exception {
        assertEquals(3, countChecks("pm_payment"));
        assertEquals(1, countChecks("pm_payment_allocation"));
        String baseline = Files.readString(repositoryRoot().resolve("script/sql/zbm/baseline/01_schema.sql"));
        assertBaselineTableEquivalent(baseline, "pm_payment");
        assertBaselineTableEquivalent(baseline, "pm_payment_allocation");
        assertEquals(2, scalar("SELECT COUNT(*) FROM sys_dict_type WHERE dict_type IN "
            + "('pm_payment_method','pm_payment_record_status')"));
        assertEquals(8, scalar("SELECT COUNT(*) FROM sys_dict_data WHERE dict_type IN "
            + "('pm_payment_method','pm_payment_record_status')"));
        assertEquals(5, scalar("SELECT COUNT(*) FROM sys_menu WHERE perms IN "
            + "('property:payment:list','property:payment:query','property:payment:add',"
            + "'property:payment:allocate','property:payment:export')"));
        assertEquals(1, scalar("SELECT COUNT(*) FROM sys_menu WHERE perms='property:payment:reverse'"));
        assertEquals(0, scalar("SELECT COUNT(*) FROM sys_menu WHERE perms IN "
            + "('property:payment:edit','property:payment:remove')"));
    }

    @Test @Order(2)
    void paymentNumberIsGloballyUnique() throws SQLException {
        String no = prefix + "P1";
        insertPayment(id(), projectId, no, "100.00", "0.00", "CASH", "VALID");
        assertThrows(SQLException.class,
            () -> insertPayment(id(), id(), no, "100.00", "0.00", "CASH", "VALID"));
    }

    @Test @Order(3)
    void paymentAmountsMethodsAndStatusesAreConstrained() {
        assertRejected("P2", "0.00", "0.00", "CASH", "VALID");
        assertRejected("P3", "100.00", "-0.01", "CASH", "VALID");
        assertRejected("P4", "100.00", "100.01", "CASH", "VALID");
        assertRejected("P5", "100.00", "0.00", "CARD", "VALID");
        assertRejected("P6", "100.00", "0.00", "CASH", "CANCELLED");
        assertDoesNotThrow(() -> insertPayment(id(), projectId, prefix + "P7",
            "100.00", "0.00", "WECHAT", "VALID"));
        assertDoesNotThrow(() -> insertPayment(id(), projectId, prefix + "P8",
            "100.00", "100.00", "BANK_TRANSFER", "REVERSED"));
    }

    @Test @Order(4)
    void allocationAmountMustBePositive() throws SQLException {
        long paymentId = id();
        insertPayment(paymentId, projectId, prefix + "P9", "100.00", "0.00", "ALIPAY", "VALID");
        assertThrows(SQLException.class, () -> insertAllocation(id(), paymentId, id(), "0.00"));
        assertThrows(SQLException.class, () -> insertAllocation(id(), paymentId, id(), "-0.01"));
        assertDoesNotThrow(() -> insertAllocation(id(), paymentId, id(), "0.01"));
    }

    @Test @Order(5)
    void sameReceivableCanOnlyBeAllocatedOncePerPayment() throws SQLException {
        long paymentId = id(), receivableId = id();
        insertPayment(paymentId, projectId, prefix + "P10", "200.00", "0.00", "POS", "VALID");
        insertAllocation(id(), paymentId, receivableId, "100.00");
        assertThrows(SQLException.class, () -> insertAllocation(id(), paymentId, receivableId, "50.00"));
        long secondPaymentId = id();
        insertPayment(secondPaymentId, projectId, prefix + "P11", "50.00", "0.00", "OTHER", "VALID");
        assertDoesNotThrow(() -> insertAllocation(id(), secondPaymentId, receivableId, "50.00"));
    }

    @Test @Order(6)
    void onePaymentCanAllocateAcrossHistoricalReceivables() throws SQLException {
        long paymentId = id();
        insertPayment(paymentId, projectId, prefix + "P12", "3000.00", "3000.00", "BANK_TRANSFER", "VALID");
        insertAllocation(id(), paymentId, id(), "2400.00");
        insertAllocation(id(), paymentId, id(), "600.00");
        try (PreparedStatement s = connection.prepareStatement(
            "SELECT p.payment_amount,p.allocated_amount,SUM(a.allocation_amount),COUNT(*) "
                + "FROM pm_payment p JOIN pm_payment_allocation a ON a.payment_id=p.id "
                + "WHERE p.id=? GROUP BY p.id,p.payment_amount,p.allocated_amount")) {
            s.setLong(1, paymentId);
            try (ResultSet r = s.executeQuery()) {
                assertTrue(r.next());
                assertEquals("3000.00", r.getBigDecimal(1).toPlainString());
                assertEquals("3000.00", r.getBigDecimal(2).toPlainString());
                assertEquals("3000.00", r.getBigDecimal(3).toPlainString());
                assertEquals(2, r.getInt(4));
            }
        }
    }

    private static void assertRejected(String suffix, String amount, String allocated,
                                       String method, String status) {
        assertThrows(SQLException.class, () -> insertPayment(id(), projectId, prefix + suffix,
            amount, allocated, method, status));
    }

    private static void insertPayment(long id, long paymentProjectId, String no, String amount,
                                      String allocated, String method, String status) throws SQLException {
        String sql = "INSERT INTO pm_payment(id,project_id,payment_no,room_id,room_full_name_snapshot,"
            + "payer_name_snapshot,payer_type_snapshot,payment_amount,allocated_amount,payment_method,"
            + "payment_time,status) VALUES(?,?,?,101,'1号楼/1单元/101','测试付款人','OWNER',?,?,?,?,?)";
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setLong(1, id); s.setLong(2, paymentProjectId); s.setString(3, no);
            s.setBigDecimal(4, new BigDecimal(amount)); s.setBigDecimal(5, new BigDecimal(allocated));
            s.setString(6, method); s.setTimestamp(7, Timestamp.valueOf("2026-10-07 10:00:00"));
            s.setString(8, status); s.executeUpdate();
        }
    }

    private static void insertAllocation(long id, long paymentId, long receivableId, String amount)
        throws SQLException {
        try (PreparedStatement s = connection.prepareStatement(
            "INSERT INTO pm_payment_allocation(id,project_id,payment_id,receivable_id,"
                + "allocation_amount,allocation_time) VALUES(?,?,?,?,?,?)")) {
            s.setLong(1, id); s.setLong(2, projectId); s.setLong(3, paymentId);
            s.setLong(4, receivableId); s.setBigDecimal(5, new BigDecimal(amount));
            s.setTimestamp(6, Timestamp.valueOf("2026-10-07 10:01:00")); s.executeUpdate();
        }
    }

    private static void deleteByProject(String table) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement("DELETE FROM " + table + " WHERE project_id=?")) {
            s.setLong(1, projectId); s.executeUpdate();
        }
    }

    private static boolean tableExists(String table) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")) {
            s.setString(1, table); try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1) == 1; }
        }
    }

    private static int countChecks(String table) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement(
            "SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() "
                + "AND table_name=? AND constraint_type='CHECK'")) {
            s.setString(1, table); try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    private static int scalar(String sql) throws SQLException {
        try (Statement s = connection.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next(); return r.getInt(1);
        }
    }

    private static void executeMigration(Path path) throws Exception {
        String sql = Files.readString(path).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)--.*$", "");
        for (String part : sql.split(";")) if (!part.isBlank()) {
            try (Statement s = connection.createStatement()) { s.execute(part); }
        }
    }

    private static void assertBaselineTableEquivalent(String baseline, String table) throws SQLException {
        String marker = "CREATE TABLE `" + table + "`";
        int start = baseline.indexOf(marker), end = baseline.indexOf(";", start);
        assertTrue(start >= 0 && end > start);
        String probe = table + "_baseline_probe";
        String ddl = baseline.substring(start, end).replaceFirst(marker, "CREATE TABLE `" + probe + "`")
            .replaceAll("CONSTRAINT `([^`]+)`", "CONSTRAINT `$1_probe`");
        try (Statement s = connection.createStatement()) {
            s.execute("DROP TABLE IF EXISTS `" + probe + "`"); s.execute(ddl);
            assertEquals(metadata("columns", table), metadata("columns", probe));
            assertEquals(metadata("indexes", table), metadata("indexes", probe));
            assertEquals(metadata("checks", table), metadata("checks", probe));
        } finally {
            try (Statement s = connection.createStatement()) { s.execute("DROP TABLE IF EXISTS `" + probe + "`"); }
        }
    }

    private static List<String> metadata(String kind, String table) throws SQLException {
        String sql = switch (kind) {
            case "columns" -> "SELECT CONCAT_WS('|',ordinal_position,column_name,column_type,is_nullable,"
                + "COALESCE(column_default,'<NULL>'),extra,COALESCE(generation_expression,'')) FROM "
                + "information_schema.columns WHERE table_schema=DATABASE() AND table_name=? ORDER BY ordinal_position";
            case "indexes" -> "SELECT CONCAT_WS('|',index_name,non_unique,seq_in_index,column_name,"
                + "COALESCE(sub_part,'')) FROM information_schema.statistics WHERE table_schema=DATABASE() "
                + "AND table_name=? ORDER BY index_name,seq_in_index";
            default -> "SELECT REPLACE(cc.check_clause,'`','') FROM information_schema.table_constraints tc "
                + "JOIN information_schema.check_constraints cc ON cc.constraint_schema=tc.constraint_schema "
                + "AND cc.constraint_name=tc.constraint_name WHERE tc.table_schema=DATABASE() AND tc.table_name=? "
                + "AND tc.constraint_type='CHECK' ORDER BY cc.check_clause";
        };
        List<String> rows = new ArrayList<>();
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, table); try (ResultSet r = s.executeQuery()) {
                while (r.next()) rows.add(r.getString(1).replace(table + "_baseline_probe", table));
            }
        }
        return rows;
    }

    private static Path repositoryRoot() {
        Path path = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (path != null && !Files.exists(path.resolve("script/sql/zbm"))) path = path.getParent();
        return Objects.requireNonNull(path, "repository root");
    }

    private static long id() { return Math.abs(UUID.randomUUID().getMostSignificantBits() >>> 1); }
}
