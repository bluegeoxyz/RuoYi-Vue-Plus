package org.dromara.property.charge.receivable.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** TASK-019A 应收模型、约束和迁移的真实 MySQL 验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReceivableSchemaMySqlTest {
    private static Connection connection;
    private static long bindingId;
    private static String prefix;

    @BeforeAll
    static void setUpSchema() throws Exception {
        connection = DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"), System.getenv("ZBM_TEST_DB_USER"), System.getenv("ZBM_TEST_DB_PASSWORD"));
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        if (!tableExists("pm_receivable")) {
            executeMigration(repositoryRoot().resolve("script/sql/zbm/archive/legacy-development-migrations/V2.0.20__receivable_schema_dict_menu.sql"));
        }
        bindingId = id();
        prefix = "T19A" + Long.toUnsignedString(bindingId);
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (connection == null) return;
        if (tableExists("pm_receivable")) {
            try (PreparedStatement s = connection.prepareStatement("DELETE FROM pm_receivable WHERE binding_id=?")) {
                s.setLong(1, bindingId); s.executeUpdate();
            }
        }
        if (tableExists("pm_receivable_batch")) {
            try (PreparedStatement s = connection.prepareStatement("DELETE FROM pm_receivable_batch WHERE batch_no LIKE ?")) {
                s.setString(1, prefix + "%"); s.executeUpdate();
            }
        }
        connection.close();
    }

    @Test @Order(1)
    void migrationAndBaselineExposeEquivalentFinalStructure() throws Exception {
        assertTrue(tableExists("pm_receivable_batch"));
        assertTrue(tableExists("pm_receivable"));
        assertEquals(3, count("information_schema.columns", "table_name='pm_charge_rule' AND column_name IN ('price_cycle','due_date_mode','due_date_offset_days')"));
        assertEquals(2, count("information_schema.tables", "table_name IN ('pm_receivable_batch','pm_receivable')"));
        assertEquals(8, count("information_schema.table_constraints", "table_name='pm_receivable' AND constraint_type='CHECK'"));
        assertEquals(9, count("information_schema.statistics", "table_name='pm_receivable'"));
        String baseline = Files.readString(repositoryRoot().resolve("script/sql/zbm/baseline/01_schema.sql"));
        for (String token : new String[]{"`price_cycle`", "`due_date_mode`", "CREATE TABLE `pm_receivable_batch`", "CREATE TABLE `pm_receivable`", "`active_period_key`"}) {
            assertTrue(baseline.contains(token), "baseline 缺少 " + token);
        }
        assertBaselineTableEquivalent(baseline, "pm_charge_rule");
        assertBaselineTableEquivalent(baseline, "pm_receivable_batch");
        assertBaselineTableEquivalent(baseline, "pm_receivable");
        assertEquals(3, scalar("SELECT COUNT(*) FROM sys_dict_type WHERE dict_type IN ('pm_due_date_mode','pm_receivable_batch_status','pm_payment_status')"));
        assertEquals(5, scalar("SELECT COUNT(*) FROM sys_menu WHERE perms IN ('property:receivable:list','property:receivable:query','property:receivable:generate','property:receivable:cancel','property:receivable:export')"));
    }

    @Test @Order(2)
    void batchStatusMonthAndCountersAreConstrained() throws SQLException {
        insertBatch(id(), prefix + "B1", LocalDate.of(2026, 5, 1), "RUNNING", 0);
        assertThrows(SQLException.class, () -> insertBatch(id(), prefix + "B2", LocalDate.of(2026, 5, 2), "RUNNING", 0));
        assertThrows(SQLException.class, () -> insertBatch(id(), prefix + "B3", LocalDate.of(2026, 5, 1), "DONE", 0));
        assertThrows(SQLException.class, () -> insertBatch(id(), prefix + "B4", LocalDate.of(2026, 5, 1), "FAILED", -1));
    }

    @Test @Order(3)
    void periodDayAmountAndPaymentStatusAreConstrained() throws SQLException {
        long batchId = id(); insertBatch(batchId, prefix + "B5", LocalDate.of(2026, 5, 1), "SUCCESS", 0);
        assertDoesNotThrow(() -> insertReceivable(id(), prefix + "R1", batchId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), 31, 31, "UNPAID", 200, 0));
        assertThrows(SQLException.class, () -> insertReceivable(id(), prefix + "R2", batchId, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 31), 31, 31, "UNPAID", 200, 0));
        assertThrows(SQLException.class, () -> insertReceivable(id(), prefix + "R3", batchId, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 30, 31, "UNPAID", 200, 0));
        assertThrows(SQLException.class, () -> insertReceivable(id(), prefix + "R4", batchId, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 30, 30, "OVERDUE", 200, 0));
        assertThrows(SQLException.class, () -> insertReceivable(id(), prefix + "R5", batchId, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 30, 30, "PARTIAL", 200, 201));
    }

    @Test @Order(4)
    void activePeriodIsUniqueAndCancellationAllowsRegeneration() throws SQLException {
        long batchId = id(); insertBatch(batchId, prefix + "B6", LocalDate.of(2026, 7, 1), "SUCCESS", 0);
        long firstId = id();
        insertReceivable(firstId, prefix + "R6", batchId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), 31, 31, "UNPAID", 200, 0);
        assertThrows(SQLException.class, () -> insertReceivable(id(), prefix + "R7", batchId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), 31, 31, "UNPAID", 200, 0));
        try (PreparedStatement s = connection.prepareStatement("UPDATE pm_receivable SET payment_status='CANCELLED',cancel_reason='test',cancelled_at=NOW() WHERE id=?")) {
            s.setLong(1, firstId); assertEquals(1, s.executeUpdate());
        }
        assertDoesNotThrow(() -> insertReceivable(id(), prefix + "R8", batchId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), 31, 31, "UNPAID", 200, 0));
    }

    @Test @Order(5)
    void unpaidHistoryDoesNotBlockTheNextPeriod() throws SQLException {
        long batchId = id(); insertBatch(batchId, prefix + "B7", LocalDate.of(2026, 8, 1), "SUCCESS", 0);
        insertReceivable(id(), prefix + "R9", batchId, LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 31), 31, 31, "UNPAID", 200, 0);
        assertDoesNotThrow(() -> insertReceivable(id(), prefix + "R10", batchId, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), 31, 31, "UNPAID", 200, 0));
    }

    @Test @Order(6)
    void amountPrecisionAndBatchStatisticsPersistInMySql() throws SQLException {
        long batchId = id(); insertBatch(batchId, prefix + "B8", LocalDate.of(2026, 9, 1), "RUNNING", 0);
        insertReceivable(id(), prefix + "R11", batchId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), 30, 21, "UNPAID", 140.005, 0);
        try (PreparedStatement s = connection.prepareStatement("SELECT receivable_amount FROM pm_receivable WHERE receivable_no=?")) {
            s.setString(1, prefix + "R11"); try (ResultSet r = s.executeQuery()) { assertTrue(r.next()); assertEquals("140.01", r.getBigDecimal(1).toPlainString()); }
        }
        try (PreparedStatement s = connection.prepareStatement("UPDATE pm_receivable_batch SET status='SUCCESS',scanned_count=3,generated_count=1,skipped_existing_count=1,skipped_not_due_count=1,failed_count=0,finished_at=NOW() WHERE id=?")) {
            s.setLong(1, batchId); assertEquals(1, s.executeUpdate());
        }
        assertEquals(1, scalar("SELECT COUNT(*) FROM pm_receivable_batch WHERE id=" + batchId + " AND status='SUCCESS' AND scanned_count=3 AND generated_count=1 AND skipped_existing_count=1 AND skipped_not_due_count=1 AND failed_count=0"));
    }

    @Test @Order(7)
    void databaseClockSupportsImmediateBatchFinish() throws SQLException {
        long batchId = id();
        insertBatch(batchId, prefix + "B9", LocalDate.of(2026, 10, 1), "RUNNING", 0);
        try (PreparedStatement start = connection.prepareStatement("UPDATE pm_receivable_batch SET started_at=NOW() WHERE id=?");
             PreparedStatement finish = connection.prepareStatement("UPDATE pm_receivable_batch SET status='SUCCESS',finished_at=NOW() WHERE id=?")) {
            start.setLong(1, batchId); assertEquals(1, start.executeUpdate());
            finish.setLong(1, batchId); assertEquals(1, finish.executeUpdate());
        }
        assertEquals(1, scalar("SELECT COUNT(*) FROM pm_receivable_batch WHERE id=" + batchId + " AND finished_at>=started_at"));
    }

    @Test @Order(8)
    void sameRoomAndPeriodAllowDifferentChargeBindings() throws SQLException {
        long batchId = id(); insertBatch(batchId, prefix + "B10", LocalDate.of(2026, 11, 1), "SUCCESS", 0);
        long originalBindingId = bindingId;
        try {
            bindingId = id();
            insertReceivable(id(), prefix + "R12", batchId, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 30, 30, "UNPAID", 200, 0);
            bindingId = id();
            insertReceivable(id(), prefix + "R13", batchId, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 30, 30, "UNPAID", 30, 0);
            assertEquals(2, scalar("SELECT COUNT(*) FROM pm_receivable WHERE receivable_no IN ('" + prefix + "R12','" + prefix + "R13')"));
        } finally {
            try (PreparedStatement s = connection.prepareStatement("DELETE FROM pm_receivable WHERE receivable_no IN (?,?)")) {
                s.setString(1, prefix + "R12"); s.setString(2, prefix + "R13"); s.executeUpdate();
            }
            bindingId = originalBindingId;
        }
    }

    private static void insertBatch(long id, String no, LocalDate month, String status, int failed) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement("INSERT INTO pm_receivable_batch(id,project_id,batch_no,generate_month,status,failed_count) VALUES(?,1,?,?,?,?)")) {
            s.setLong(1, id); s.setString(2, no); s.setDate(3, Date.valueOf(month)); s.setString(4, status); s.setInt(5, failed); s.executeUpdate();
        }
    }

    private static void insertReceivable(long id, String no, long batchId, LocalDate start, LocalDate end, int theoreticalDays, int actualDays, String status, double amount, double paid) throws SQLException {
        String sql = "INSERT INTO pm_receivable(id,project_id,receivable_no,batch_id,binding_id,room_id,charge_item_id,charge_rule_id,generate_month,period_start_date,period_end_date,theoretical_start_date,theoretical_end_date,due_date,room_full_name_snapshot,charge_item_code_snapshot,charge_item_name_snapshot,charge_rule_code_snapshot,charge_rule_name_snapshot,billing_method_snapshot,billing_cycle_snapshot,price_cycle_snapshot,cycle_mode_snapshot,due_date_mode_snapshot,due_date_offset_days_snapshot,area_snapshot,unit_price_snapshot,theoretical_days,actual_days,receivable_amount,paid_amount,payment_status) VALUES(?,1,?,?,?,7,11,12,?,?,?,?,?,?,'1号楼/1单元/101','PROPERTY_FEE','物业费','RULE','物业费规则','AREA','MONTH','MONTH','NATURAL','AFTER_PERIOD_START',0,100,2,?,?,?,?,?)";
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            int i=1; s.setLong(i++,id);s.setString(i++,no);s.setLong(i++,batchId);s.setLong(i++,bindingId);
            s.setDate(i++,Date.valueOf(start.withDayOfMonth(1)));s.setDate(i++,Date.valueOf(start));s.setDate(i++,Date.valueOf(end));
            s.setDate(i++,Date.valueOf(start));s.setDate(i++,Date.valueOf(end));s.setDate(i++,Date.valueOf(start.plusDays(7)));
            s.setInt(i++,theoreticalDays);s.setInt(i++,actualDays);s.setDouble(i++,amount);s.setDouble(i++,paid);s.setString(i,status);s.executeUpdate();
        }
    }

    private static boolean tableExists(String table) throws SQLException {
        try (PreparedStatement s=connection.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")){s.setString(1,table);try(ResultSet r=s.executeQuery()){r.next();return r.getInt(1)==1;}}
    }
    private static int count(String table,String predicate)throws SQLException{return scalar("SELECT COUNT(DISTINCT "+(table.endsWith("statistics")?"index_name":table.endsWith("columns")?"column_name":table.endsWith("tables")?"table_name":"constraint_name")+") FROM "+table+" WHERE table_schema=DATABASE() AND "+predicate);}
    private static int scalar(String sql)throws SQLException{try(Statement s=connection.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    private static void executeMigration(Path path)throws Exception{String sql=Files.readString(path).replaceAll("(?s)/\\*.*?\\*/","").replaceAll("(?m)--.*$","");for(String part:sql.split(";")){if(!part.isBlank())try(Statement s=connection.createStatement()){s.execute(part);}}}
    private static void assertBaselineTableEquivalent(String baseline,String table)throws SQLException{String marker="CREATE TABLE `"+table+"`";int start=baseline.indexOf(marker),end=baseline.indexOf(";",start);assertTrue(start>=0&&end>start);String probe=table+"_baseline_probe";String ddl=baseline.substring(start,end).replaceFirst("CREATE TABLE `"+table+"`","CREATE TABLE `"+probe+"`").replaceAll("CONSTRAINT `([^`]+)`","CONSTRAINT `$1_probe`");try(Statement s=connection.createStatement()){s.execute("DROP TABLE IF EXISTS `"+probe+"`");s.execute(ddl);assertEquals(metadata("columns",table),metadata("columns",probe));assertEquals(metadata("indexes",table),metadata("indexes",probe));assertEquals(metadata("checks",table),metadata("checks",probe));}finally{try(Statement s=connection.createStatement()){s.execute("DROP TABLE IF EXISTS `"+probe+"`");}}}
    private static List<String> metadata(String kind,String table)throws SQLException{String sql=switch(kind){case "columns"->"SELECT CONCAT_WS('|',ordinal_position,column_name,column_type,is_nullable,COALESCE(column_default,'<NULL>'),extra,COALESCE(generation_expression,'')) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=? ORDER BY ordinal_position";case "indexes"->"SELECT CONCAT_WS('|',index_name,non_unique,seq_in_index,column_name,COALESCE(sub_part,'')) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name=? ORDER BY index_name,seq_in_index";default->"SELECT REPLACE(cc.check_clause,'`','') FROM information_schema.table_constraints tc JOIN information_schema.check_constraints cc ON cc.constraint_schema=tc.constraint_schema AND cc.constraint_name=tc.constraint_name WHERE tc.table_schema=DATABASE() AND tc.table_name=? AND tc.constraint_type='CHECK' ORDER BY cc.check_clause";};List<String> rows=new ArrayList<>();try(PreparedStatement s=connection.prepareStatement(sql)){s.setString(1,table);try(ResultSet r=s.executeQuery()){while(r.next())rows.add(r.getString(1).replace(table+"_baseline_probe",table));}}return rows;}
    private static Path repositoryRoot(){Path path=Path.of(System.getProperty("user.dir")).toAbsolutePath();while(path!=null&&!Files.exists(path.resolve("script/sql/zbm")))path=path.getParent();return java.util.Objects.requireNonNull(path,"repository root");}
    private static long id(){return Math.abs(UUID.randomUUID().getMostSignificantBits()>>>1);}
}
