package org.dromara.property.resource.unit.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** 使用隔离 MySQL 验收有效单元编码约束；连接信息仅从环境变量读取。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class BuildingUnitUniqueConstraintMySqlTest {
    private Connection connection;
    private long projectId;
    private long buildingA;
    private long buildingB;
    private String prefix;

    @BeforeEach void setUp() throws SQLException {
        connection = openConnection();
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        prefix = "U10_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        projectId = positiveTestId(); buildingA = positiveTestId(); buildingB = positiveTestId();
        insertProject(); insertBuilding(buildingA, "BA"); insertBuilding(buildingB, "BB");
    }

    @AfterEach void cleanUp() throws SQLException {
        if (connection == null) return;
        try (var units = connection.prepareStatement("DELETE FROM pm_unit WHERE project_id = ?");
             var buildings = connection.prepareStatement("DELETE FROM pm_building WHERE id IN (?, ?)");
             var projects = connection.prepareStatement("DELETE FROM pm_project WHERE id = ?")) {
            units.setLong(1, projectId); units.executeUpdate();
            buildings.setLong(1, buildingA); buildings.setLong(2, buildingB); buildings.executeUpdate();
            projects.setLong(1, projectId); projects.executeUpdate();
        } finally { connection.close(); }
    }

    @Test void migrationExposesStoredGeneratedColumnAndUniqueIndex() throws SQLException {
        try (var column = connection.prepareStatement("SELECT EXTRA, GENERATION_EXPRESSION FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pm_unit' AND COLUMN_NAME = 'active_unit_code'");
             var index = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pm_unit' AND INDEX_NAME = 'uk_pm_unit_active_code'")) {
            try (ResultSet result = column.executeQuery()) { assertTrue(result.next()); assertTrue(result.getString("EXTRA").toUpperCase().contains("STORED GENERATED")); assertTrue(result.getString("GENERATION_EXPRESSION").contains("del_flag")); }
            try (ResultSet result = index.executeQuery()) { assertTrue(result.next()); assertEquals(2, result.getInt(1)); }
        }
    }

    @Test void sameBuildingActiveCodeMustBeUnique() throws SQLException {
        insertUnit(positiveTestId(), buildingA, "SAME", "0");
        assertThrows(SQLException.class, () -> insertUnit(positiveTestId(), buildingA, "SAME", "0"));
    }

    @Test void differentBuildingsMayReuseCode() throws SQLException {
        insertUnit(positiveTestId(), buildingA, "SHARED", "0");
        assertDoesNotThrow(() -> insertUnit(positiveTestId(), buildingB, "SHARED", "0"));
    }

    @Test void logicalDeleteAllowsReuseAndPreservesHistory() throws SQLException {
        for (int round = 0; round < 3; round++) {
            long id = positiveTestId(); insertUnit(id, buildingA, "REUSE", "0");
            try (var statement = connection.prepareStatement("UPDATE pm_unit SET del_flag = '1' WHERE id = ?")) { statement.setLong(1, id); assertEquals(1, statement.executeUpdate()); }
        }
        insertUnit(positiveTestId(), buildingA, "REUSE", "0");
        try (var statement = connection.prepareStatement("SELECT active_unit_code FROM pm_unit WHERE building_id = ? AND unit_code = 'REUSE'")) {
            statement.setLong(1, buildingA); List<String> keys = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) { while (result.next()) keys.add(result.getString(1)); }
            assertEquals(4, keys.size()); assertEquals(1, keys.stream().filter("REUSE"::equals).count()); assertEquals(3, keys.stream().filter(Objects::isNull).count());
        }
    }

    @Test void concurrentInsertAllowsAtMostOneActiveRow() throws Exception {
        CountDownLatch start = new CountDownLatch(1); AtomicInteger success = new AtomicInteger();
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        Runnable insert = () -> {
            try (Connection concurrent = openConnection()) {
                start.await();
                try (var statement = concurrent.prepareStatement("INSERT INTO pm_unit (id, project_id, building_id, unit_code, unit_name, sort, status, del_flag) VALUES (?, ?, ?, 'RACE', '并发单元', 0, '0', '0')")) {
                    statement.setLong(1, positiveTestId()); statement.setLong(2, projectId); statement.setLong(3, buildingA); statement.executeUpdate(); success.incrementAndGet();
                }
            } catch (Throwable error) { failures.add(error); }
        };
        Thread first = Thread.ofPlatform().start(insert); Thread second = Thread.ofPlatform().start(insert);
        start.countDown(); first.join(); second.join();
        assertEquals(1, success.get()); assertEquals(1, failures.size()); assertInstanceOf(SQLException.class, failures.getFirst());
    }

    private Connection openConnection() throws SQLException { return DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"), System.getenv("ZBM_TEST_DB_USER"), System.getenv("ZBM_TEST_DB_PASSWORD")); }
    private void insertProject() throws SQLException { try (var s = connection.prepareStatement("INSERT INTO pm_project (id, project_code, project_name, sort, status, del_flag) VALUES (?, ?, ?, 0, '0', '0')")) { s.setLong(1, projectId); s.setString(2, prefix); s.setString(3, prefix); s.executeUpdate(); } }
    private void insertBuilding(long id, String code) throws SQLException { try (var s = connection.prepareStatement("INSERT INTO pm_building (id, project_id, building_code, building_name, sort, status, del_flag) VALUES (?, ?, ?, ?, 0, '0', '0')")) { s.setLong(1, id); s.setLong(2, projectId); s.setString(3, prefix + code); s.setString(4, code); s.executeUpdate(); } }
    private void insertUnit(long id, long buildingId, String code, String delFlag) throws SQLException { try (var s = connection.prepareStatement("INSERT INTO pm_unit (id, project_id, building_id, unit_code, unit_name, sort, status, del_flag) VALUES (?, ?, ?, ?, ?, 0, '0', ?)")) { s.setLong(1, id); s.setLong(2, projectId); s.setLong(3, buildingId); s.setString(4, code); s.setString(5, prefix + code); s.setString(6, delFlag); s.executeUpdate(); } }
    private static long positiveTestId() { return Math.abs(UUID.randomUUID().getMostSignificantBits() >>> 1); }
}
