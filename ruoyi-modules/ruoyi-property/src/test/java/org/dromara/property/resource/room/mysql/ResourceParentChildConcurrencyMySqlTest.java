package org.dromara.property.resource.room.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** 使用两个真实连接验证 Building -> Unit -> Room 的父子资源锁协议。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class ResourceParentChildConcurrencyMySqlTest {
    private Connection admin; private long projectId,buildingId,unitId; private String prefix;
    @BeforeEach void setUp() throws SQLException {admin=open();assertEquals("zbm_property_test",admin.getCatalog());projectId=id();buildingId=id();unitId=id();prefix="L11_"+UUID.randomUUID().toString().replace("-","").substring(0,8);seed();}
    @AfterEach void clean() throws SQLException {if(admin==null)return;exec(admin,"DELETE FROM pm_room WHERE project_id=?",projectId);exec(admin,"DELETE FROM pm_unit WHERE project_id=?",projectId);exec(admin,"DELETE FROM pm_building WHERE project_id=?",projectId);exec(admin,"DELETE FROM pm_project WHERE id=?",projectId);admin.close();}

    @Test void roomInsertFirstMakesFollowingUnitDeleteReject() throws Exception {
        long roomId=id();CountDownLatch locked=new CountDownLatch(1),attempted=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)){
            Future<Void> insert=pool.submit(()->{try(Connection c=tx()){lockBuilding(c);lockUnit(c);insertRoom(c,roomId);locked.countDown();await(attempted);c.commit();return null;}});
            Future<Boolean> delete=pool.submit(()->{await(locked);try(Connection c=tx()){attempted.countDown();lockBuilding(c);assertNotNull(lockUnit(c));lockRoomRange(c);boolean has=count(c,"SELECT COUNT(*) FROM pm_room WHERE unit_id=? AND del_flag='0'",unitId)>0;c.rollback();return has;}});
            insert.get(10,TimeUnit.SECONDS);assertTrue(delete.get(10,TimeUnit.SECONDS));
        }
        assertEquals(1,count(admin,"SELECT COUNT(*) FROM pm_unit WHERE id=? AND del_flag='0'",unitId));assertEquals(1,count(admin,"SELECT COUNT(*) FROM pm_room WHERE id=? AND del_flag='0'",roomId));
    }

    @Test void unitDeleteFirstMakesFollowingRoomInsertReject() throws Exception {
        CountDownLatch locked=new CountDownLatch(1),attempted=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)){
            Future<Void> delete=pool.submit(()->{try(Connection c=tx()){lockBuilding(c);assertNotNull(lockUnit(c));lockRoomRange(c);assertEquals(0,count(c,"SELECT COUNT(*) FROM pm_room WHERE unit_id=? AND del_flag='0'",unitId));exec(c,"UPDATE pm_unit SET del_flag='1' WHERE id=?",unitId);locked.countDown();await(attempted);c.commit();return null;}});
            Future<Boolean> insert=pool.submit(()->{await(locked);try(Connection c=tx()){attempted.countDown();lockBuilding(c);boolean active=lockUnit(c)!=null;c.rollback();return active;}});
            delete.get(10,TimeUnit.SECONDS);assertFalse(insert.get(10,TimeUnit.SECONDS));
        }
        assertEquals(0,count(admin,"SELECT COUNT(*) FROM pm_unit WHERE id=? AND del_flag='0'",unitId));assertEquals(0,count(admin,"SELECT COUNT(*) FROM pm_room WHERE unit_id=? AND del_flag='0'",unitId));
    }

    @Test void unitInsertFirstMakesFollowingBuildingDeleteReject() throws Exception {
        long newUnit=id();CountDownLatch locked=new CountDownLatch(1),attempted=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)){
            Future<Void> insert=pool.submit(()->{try(Connection c=tx()){lockBuilding(c);insertUnit(c,newUnit,"NEW");locked.countDown();await(attempted);c.commit();return null;}});
            Future<Boolean> delete=pool.submit(()->{await(locked);try(Connection c=tx()){attempted.countDown();lockBuilding(c);lockUnitRange(c);lockBuildingRoomRange(c);boolean has=count(c,"SELECT COUNT(*) FROM pm_unit WHERE building_id=? AND del_flag='0'",buildingId)>0;c.rollback();return has;}});
            insert.get(10,TimeUnit.SECONDS);assertTrue(delete.get(10,TimeUnit.SECONDS));
        }
        assertEquals(1,count(admin,"SELECT COUNT(*) FROM pm_building WHERE id=? AND del_flag='0'",buildingId));assertEquals(1,count(admin,"SELECT COUNT(*) FROM pm_unit WHERE id=? AND del_flag='0'",newUnit));
    }

    @Test void buildingDeleteFirstMakesFollowingUnitInsertReject() throws Exception {
        exec(admin,"UPDATE pm_unit SET del_flag='1' WHERE id=?",unitId);long newUnit=id();CountDownLatch locked=new CountDownLatch(1),attempted=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)){
            Future<Void> delete=pool.submit(()->{try(Connection c=tx()){assertNotNull(lockBuilding(c));lockUnitRange(c);lockBuildingRoomRange(c);assertEquals(0,count(c,"SELECT COUNT(*) FROM pm_unit WHERE building_id=? AND del_flag='0'",buildingId));exec(c,"UPDATE pm_building SET del_flag='1' WHERE id=?",buildingId);locked.countDown();await(attempted);c.commit();return null;}});
            Future<Boolean> insert=pool.submit(()->{await(locked);try(Connection c=tx()){attempted.countDown();boolean active=lockBuilding(c)!=null;c.rollback();return active;}});
            delete.get(10,TimeUnit.SECONDS);assertFalse(insert.get(10,TimeUnit.SECONDS));
        }
        assertEquals(0,count(admin,"SELECT COUNT(*) FROM pm_building WHERE id=? AND del_flag='0'",buildingId));assertEquals(0,count(admin,"SELECT COUNT(*) FROM pm_unit WHERE id=?",newUnit));
    }

    private Connection tx() throws SQLException {Connection c=open();c.setAutoCommit(false);return c;}
    private Long lockBuilding(Connection c)throws SQLException{return one(c,"SELECT id FROM pm_building WHERE id=? AND del_flag='0' FOR UPDATE",buildingId);}
    private Long lockUnit(Connection c)throws SQLException{return one(c,"SELECT id FROM pm_unit WHERE id=? AND del_flag='0' FOR UPDATE",unitId);}
    private void lockUnitRange(Connection c)throws SQLException{query(c,"SELECT id FROM pm_unit FORCE INDEX(idx_pm_unit_building) WHERE building_id=? FOR UPDATE",buildingId);}
    private void lockRoomRange(Connection c)throws SQLException{query(c,"SELECT id FROM pm_room FORCE INDEX(idx_pm_room_unit) WHERE unit_id=? FOR UPDATE",unitId);}
    private void lockBuildingRoomRange(Connection c)throws SQLException{query(c,"SELECT id FROM pm_room FORCE INDEX(idx_pm_room_building) WHERE building_id=? FOR UPDATE",buildingId);}
    private void insertRoom(Connection c,long room)throws SQLException{exec(c,"INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,sort,status,del_flag) VALUES(?,?,?,?,?,'101',0,'0','0')",room,projectId,buildingId,unitId,prefix+room);}
    private void insertUnit(Connection c,long unit,String code)throws SQLException{exec(c,"INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,sort,status,del_flag) VALUES(?,?,?,?,?,0,'0','0')",unit,projectId,buildingId,prefix+code,code);}
    private void seed()throws SQLException{exec(admin,"INSERT INTO pm_project(id,project_code,project_name,sort,status,del_flag) VALUES(?,?,?,0,'0','0')",projectId,prefix,prefix);exec(admin,"INSERT INTO pm_building(id,project_id,building_code,building_name,sort,status,del_flag) VALUES(?,?,?,?,0,'0','0')",buildingId,projectId,prefix,"楼栋");insertUnit(admin,unitId,"BASE");}
    private Connection open()throws SQLException{return DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),System.getenv("ZBM_TEST_DB_USER"),System.getenv("ZBM_TEST_DB_PASSWORD"));}
    private static void await(CountDownLatch latch)throws InterruptedException{assertTrue(latch.await(5,TimeUnit.SECONDS));}
    private static void query(Connection c,String sql,long value)throws SQLException{try(var s=c.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){while(rs.next()){}}}}
    private static Long one(Connection c,String sql,long value)throws SQLException{try(var s=c.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){return rs.next()?rs.getLong(1):null;}}}
    private static int count(Connection c,String sql,long value)throws SQLException{try(var s=c.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){assertTrue(rs.next());return rs.getInt(1);}}}
    private static void exec(Connection c,String sql,Object... values)throws SQLException{try(var s=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);s.executeUpdate();}}
    private static long id(){return Math.abs(UUID.randomUUID().getMostSignificantBits()>>>1);}
}
