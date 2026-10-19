package org.dromara.property.charge.item.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** 收费项目 V2.0.17 结构与逻辑删除唯一约束的真实 MySQL 验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class ChargeItemMySqlTest {
    private Connection connection;
    private long projectA;
    private long projectB;

    @BeforeEach void setUp() throws SQLException {
        connection = DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"), System.getenv("ZBM_TEST_DB_USER"), System.getenv("ZBM_TEST_DB_PASSWORD"));
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        projectA = id(); projectB = id();
        insertProject(projectA); insertProject(projectB);
    }

    @AfterEach void cleanUp() throws SQLException {
        if (connection == null) return;
        try (PreparedStatement items = connection.prepareStatement("DELETE FROM pm_charge_item WHERE project_id IN (?,?)");
             PreparedStatement projects = connection.prepareStatement("DELETE FROM pm_project WHERE id IN (?,?)")) {
            items.setLong(1, projectA); items.setLong(2, projectB); items.executeUpdate();
            projects.setLong(1, projectA); projects.setLong(2, projectB); projects.executeUpdate();
        } finally { connection.close(); }
    }

    @Test void refinedObjectConstraintMustAcceptOnlyRoomAndParkingSpace() throws SQLException {
        assertDoesNotThrow(() -> insertItem(id(), projectA, "ROOM_FEE", "ROOM", "0"));
        assertDoesNotThrow(() -> insertItem(id(), projectA, "PARKING_FEE", "PARKING_SPACE", "0"));
        assertThrows(SQLException.class, () -> insertItem(id(), projectA, "OWNER_FEE", "OWNER", "0"));
        assertThrows(SQLException.class, () -> insertItem(id(), projectA, "HOUSEHOLD_FEE", "HOUSEHOLD", "0"));
    }

    @Test void sameProjectActiveCodeMustBeUniqueButDifferentProjectsMayReuseIt() throws SQLException {
        insertItem(id(), projectA, "SHARED", "ROOM", "0");
        assertThrows(SQLException.class, () -> insertItem(id(), projectA, "SHARED", "ROOM", "0"));
        assertDoesNotThrow(() -> insertItem(id(), projectB, "SHARED", "ROOM", "0"));
    }

    @Test void logicalDeleteMustAllowCodeReuse() throws SQLException {
        insertItem(id(), projectA, "REUSE", "ROOM", "1");
        assertDoesNotThrow(() -> insertItem(id(), projectA, "REUSE", "ROOM", "0"));
    }

    @Test void obsoleteDictionaryValuesMustBeRemoved() throws SQLException {
        try (PreparedStatement s = connection.prepareStatement("SELECT COUNT(*) FROM sys_dict_data WHERE dict_type='pm_charge_object_type' AND dict_value IN ('OWNER','HOUSEHOLD')"); ResultSet r = s.executeQuery()) {
            assertTrue(r.next()); assertEquals(0, r.getLong(1));
        }
    }

    private void insertProject(long projectId) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement("INSERT INTO pm_project (id,project_code,project_name,sort,status,del_flag) VALUES (?,?,?,0,'0','0')")) {
            String code = "CI_" + projectId; s.setLong(1, projectId); s.setString(2, code); s.setString(3, code); s.executeUpdate();
        }
    }

    private void insertItem(long itemId, long projectId, String code, String objectType, String delFlag) throws SQLException {
        try (PreparedStatement s = connection.prepareStatement("INSERT INTO pm_charge_item (id,project_id,item_code,item_name,charge_type,charge_category,charge_object_type,status,sort,del_flag) VALUES (?,?,?,?,'PERIODIC','PROPERTY_FEE',?,'0',0,?)")) {
            s.setLong(1, itemId); s.setLong(2, projectId); s.setString(3, code); s.setString(4, code); s.setString(5, objectType); s.setString(6, delFlag); s.executeUpdate();
        }
    }

    private static long id() { return Math.abs(UUID.randomUUID().getMostSignificantBits() >>> 1); }
}
