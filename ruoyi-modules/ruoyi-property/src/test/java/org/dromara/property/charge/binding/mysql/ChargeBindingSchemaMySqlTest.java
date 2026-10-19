package org.dromara.property.charge.binding.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** TASK-018A 收费载体绑定结构的真实 MySQL 验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class ChargeBindingSchemaMySqlTest {
    private Connection connection;
    private long objectId;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"), System.getenv("ZBM_TEST_DB_USER"), System.getenv("ZBM_TEST_DB_PASSWORD"));
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        objectId = id();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (connection == null) return;
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM pm_charge_binding WHERE object_id=?")) {
            statement.setLong(1, objectId);
            statement.executeUpdate();
        } finally {
            connection.close();
        }
    }

    @Test
    void nonOverlappingHistoryAndOneOpenEndedBindingCanCoexist() throws SQLException {
        insert(id(), 11L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "0");
        insert(id(), 11L, LocalDate.of(2026, 7, 1), null, "0");
        assertEquals(2, count());
    }

    @Test
    void duplicateOpenEndedBindingForSameObjectAndItemMustFail() throws SQLException {
        insert(id(), 11L, LocalDate.of(2026, 1, 1), null, "0");
        assertThrows(SQLException.class, () -> insert(id(), 11L, LocalDate.of(2026, 7, 1), null, "0"));
    }

    @Test
    void logicalDeleteAllowsAReplacementOpenEndedBinding() throws SQLException {
        insert(id(), 11L, LocalDate.of(2026, 1, 1), null, "1");
        assertDoesNotThrow(() -> insert(id(), 11L, LocalDate.of(2026, 7, 1), null, "0"));
    }

    @Test
    void invalidDateStatusAndObjectTypeMustBeRejected() {
        assertThrows(SQLException.class, () -> insert(id(), 11L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 6, 30), "0"));
        assertThrows(SQLException.class, () -> insertRaw(id(), "VISITOR", 12L, LocalDate.now(), null, "0", "0"));
        assertThrows(SQLException.class, () -> insertRaw(id(), "ROOM", 13L, LocalDate.now(), null, "2", "0"));
    }

    @Test
    void expectedIndexesAndGeneratedColumnMustExist() throws SQLException {
        assertTrue(metadataCount("information_schema.columns", "column_name='active_open_charge_item_id' AND extra LIKE '%STORED GENERATED%'") > 0);
        assertEquals(4, metadataCount("information_schema.statistics", "index_name IN ('idx_pm_charge_binding_project','idx_pm_charge_binding_object','idx_pm_charge_binding_item','idx_pm_charge_binding_rule')"));
    }

    private void insert(long bindingId, long itemId, LocalDate start, LocalDate end, String delFlag) throws SQLException {
        insertRaw(bindingId, "ROOM", itemId, start, end, "0", delFlag);
    }

    private void insertRaw(long bindingId, String objectType, long itemId, LocalDate start, LocalDate end, String status, String delFlag) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement("INSERT INTO pm_charge_binding(id,project_id,object_type,object_id,charge_item_id,charge_rule_id,effective_start_date,effective_end_date,status,sort,del_flag) VALUES(?,?,?,?,?,?,?,?,?,0,?)")) {
            s.setLong(1, bindingId); s.setLong(2, 1L); s.setString(3, objectType); s.setLong(4, objectId);
            s.setLong(5, itemId); s.setLong(6, itemId + 100); s.setDate(7, Date.valueOf(start));
            if (end == null) s.setNull(8, Types.DATE); else s.setDate(8, Date.valueOf(end));
            s.setString(9, status); s.setString(10, delFlag); s.executeUpdate();
        }
    }

    private int count() throws SQLException {
        try (PreparedStatement s = connection.prepareStatement("SELECT COUNT(*) FROM pm_charge_binding WHERE object_id=?")) {
            s.setLong(1, objectId); try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    private int metadataCount(String table, String predicate) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT " + (table.endsWith("columns") ? "column_name" : "index_name") + ") FROM " + table + " WHERE table_schema=DATABASE() AND table_name='pm_charge_binding' AND " + predicate;
        try (Statement s = connection.createStatement(); ResultSet r = s.executeQuery(sql)) { r.next(); return r.getInt(1); }
    }

    private static long id() { return Math.abs(UUID.randomUUID().getMostSignificantBits() >>> 1); }
}
