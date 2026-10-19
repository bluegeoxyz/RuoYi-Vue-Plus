package org.dromara.property.customer.service.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Customer 删除与 TASK-013 关系新增锁协议的真实双连接验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class CustomerRelationConcurrencyMySqlTest {
    private Connection admin;
    private long projectId, buildingId, unitId, roomId, customerId;
    private String prefix;

    @BeforeEach void setUp() throws SQLException {
        admin = open();
        assertEquals("zbm_property_test", admin.getCatalog(), "只允许在隔离测试库执行");
        projectId=id(); buildingId=id(); unitId=id(); roomId=id(); customerId=id();
        prefix="C12L_"+UUID.randomUUID().toString().replace("-", "").substring(0,8);
        seed();
    }

    @AfterEach void clean() throws SQLException {
        if (admin == null) return;
        exec(admin,"DELETE FROM pm_room_customer_rel WHERE customer_id=?",customerId);
        exec(admin,"DELETE FROM pm_customer WHERE id=?",customerId);
        exec(admin,"DELETE FROM pm_room WHERE id=?",roomId);
        exec(admin,"DELETE FROM pm_unit WHERE id=?",unitId);
        exec(admin,"DELETE FROM pm_building WHERE id=?",buildingId);
        exec(admin,"DELETE FROM pm_project WHERE id=?",projectId);
        admin.close();
    }

    @Test void relationInsertFirstMakesFollowingCustomerDeleteReject() throws Exception {
        CountDownLatch locked=new CountDownLatch(1), attempted=new CountDownLatch(1);
        try (ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Future<Void> relation=pool.submit(()->{try(Connection c=tx()){
                assertNotNull(lockRoom(c)); assertNotNull(lockCustomer(c)); insertRelation(c);
                locked.countDown(); await(attempted); c.commit(); return null;
            }});
            Future<Boolean> deletion=pool.submit(()->{await(locked);try(Connection c=tx()){
                attempted.countDown(); assertNotNull(lockCustomer(c)); lockRelationRange(c);
                boolean history=count(c,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE customer_id=?",customerId)>0;
                c.rollback(); return history;
            }});
            relation.get(10,TimeUnit.SECONDS); assertTrue(deletion.get(10,TimeUnit.SECONDS));
        }
        assertEquals(1,count(admin,"SELECT COUNT(*) FROM pm_customer WHERE id=? AND del_flag='0'",customerId));
        assertEquals(1,count(admin,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE customer_id=?",customerId));
    }

    @Test void customerDeleteFirstMakesFollowingRelationInsertReject() throws Exception {
        CountDownLatch locked=new CountDownLatch(1), attempted=new CountDownLatch(1);
        try (ExecutorService pool=Executors.newFixedThreadPool(2)) {
            Future<Void> deletion=pool.submit(()->{try(Connection c=tx()){
                assertNotNull(lockCustomer(c)); lockRelationRange(c);
                assertEquals(0,count(c,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE customer_id=?",customerId));
                exec(c,"UPDATE pm_customer SET del_flag='1',version=version+1 WHERE id=?",customerId);
                locked.countDown(); await(attempted); c.commit(); return null;
            }});
            Future<Boolean> relation=pool.submit(()->{await(locked);try(Connection c=tx()){
                assertNotNull(lockRoom(c)); attempted.countDown(); boolean active=lockCustomer(c)!=null;
                c.rollback(); return active;
            }});
            deletion.get(10,TimeUnit.SECONDS); assertFalse(relation.get(10,TimeUnit.SECONDS));
        }
        assertEquals(0,count(admin,"SELECT COUNT(*) FROM pm_customer WHERE id=? AND del_flag='0'",customerId));
        assertEquals(0,count(admin,"SELECT COUNT(*) FROM pm_room_customer_rel WHERE customer_id=?",customerId));
    }

    private void seed() throws SQLException {
        exec(admin,"INSERT INTO pm_project(id,project_code,project_name,sort,status,del_flag) VALUES(?,?,?,0,'0','0')",projectId,prefix,prefix);
        exec(admin,"INSERT INTO pm_building(id,project_id,building_code,building_name,sort,status,del_flag) VALUES(?,?,?,?,0,'0','0')",buildingId,projectId,prefix,"楼栋");
        exec(admin,"INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,sort,status,del_flag) VALUES(?,?,?,?,?,0,'0','0')",unitId,projectId,buildingId,prefix,"单元");
        exec(admin,"INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,sort,status,del_flag) VALUES(?,?,?,?,?,'101',0,'0','0')",roomId,projectId,buildingId,unitId,prefix);
        exec(admin,"INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,status,lifecycle_status,version,del_flag) VALUES(?,?,'PERSON','锁协议客户','0','ACTIVE',0,'0')",customerId,prefix);
    }
    private void insertRelation(Connection c)throws SQLException{exec(c,"INSERT INTO pm_room_customer_rel(id,project_id,room_id,customer_id,relation_type,is_primary,start_date,relation_status,status,del_flag) VALUES(?,?,?,?,'OWNER','1',CURRENT_DATE,'ACTIVE','0','0')",id(),projectId,roomId,customerId);}
    private Connection tx()throws SQLException{Connection c=open();c.setAutoCommit(false);return c;}
    private Long lockRoom(Connection c)throws SQLException{return one(c,"SELECT id FROM pm_room WHERE id=? AND del_flag='0' FOR UPDATE",roomId);}
    private Long lockCustomer(Connection c)throws SQLException{return one(c,"SELECT id FROM pm_customer WHERE id=? AND del_flag='0' FOR UPDATE",customerId);}
    private void lockRelationRange(Connection c)throws SQLException{try(var s=c.prepareStatement("SELECT id FROM pm_room_customer_rel FORCE INDEX(idx_pm_room_customer_customer) WHERE customer_id=? FOR UPDATE")){s.setLong(1,customerId);try(var rs=s.executeQuery()){while(rs.next()){}}}}
    private Connection open()throws SQLException{return DriverManager.getConnection(required("ZBM_TEST_DB_URL"),required("ZBM_TEST_DB_USER"),required("ZBM_TEST_DB_PASSWORD"));}
    private static void await(CountDownLatch latch)throws InterruptedException{assertTrue(latch.await(5,TimeUnit.SECONDS));}
    private static Long one(Connection c,String sql,long value)throws SQLException{try(var s=c.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){return rs.next()?rs.getLong(1):null;}}}
    private static int count(Connection c,String sql,long value)throws SQLException{try(var s=c.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){assertTrue(rs.next());return rs.getInt(1);}}}
    private static void exec(Connection c,String sql,Object... values)throws SQLException{try(var s=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);s.executeUpdate();}}
    private static String required(String name){String v=System.getenv(name);if(v==null||v.isBlank())throw new IllegalStateException(name+" is required");return v;}
    private static long id(){return Math.abs(UUID.randomUUID().getMostSignificantBits()>>>1);}
}
