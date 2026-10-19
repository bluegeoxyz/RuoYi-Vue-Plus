package org.dromara.property.charge.payment.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** TASK-022A 收款冲销审计模型、约束及 baseline 一致性的真实 MySQL 验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentReversalSchemaMySqlTest {
    private static Connection connection;
    private static long projectId;
    private static String prefix;

    @BeforeAll
    static void setUpSchema() throws Exception {
        connection = DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),
            System.getenv("ZBM_TEST_DB_USER"), System.getenv("ZBM_TEST_DB_PASSWORD"));
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        assertTrue(tableExists("pm_payment"), "请先执行 V2.0.21 收款模型 migration");
        assertTrue(tableExists("pm_payment_allocation"), "请先执行 V2.0.21 核销模型 migration");
        if (!tableExists("pm_payment_reversal")) {
            executeMigration(repositoryRoot().resolve(
                "script/sql/zbm/archive/legacy-development-migrations/V2.0.22__payment_reversal_schema_permission.sql"));
        }
        projectId = id();
        prefix = "T22A" + Long.toUnsignedString(projectId);
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (connection == null) return;
        deleteByProject("pm_payment_reversal");
        deleteByProject("pm_payment_allocation");
        deleteByProject("pm_payment");
        connection.close();
    }

    @Test @Order(1)
    void migrationBaselineIndexAndPermissionExposeEquivalentFinalState() throws Exception {
        assertEquals(1, countChecks("pm_payment_reversal"));
        assertTrue(indexExists("pm_payment_reversal", "uk_pm_payment_reversal_no", true));
        assertTrue(indexExists("pm_payment_reversal", "uk_pm_payment_reversal_payment", true));
        assertTrue(indexExists("pm_payment_reversal", "idx_pm_payment_reversal_project_time", false));
        assertEquals(1, scalar("SELECT COUNT(*) FROM sys_menu WHERE parent_id=2609252000000000504 "
            + "AND menu_type='F' AND perms='property:payment:reverse'"));
        String baseline = Files.readString(repositoryRoot().resolve("script/sql/zbm/baseline/01_schema.sql"));
        assertBaselineTableEquivalent(baseline, "pm_payment_reversal");
        String menuBaseline = Files.readString(repositoryRoot().resolve(
            "script/sql/zbm/baseline/03_menu_permission.sql"));
        assertTrue(menuBaseline.contains("'property:payment:reverse'"));
    }

    @Test @Order(2)
    void reversalNumberAndPaymentAreBothUnique() throws SQLException {
        long payment1 = insertPayment("P1", "3000.00");
        long payment2 = insertPayment("P2", "3000.00");
        String reversalNo = prefix + "R1";
        insertReversal(id(), reversalNo, payment1, "3000.00");
        assertThrows(SQLException.class,
            () -> insertReversal(id(), reversalNo, payment2, "3000.00"));
        assertThrows(SQLException.class,
            () -> insertReversal(id(), prefix + "R2", payment1, "3000.00"));
    }

    @Test @Order(3)
    void reversalAmountMustBePositiveAndReasonMustBePresent() throws SQLException {
        long payment1 = insertPayment("P3", "100.00");
        long payment2 = insertPayment("P4", "100.00");
        assertThrows(SQLException.class,
            () -> insertReversal(id(), prefix + "R3", payment1, "0.00"));
        assertThrows(SQLException.class,
            () -> insertReversal(id(), prefix + "R4", payment1, "-0.01"));
        assertThrows(SQLException.class, () -> {
            try (PreparedStatement s = connection.prepareStatement(
                "INSERT INTO pm_payment_reversal(id,project_id,reversal_no,payment_id,reversal_amount,"
                    + "reversal_time,reason) VALUES(?,?,?,?,?,NOW(),NULL)")) {
                s.setLong(1, id()); s.setLong(2, projectId); s.setString(3, prefix + "R5");
                s.setLong(4, payment2); s.setBigDecimal(5, new BigDecimal("100.00")); s.executeUpdate();
            }
        });
    }

    @Test @Order(4)
    void reversalPreservesHistoricalPaymentAndAllocationsAndAllowsNewPayment() throws SQLException {
        long payment1 = insertPayment("P5", "3000.00");
        long receivable1 = id(), receivable2 = id();
        insertAllocation(id(), payment1, receivable1, "2400.00");
        insertAllocation(id(), payment1, receivable2, "600.00");
        insertReversal(id(), prefix + "R6", payment1, "3000.00");
        try (PreparedStatement s = connection.prepareStatement(
            "UPDATE pm_payment SET status='REVERSED' WHERE id=?")) {
            s.setLong(1, payment1); assertEquals(1, s.executeUpdate());
        }
        assertEquals(1, scalar("SELECT COUNT(*) FROM pm_payment WHERE id=" + payment1
            + " AND status='REVERSED'"));
        assertEquals(2, scalar("SELECT COUNT(*) FROM pm_payment_allocation WHERE payment_id=" + payment1));

        long payment2 = insertPayment("P6", "3000.00");
        assertDoesNotThrow(() -> insertAllocation(id(), payment2, receivable1, "2400.00"));
    }

    private static long insertPayment(String suffix, String amount) throws SQLException {
        long paymentId = id();
        try (PreparedStatement s = connection.prepareStatement(
            "INSERT INTO pm_payment(id,project_id,payment_no,room_id,room_full_name_snapshot,"
                + "payer_name_snapshot,payment_amount,allocated_amount,payment_method,payment_time,status) "
                + "VALUES(?,?,?,101,'1号楼/1单元/101','测试付款人',?,?, 'CASH',NOW(),'VALID')")) {
            s.setLong(1, paymentId); s.setLong(2, projectId); s.setString(3, prefix + suffix);
            s.setBigDecimal(4, new BigDecimal(amount)); s.setBigDecimal(5, new BigDecimal(amount));
            s.executeUpdate();
        }
        return paymentId;
    }

    private static void insertAllocation(long allocationId, long paymentId, long receivableId,
                                         String amount) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement(
            "INSERT INTO pm_payment_allocation(id,project_id,payment_id,receivable_id,"
                + "allocation_amount,allocation_time) VALUES(?,?,?,?,?,NOW())")) {
            s.setLong(1, allocationId); s.setLong(2, projectId); s.setLong(3, paymentId);
            s.setLong(4, receivableId); s.setBigDecimal(5, new BigDecimal(amount)); s.executeUpdate();
        }
    }

    private static void insertReversal(long reversalId, String no, long paymentId, String amount)
        throws SQLException {
        try (PreparedStatement s = connection.prepareStatement(
            "INSERT INTO pm_payment_reversal(id,project_id,reversal_no,payment_id,reversal_amount,"
                + "reversal_time,reason,create_by) VALUES(?,?,?,?,?,NOW(),'测试冲销',1)")) {
            s.setLong(1, reversalId); s.setLong(2, projectId); s.setString(3, no);
            s.setLong(4, paymentId); s.setBigDecimal(5, new BigDecimal(amount)); s.executeUpdate();
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

    private static boolean indexExists(String table, String index, boolean unique) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement(
            "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() "
                + "AND table_name=? AND index_name=? AND non_unique=?")) {
            s.setString(1, table); s.setString(2, index); s.setInt(3, unique ? 0 : 1);
            try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1) > 0; }
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
