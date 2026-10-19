package org.dromara.property.resource.building.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** 使用隔离 MySQL 的楼栋有效编码约束验收；连接信息仅从环境变量读取。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class BuildingUniqueConstraintMySqlTest {
    private Connection connection;
    private long projectA;
    private long projectB;
    private String prefix;

    @BeforeEach void setUp() throws SQLException {
        connection = openConnection();
        assertEquals("zbm_property_test", connection.getCatalog(), "只允许在隔离测试库执行");
        prefix = "B9C_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        projectA = positiveTestId(); projectB = projectA + 1;
        insertProject(projectA, prefix + "_PA"); insertProject(projectB, prefix + "_PB");
    }

    @AfterEach void cleanUp() throws SQLException {
        if (connection == null) return;
        try (var buildings = connection.prepareStatement("DELETE FROM pm_building WHERE project_id IN (?, ?)");
             var projects = connection.prepareStatement("DELETE FROM pm_project WHERE id IN (?, ?)")) {
            buildings.setLong(1, projectA); buildings.setLong(2, projectB); buildings.executeUpdate();
            projects.setLong(1, projectA); projects.setLong(2, projectB); projects.executeUpdate();
        } finally { connection.close(); }
    }

    @Test void migrationMustExposeStoredGeneratedColumnAndUniqueIndex() throws SQLException {
        try (var column = connection.prepareStatement("SELECT EXTRA, GENERATION_EXPRESSION FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pm_building' AND COLUMN_NAME = 'active_building_code'");
             var index = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pm_building' AND INDEX_NAME = 'uk_pm_building_active_code'")) {
            try (ResultSet result = column.executeQuery()) { assertTrue(result.next()); assertTrue(result.getString("EXTRA").toUpperCase().contains("STORED GENERATED")); assertTrue(result.getString("GENERATION_EXPRESSION").contains("del_flag")); }
            try (ResultSet result = index.executeQuery()) { assertTrue(result.next()); assertEquals(2, result.getInt(1)); }
        }
    }

    @Test void sameProjectActiveCodeMustBeUnique() throws SQLException {
        insertBuilding(positiveTestId(), projectA, "SAME", "0");
        assertThrows(SQLException.class, () -> insertBuilding(positiveTestId(), projectA, "SAME", "0"));
    }

    @Test void differentProjectsMayReuseCode() throws SQLException {
        insertBuilding(positiveTestId(), projectA, "SHARED", "0");
        assertDoesNotThrow(() -> insertBuilding(positiveTestId(), projectB, "SHARED", "0"));
    }

    @Test void logicalDeleteMustAllowReuseAndPreserveRepeatedHistory() throws SQLException {
        for (int round = 0; round < 3; round++) {
            long id = positiveTestId(); insertBuilding(id, projectA, "REUSE", "0");
            try (var statement = connection.prepareStatement("UPDATE pm_building SET del_flag = '1' WHERE id = ?")) { statement.setLong(1, id); assertEquals(1, statement.executeUpdate()); }
        }
        insertBuilding(positiveTestId(), projectA, "REUSE", "0");
        try (var statement = connection.prepareStatement("SELECT active_building_code FROM pm_building WHERE project_id = ? AND building_code = 'REUSE'")) {
            statement.setLong(1, projectA); List<String> keys = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) { while (result.next()) keys.add(result.getString(1)); }
            assertEquals(4, keys.size()); assertEquals(1, keys.stream().filter("REUSE"::equals).count()); assertEquals(3, keys.stream().filter(Objects::isNull).count());
        }
    }

    @Test void concurrentInsertMustAllowAtMostOneActiveRow() throws Exception {
        CountDownLatch start = new CountDownLatch(1); AtomicInteger success = new AtomicInteger();
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        Runnable insert = () -> {
            try (Connection concurrent = openConnection()) {
                start.await();
                try (var statement = concurrent.prepareStatement("INSERT INTO pm_building (id, project_id, building_code, building_name, sort, status, del_flag) VALUES (?, ?, 'RACE', '并发楼栋', 0, '0', '0')")) {
                    statement.setLong(1, positiveTestId()); statement.setLong(2, projectA); statement.executeUpdate(); success.incrementAndGet();
                }
            } catch (Throwable error) { failures.add(error); }
        };
        Thread first = Thread.ofPlatform().start(insert); Thread second = Thread.ofPlatform().start(insert); start.countDown(); first.join(); second.join();
        assertEquals(1, success.get()); assertEquals(1, failures.size()); assertInstanceOf(SQLException.class, failures.getFirst());
    }

    private Connection openConnection() throws SQLException { return DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"), System.getenv("ZBM_TEST_DB_USER"), System.getenv("ZBM_TEST_DB_PASSWORD")); }
    private void insertProject(long id, String code) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO pm_project (id, project_code, project_name, sort, status, del_flag) VALUES (?, ?, ?, 0, '0', '0')")) { statement.setLong(1, id); statement.setString(2, code); statement.setString(3, code); statement.executeUpdate(); }
    }
    private void insertBuilding(long id, long projectId, String code, String delFlag) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO pm_building (id, project_id, building_code, building_name, sort, status, del_flag) VALUES (?, ?, ?, ?, 0, '0', ?)")) { statement.setLong(1, id); statement.setLong(2, projectId); statement.setString(3, code); statement.setString(4, prefix + "_" + code); statement.setString(5, delFlag); statement.executeUpdate(); }
    }
    private static long positiveTestId() { return Math.abs(UUID.randomUUID().getMostSignificantBits() >>> 1); }
}
