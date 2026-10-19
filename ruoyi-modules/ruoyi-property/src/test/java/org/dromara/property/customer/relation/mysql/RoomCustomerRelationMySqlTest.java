package org.dromara.property.customer.relation.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** V2.0.10 唯一约束及 Room -> Customer -> Relation 双连接协议。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class RoomCustomerRelationMySqlTest {
    private Connection admin;
    private long projectId, buildingId, unitId, roomId, customerId;
    private String prefix;

    @BeforeEach void setUp() throws SQLException {
        admin = open();
        assertEquals("zbm_property_test", admin.getCatalog());
        projectId=id(); buildingId=id(); unitId=id(); roomId=id(); customerId=id();
        prefix="R13_"+UUID.randomUUID().toString().replace("-", "").substring(0,8);
        seed();
    }

    @AfterEach void clean() throws SQLException {
        if (admin == null) return;
        exec(admin,"DELETE FROM pm_room_customer_rel WHERE project_id=?",projectId);
        exec(admin,"DELETE FROM pm_customer WHERE id=?",customerId);
        exec(admin,"DELETE FROM pm_room WHERE id=?",roomId);
        exec(admin,"DELETE FROM pm_unit WHERE id=?",unitId);
        exec(admin,"DELETE FROM pm_building WHERE id=?",buildingId);
        exec(admin,"DELETE FROM pm_project WHERE id=?",projectId);
        admin.close();
    }

    @Test void effectiveUniqueAllowsEndedAndLogicalDeleteHistory() throws SQLException {
        long first=id(); insertRelation(admin,first,"PENDING");
        assertThrows(SQLException.class,()->insertRelation(admin,id(),"ACTIVE"));
        exec(admin,"UPDATE pm_room_customer_rel SET relation_status='ENDED',end_date=CURRENT_DATE WHERE id=?",first);
        long second=id(); insertRelation(admin,second,"ACTIVE");
        exec(admin,"UPDATE pm_room_customer_rel SET del_flag='1' WHERE id=?",second);
        insertRelation(admin,id(),"PENDING");
        assertEquals(3,count(admin,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE project_id=?",projectId));
    }

    @Test void concurrentDuplicateInsertAllowsAtMostOne() throws Exception {
        CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Callable<Boolean> task=()->{try(Connection c=open()){ready.countDown();await(start);try{insertRelation(c,id(),"PENDING");return true;}catch(SQLException e){return false;}}};
            Future<Boolean> a=pool.submit(task),b=pool.submit(task); await(ready); start.countDown();
            int successes=(a.get(10,TimeUnit.SECONDS)?1:0)+(b.get(10,TimeUnit.SECONDS)?1:0);
            assertEquals(1,successes);
        }
        assertEquals(1,count(admin,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE project_id=? AND effective_relation_key=1",projectId));
    }

    @Test void relationFirstMakesRoomDeleteRejectAndRoomDeleteFirstMakesRelationReject() throws Exception {
        CountDownLatch locked=new CountDownLatch(1),attempted=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Future<Void> relation=pool.submit(()->{try(Connection c=tx()){lockRoom(c);lockCustomer(c);insertRelation(c,id(),"PENDING");locked.countDown();await(attempted);c.commit();return null;}});
            Future<Boolean> deletion=pool.submit(()->{await(locked);try(Connection c=tx()){attempted.countDown();lockRoom(c);lockRoomRelationRange(c);boolean found=count(c,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE room_id=? AND effective_relation_key=1",roomId)>0;c.rollback();return found;}});
            relation.get(10,TimeUnit.SECONDS); assertTrue(deletion.get(10,TimeUnit.SECONDS));
        }
        exec(admin,"DELETE FROM pm_room_customer_rel WHERE project_id=?",projectId);
        CountDownLatch deleted=new CountDownLatch(1),insertAttempted=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Future<Void> deletion=pool.submit(()->{try(Connection c=tx()){lockRoom(c);lockRoomRelationRange(c);exec(c,"UPDATE pm_room SET del_flag='1' WHERE id=?",roomId);deleted.countDown();await(insertAttempted);c.commit();return null;}});
            Future<Boolean> relation=pool.submit(()->{await(deleted);try(Connection c=tx()){insertAttempted.countDown();boolean active=lockRoom(c)!=null;c.rollback();return active;}});
            deletion.get(10,TimeUnit.SECONDS); assertFalse(relation.get(10,TimeUnit.SECONDS));
        }
        assertEquals(0,count(admin,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE project_id=?",projectId));
    }

    @Test void relationFirstMakesCustomerDeactivateRejectAndDeactivateFirstMakesRelationReject() throws Exception {
        CountDownLatch locked=new CountDownLatch(1),attempted=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Future<Void> relation=pool.submit(()->{try(Connection c=tx()){lockRoom(c);lockCustomer(c);insertRelation(c,id(),"PENDING");locked.countDown();await(attempted);c.commit();return null;}});
            Future<Boolean> stop=pool.submit(()->{await(locked);try(Connection c=tx()){attempted.countDown();lockCustomer(c);lockCustomerRelationRange(c);boolean found=count(c,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE customer_id=? AND effective_relation_key=1",customerId)>0;c.rollback();return found;}});
            relation.get(10,TimeUnit.SECONDS); assertTrue(stop.get(10,TimeUnit.SECONDS));
        }
        exec(admin,"DELETE FROM pm_room_customer_rel WHERE project_id=?",projectId);
        CountDownLatch stopped=new CountDownLatch(1),insertAttempted=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Future<Void> stop=pool.submit(()->{try(Connection c=tx()){lockCustomer(c);lockCustomerRelationRange(c);exec(c,"UPDATE pm_customer SET status='1',lifecycle_status='INACTIVE' WHERE id=?",customerId);stopped.countDown();await(insertAttempted);c.commit();return null;}});
            Future<Boolean> relation=pool.submit(()->{await(stopped);try(Connection c=tx()){lockRoom(c);insertAttempted.countDown();boolean active=lockActiveCustomer(c)!=null;c.rollback();return active;}});
            stop.get(10,TimeUnit.SECONDS); assertFalse(relation.get(10,TimeUnit.SECONDS));
        }
    }

    @Test void batchDeleteRollbackKeepsAllRows() throws SQLException {
        long pending=id(),active=id(); insertRelation(admin,pending,"PENDING");
        exec(admin,"UPDATE pm_room_customer_rel SET relation_status='ENDED' WHERE id=?",pending);
        insertRelation(admin,active,"ACTIVE");
        try(Connection c=tx()) {
            exec(c,"UPDATE pm_room_customer_rel SET del_flag='1' WHERE id=?",pending);
            if (count(c,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE id=? AND relation_status='ACTIVE'",active)>0) c.rollback(); else c.commit();
        }
        assertEquals(2,count(admin,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE project_id=? AND del_flag='0'",projectId));
    }

    private void seed() throws SQLException {
        exec(admin,"INSERT INTO pm_project(id,project_code,project_name,sort,status,del_flag) VALUES(?,?,?,0,'0','0')",projectId,prefix,prefix);
        exec(admin,"INSERT INTO pm_building(id,project_id,building_code,building_name,sort,status,del_flag) VALUES(?,?,?,?,0,'0','0')",buildingId,projectId,prefix,"楼栋");
        exec(admin,"INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,sort,status,del_flag) VALUES(?,?,?,?,?,0,'0','0')",unitId,projectId,buildingId,prefix,"单元");
        exec(admin,"INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,sort,status,del_flag) VALUES(?,?,?,?,?,'101',0,'0','0')",roomId,projectId,buildingId,unitId,prefix);
        exec(admin,"INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,status,lifecycle_status,version,del_flag) VALUES(?,?,'PERSON','关系测试客户','0','ACTIVE',0,'0')",customerId,prefix);
    }
    private void insertRelation(Connection c,long relationId,String status)throws SQLException{exec(c,"INSERT INTO pm_room_customer_rel(id,project_id,room_id,customer_id,relation_type,is_primary,start_date,relation_status,status,del_flag) VALUES(?,?,?,?,'OWNER','1',CURRENT_DATE,?,'0','0')",relationId,projectId,roomId,customerId,status);}
    private Connection tx()throws SQLException{Connection c=open();c.setAutoCommit(false);return c;}
    private Long lockRoom(Connection c)throws SQLException{return one(c,"SELECT id FROM pm_room WHERE id=? AND del_flag='0' AND status='0' FOR UPDATE",roomId);}
    private Long lockCustomer(Connection c)throws SQLException{return one(c,"SELECT id FROM pm_customer WHERE id=? AND del_flag='0' FOR UPDATE",customerId);}
    private Long lockActiveCustomer(Connection c)throws SQLException{return one(c,"SELECT id FROM pm_customer WHERE id=? AND del_flag='0' AND status='0' AND lifecycle_status='ACTIVE' FOR UPDATE",customerId);}
    private void lockRoomRelationRange(Connection c)throws SQLException{query(c,"SELECT id FROM pm_room_customer_rel FORCE INDEX(idx_pm_room_customer_room) WHERE room_id=? AND relation_status IN ('PENDING','ACTIVE') AND status='0' AND del_flag='0' FOR UPDATE",roomId);}
    private void lockCustomerRelationRange(Connection c)throws SQLException{query(c,"SELECT id FROM pm_room_customer_rel FORCE INDEX(idx_pm_room_customer_customer) WHERE customer_id=? AND relation_status IN ('PENDING','ACTIVE') AND status='0' AND del_flag='0' FOR UPDATE",customerId);}
    private Connection open()throws SQLException{return DriverManager.getConnection(required("ZBM_TEST_DB_URL"),required("ZBM_TEST_DB_USER"),required("ZBM_TEST_DB_PASSWORD"));}
    private static void await(CountDownLatch latch)throws InterruptedException{assertTrue(latch.await(5,TimeUnit.SECONDS));}
    private static void query(Connection c,String sql,long value)throws SQLException{try(var s=c.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){while(rs.next()){}}}}
    private static Long one(Connection c,String sql,long value)throws SQLException{try(var s=c.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){return rs.next()?rs.getLong(1):null;}}}
    private static int count(Connection c,String sql,long value)throws SQLException{try(var s=c.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){assertTrue(rs.next());return rs.getInt(1);}}}
    private static void exec(Connection c,String sql,Object... values)throws SQLException{try(var s=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);s.executeUpdate();}}
    private static String required(String name){String value=System.getenv(name);if(value==null||value.isBlank())throw new IllegalStateException(name+" is required");return value;}
    private static long id(){return Math.abs(UUID.randomUUID().getMostSignificantBits()>>>1);}
}
