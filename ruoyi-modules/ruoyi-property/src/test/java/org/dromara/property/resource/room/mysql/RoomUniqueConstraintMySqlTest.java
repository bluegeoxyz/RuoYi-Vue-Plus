package org.dromara.property.resource.room.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/** 使用隔离 MySQL 验收 V2.0.5 房屋有效编码约束。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class RoomUniqueConstraintMySqlTest {
    private Connection connection; private long projectId; private long buildingId; private long unitId; private String prefix;
    @BeforeEach void setUp() throws SQLException {
        connection=open(); assertEquals("zbm_property_test",connection.getCatalog()); prefix="R11_"+UUID.randomUUID().toString().replace("-","").substring(0,10);
        projectId=id();buildingId=id();unitId=id(); insertProject();insertBuilding();insertUnit();
    }
    @AfterEach void clean() throws SQLException {
        if(connection==null)return;
        try(var r=connection.prepareStatement("DELETE FROM pm_room WHERE project_id=?");var u=connection.prepareStatement("DELETE FROM pm_unit WHERE id=?");var b=connection.prepareStatement("DELETE FROM pm_building WHERE id=?");var p=connection.prepareStatement("DELETE FROM pm_project WHERE id=?")){
            r.setLong(1,projectId);r.executeUpdate();u.setLong(1,unitId);u.executeUpdate();b.setLong(1,buildingId);b.executeUpdate();p.setLong(1,projectId);p.executeUpdate();
        } finally {connection.close();}
    }
    @Test void migrationHasStoredGeneratedColumnAndUniqueIndex() throws SQLException {
        try(var c=connection.prepareStatement("SELECT EXTRA,GENERATION_EXPRESSION FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='pm_room' AND COLUMN_NAME='active_room_code'");var i=connection.prepareStatement("SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='pm_room' AND INDEX_NAME='uk_pm_room_active_code'")){
            try(var rs=c.executeQuery()){assertTrue(rs.next());assertTrue(rs.getString(1).toUpperCase().contains("STORED GENERATED"));assertTrue(rs.getString(2).contains("del_flag"));}
            try(var rs=i.executeQuery()){assertTrue(rs.next());assertEquals(2,rs.getInt(1));}
        }
    }
    @Test void activeCodeIsUniqueWithinProject() throws SQLException {insertRoom(id(),"SAME","0");assertThrows(SQLException.class,()->insertRoom(id(),"SAME","0"));}
    @Test void logicalDeleteAllowsRepeatedReuseAndPreservesHistory() throws SQLException {
        for(int n=0;n<3;n++){long room=id();insertRoom(room,"REUSE","0");try(var s=connection.prepareStatement("UPDATE pm_room SET del_flag='1' WHERE id=?")){s.setLong(1,room);assertEquals(1,s.executeUpdate());}}
        insertRoom(id(),"REUSE","0");
        try(var s=connection.prepareStatement("SELECT active_room_code FROM pm_room WHERE project_id=? AND room_code='REUSE'")){s.setLong(1,projectId);int all=0,active=0;try(var rs=s.executeQuery()){while(rs.next()){all++;if(rs.getString(1)!=null)active++;}}assertEquals(4,all);assertEquals(1,active);}
    }
    @Test void concurrentDuplicateInsertAllowsOnlyOne() throws Exception {
        CountDownLatch start=new CountDownLatch(1);AtomicInteger success=new AtomicInteger();List<Throwable> errors=Collections.synchronizedList(new ArrayList<>());
        Runnable task=()->{try(Connection c=open()){start.await();try(var s=c.prepareStatement("INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,sort,status,del_flag) VALUES(?,?,?,?,?,'101',0,'0','0')")){s.setLong(1,id());s.setLong(2,projectId);s.setLong(3,buildingId);s.setLong(4,unitId);s.setString(5,"RACE");s.executeUpdate();success.incrementAndGet();}}catch(Throwable e){errors.add(e);}};
        Thread a=Thread.ofPlatform().start(task),b=Thread.ofPlatform().start(task);start.countDown();a.join();b.join();assertEquals(1,success.get());assertEquals(1,errors.size());assertInstanceOf(SQLException.class,errors.getFirst());
    }
    private Connection open() throws SQLException{return DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),System.getenv("ZBM_TEST_DB_USER"),System.getenv("ZBM_TEST_DB_PASSWORD"));}
    private void insertProject() throws SQLException{exec("INSERT INTO pm_project(id,project_code,project_name,sort,status,del_flag) VALUES(?,?,?,0,'0','0')",projectId,prefix,prefix);}
    private void insertBuilding() throws SQLException{exec("INSERT INTO pm_building(id,project_id,building_code,building_name,sort,status,del_flag) VALUES(?,?,?,?,0,'0','0')",buildingId,projectId,prefix,"楼栋");}
    private void insertUnit() throws SQLException{exec("INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,sort,status,del_flag) VALUES(?,?,?,?,?,0,'0','0')",unitId,projectId,buildingId,prefix,"单元");}
    private void insertRoom(long room,String code,String flag) throws SQLException{exec("INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,sort,status,del_flag) VALUES(?,?,?,?,?,?,0,'0',?)",room,projectId,buildingId,unitId,code,code,flag);}
    private void exec(String sql,Object... values)throws SQLException{try(var s=connection.prepareStatement(sql)){for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);s.executeUpdate();}}
    private static long id(){return Math.abs(UUID.randomUUID().getMostSignificantBits()>>>1);}
}
