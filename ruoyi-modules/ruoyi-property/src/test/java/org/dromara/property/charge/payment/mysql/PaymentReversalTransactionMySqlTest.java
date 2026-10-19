package org.dromara.property.charge.payment.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** TASK-022B 整笔收款冲销、事务回滚和并发锁的真实 MySQL 验收。 */
@EnabledIfEnvironmentVariable(named="ZBM_TEST_DB_URL",matches=".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentReversalTransactionMySqlTest {
    private static Connection admin;
    private static long projectId;
    private static String prefix;

    @BeforeAll static void setUp() throws Exception {
        admin=open();assertEquals("zbm_property_test",admin.getCatalog(),"只允许在隔离测试库执行");
        assertTrue(tableExists("pm_payment_reversal"),"请先执行 V2.0.22 migration");
        projectId=id();prefix="T22B"+Long.toUnsignedString(projectId);
    }

    @AfterAll static void cleanUp() throws Exception {
        if(admin==null)return;delete("pm_payment_reversal");delete("pm_payment_allocation");delete("pm_payment");delete("pm_receivable");admin.close();
    }

    @Test @Order(1)
    void wholePaymentReversalRestoresMultipleReceivablesAndKeepsHistory() throws Exception {
        long r1=insertReceivable("R1","2400.00","2400.00","PAID"),r2=insertReceivable("R2","2400.00","600.00","PARTIAL");
        long p=insertPayment("P1","3000.00","3000.00");insertAllocation(p,r1,"2400.00");insertAllocation(p,r2,"600.00");
        assertTrue(reverse(p,null,null));
        assertEquals("0.00",decimal("SELECT paid_amount FROM pm_receivable WHERE id="+r1));
        assertEquals("UNPAID",text("SELECT payment_status FROM pm_receivable WHERE id="+r1));
        assertEquals("0.00",decimal("SELECT paid_amount FROM pm_receivable WHERE id="+r2));
        assertEquals("UNPAID",text("SELECT payment_status FROM pm_receivable WHERE id="+r2));
        assertEquals("REVERSED",text("SELECT status FROM pm_payment WHERE id="+p));
        assertEquals(2,scalar("SELECT COUNT(*) FROM pm_payment_allocation WHERE payment_id="+p));
        assertEquals(1,scalar("SELECT COUNT(*) FROM pm_payment_reversal WHERE payment_id="+p));
    }

    @Test @Order(2)
    void reversingOneOfMultiplePaymentsOnlySubtractsItsAllocation() throws Exception {
        long r=insertReceivable("R3","2400.00","2400.00","PAID");
        long p1=insertPayment("P2","600.00","600.00"),p2=insertPayment("P3","1800.00","1800.00");
        insertAllocation(p1,r,"600.00");insertAllocation(p2,r,"1800.00");
        assertTrue(reverse(p1,null,null));
        assertEquals("1800.00",decimal("SELECT paid_amount FROM pm_receivable WHERE id="+r));
        assertEquals("PARTIAL",text("SELECT payment_status FROM pm_receivable WHERE id="+r));
        assertEquals("VALID",text("SELECT status FROM pm_payment WHERE id="+p2));
        assertEquals("1800.00",decimal(validAllocationSum(r)));
    }

    @Test @Order(3)
    void dirtySecondReceivableRollsBackWholeReversal() throws Exception {
        long r1=insertReceivable("R4","100.00","100.00","PAID"),r2=insertReceivable("R5","100.00","50.00","PARTIAL");
        long p=insertPayment("P4","200.00","200.00");insertAllocation(p,r1,"100.00");insertAllocation(p,r2,"100.00");
        assertThrows(SQLException.class,()->reverse(p,null,null));
        assertEquals("100.00",decimal("SELECT paid_amount FROM pm_receivable WHERE id="+r1));
        assertEquals("50.00",decimal("SELECT paid_amount FROM pm_receivable WHERE id="+r2));
        assertEquals("VALID",text("SELECT status FROM pm_payment WHERE id="+p));
        assertEquals(0,scalar("SELECT COUNT(*) FROM pm_payment_reversal WHERE payment_id="+p));
    }

    @Test @Order(4)
    void concurrentReversalOnlySucceedsOnce() throws Exception {
        long r=insertReceivable("R6","100.00","100.00","PAID"),p=insertPayment("P5","100.00","100.00");insertAllocation(p,r,"100.00");
        CountDownLatch locked=new CountDownLatch(1),release=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)){
            Future<Boolean> first=pool.submit(()->reverse(p,locked,release));assertTrue(locked.await(5,TimeUnit.SECONDS));
            Future<Boolean> second=pool.submit(()->reverse(p,null,null));release.countDown();
            assertTrue(first.get(10,TimeUnit.SECONDS));assertFalse(second.get(10,TimeUnit.SECONDS));
        }
        assertEquals(1,scalar("SELECT COUNT(*) FROM pm_payment_reversal WHERE payment_id="+p));
        assertEquals("0.00",decimal("SELECT paid_amount FROM pm_receivable WHERE id="+r));
    }

    @Test @Order(5)
    void reversalAndNewCollectionSerializeOnReceivable() throws Exception {
        long r=insertReceivable("R7","200.00","100.00","PARTIAL"),oldPayment=insertPayment("P6","100.00","100.00");insertAllocation(oldPayment,r,"100.00");
        CountDownLatch locked=new CountDownLatch(1),release=new CountDownLatch(1);
        try(ExecutorService pool=Executors.newFixedThreadPool(2)){
            Future<Boolean> reversing=pool.submit(()->reverse(oldPayment,locked,release));assertTrue(locked.await(5,TimeUnit.SECONDS));
            Future<Long> collecting=pool.submit(()->collect(r,"50.00"));release.countDown();
            assertTrue(reversing.get(10,TimeUnit.SECONDS));long newPayment=collecting.get(10,TimeUnit.SECONDS);
            assertEquals("VALID",text("SELECT status FROM pm_payment WHERE id="+newPayment));
        }
        assertEquals("50.00",decimal("SELECT paid_amount FROM pm_receivable WHERE id="+r));
        assertEquals("50.00",decimal(validAllocationSum(r)));
        assertEquals("PARTIAL",text("SELECT payment_status FROM pm_receivable WHERE id="+r));
    }

    private static boolean reverse(long paymentId,CountDownLatch locked,CountDownLatch release)throws Exception{
        try(Connection c=open()){c.setAutoCommit(false);try{
            String status;BigDecimal amount,allocated;long roomId;
            try(PreparedStatement s=c.prepareStatement("SELECT status,payment_amount,allocated_amount,room_id FROM pm_payment WHERE id=? AND del_flag='0' FOR UPDATE")){s.setLong(1,paymentId);try(ResultSet rs=s.executeQuery()){if(!rs.next()){c.rollback();return false;}status=rs.getString(1);amount=rs.getBigDecimal(2);allocated=rs.getBigDecimal(3);roomId=rs.getLong(4);}}
            if(locked!=null)locked.countDown();if(release!=null)release.await(5,TimeUnit.SECONDS);
            if(!"VALID".equals(status)){c.rollback();return false;}if(amount.compareTo(allocated)!=0)throw new SQLException("not fully allocated");
            List<Allocation> allocations=new ArrayList<>();try(PreparedStatement s=c.prepareStatement("SELECT receivable_id,allocation_amount FROM pm_payment_allocation WHERE payment_id=? AND del_flag='0' ORDER BY receivable_id,id FOR UPDATE")){s.setLong(1,paymentId);try(ResultSet rs=s.executeQuery()){while(rs.next())allocations.add(new Allocation(rs.getLong(1),rs.getBigDecimal(2)));}}
            if(allocations.isEmpty()||allocations.stream().map(Allocation::amount).reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(amount)!=0)throw new SQLException("allocation mismatch");
            List<Long> ids=allocations.stream().map(Allocation::receivableId).distinct().sorted().toList();Map<Long,ReceivableState> states=new HashMap<>();String marks=String.join(",",Collections.nCopies(ids.size(),"?"));
            try(PreparedStatement s=c.prepareStatement("SELECT id,project_id,room_id,receivable_amount,paid_amount,payment_status FROM pm_receivable WHERE id IN ("+marks+") AND del_flag='0' ORDER BY id FOR UPDATE")){for(int i=0;i<ids.size();i++)s.setLong(i+1,ids.get(i));try(ResultSet rs=s.executeQuery()){while(rs.next())states.put(rs.getLong(1),new ReceivableState(rs.getLong(2),rs.getLong(3),rs.getBigDecimal(4),rs.getBigDecimal(5),rs.getString(6)));}}
            for(Allocation a:allocations){ReceivableState state=states.get(a.receivableId());if(state==null||state.projectId()!=projectId||state.roomId()!=roomId||"CANCELLED".equals(state.status()))throw new SQLException("ownership mismatch");BigDecimal paid=state.paid().subtract(a.amount());if(paid.signum()<0)throw new SQLException("negative paid");String next=paid.signum()==0?"UNPAID":paid.compareTo(state.amount())==0?"PAID":"PARTIAL";try(PreparedStatement s=c.prepareStatement("UPDATE pm_receivable SET paid_amount=?,payment_status=? WHERE id=?")){s.setBigDecimal(1,paid);s.setString(2,next);s.setLong(3,a.receivableId());s.executeUpdate();}}
            try(PreparedStatement s=c.prepareStatement("UPDATE pm_payment SET status='REVERSED' WHERE id=? AND status='VALID'")){s.setLong(1,paymentId);if(s.executeUpdate()!=1)throw new SQLException("payment changed");}
            try(PreparedStatement s=c.prepareStatement("INSERT INTO pm_payment_reversal(id,project_id,reversal_no,payment_id,reversal_amount,reversal_time,reason) VALUES(?,?,?,?,?,NOW(),'测试冲销')")){long reversal=id();s.setLong(1,reversal);s.setLong(2,projectId);s.setString(3,prefix+"REV"+reversal);s.setLong(4,paymentId);s.setBigDecimal(5,amount);s.executeUpdate();}
            c.commit();return true;
        }catch(Exception e){c.rollback();throw e;}}
    }

    private static long collect(long receivableId,String amount)throws Exception{
        try(Connection c=open()){c.setAutoCommit(false);try{
            BigDecimal current,total;try(PreparedStatement s=c.prepareStatement("SELECT paid_amount,receivable_amount FROM pm_receivable WHERE id=? FOR UPDATE")){s.setLong(1,receivableId);try(ResultSet rs=s.executeQuery()){rs.next();current=rs.getBigDecimal(1);total=rs.getBigDecimal(2);}}
            BigDecimal value=new BigDecimal(amount),next=current.add(value);if(next.compareTo(total)>0)throw new SQLException("over allocation");long payment=insertPayment(c,"PC"+id(),amount,amount);insertAllocation(c,payment,receivableId,amount);try(PreparedStatement s=c.prepareStatement("UPDATE pm_receivable SET paid_amount=?,payment_status=? WHERE id=?")){s.setBigDecimal(1,next);s.setString(2,next.compareTo(total)==0?"PAID":"PARTIAL");s.setLong(3,receivableId);s.executeUpdate();}c.commit();return payment;
        }catch(Exception e){c.rollback();throw e;}}
    }

    private static long insertReceivable(String suffix,String amount,String paid,String status)throws SQLException{long rid=id();String sql="INSERT INTO pm_receivable(id,project_id,receivable_no,batch_id,binding_id,room_id,charge_item_id,charge_rule_id,generate_month,period_start_date,period_end_date,theoretical_start_date,theoretical_end_date,due_date,room_full_name_snapshot,charge_item_code_snapshot,charge_item_name_snapshot,charge_rule_code_snapshot,charge_rule_name_snapshot,billing_method_snapshot,billing_cycle_snapshot,price_cycle_snapshot,cycle_mode_snapshot,due_date_mode_snapshot,due_date_offset_days_snapshot,area_snapshot,unit_price_snapshot,theoretical_days,actual_days,receivable_amount,paid_amount,payment_status) VALUES(?,?,?,1,?,101,11,12,?,?,?,?,?,?,'1号楼/1单元/101','PROPERTY_FEE','物业费','RULE','规则','AREA','MONTH','MONTH','NATURAL','AFTER_PERIOD_START',0,100,2,31,31,?,?,?)";try(PreparedStatement s=admin.prepareStatement(sql)){int i=1;s.setLong(i++,rid);s.setLong(i++,projectId);s.setString(i++,prefix+suffix);s.setLong(i++,id());LocalDate start=LocalDate.of(2026,1,1),end=LocalDate.of(2026,1,31);s.setDate(i++,java.sql.Date.valueOf(start));s.setDate(i++,java.sql.Date.valueOf(start));s.setDate(i++,java.sql.Date.valueOf(end));s.setDate(i++,java.sql.Date.valueOf(start));s.setDate(i++,java.sql.Date.valueOf(end));s.setDate(i++,java.sql.Date.valueOf(end));s.setBigDecimal(i++,new BigDecimal(amount));s.setBigDecimal(i++,new BigDecimal(paid));s.setString(i,status);s.executeUpdate();}return rid;}
    private static long insertPayment(String suffix,String amount,String allocated)throws SQLException{return insertPayment(admin,suffix,amount,allocated);}
    private static long insertPayment(Connection c,String suffix,String amount,String allocated)throws SQLException{long pid=id();try(PreparedStatement s=c.prepareStatement("INSERT INTO pm_payment(id,project_id,payment_no,room_id,room_full_name_snapshot,payer_name_snapshot,payment_amount,allocated_amount,payment_method,payment_time,status) VALUES(?,?,?,101,'1号楼/1单元/101','测试付款人',?,?,'CASH',NOW(),'VALID')")){s.setLong(1,pid);s.setLong(2,projectId);s.setString(3,prefix+suffix);s.setBigDecimal(4,new BigDecimal(amount));s.setBigDecimal(5,new BigDecimal(allocated));s.executeUpdate();}return pid;}
    private static void insertAllocation(long payment,long receivable,String amount)throws SQLException{insertAllocation(admin,payment,receivable,amount);}
    private static void insertAllocation(Connection c,long payment,long receivable,String amount)throws SQLException{try(PreparedStatement s=c.prepareStatement("INSERT INTO pm_payment_allocation(id,project_id,payment_id,receivable_id,allocation_amount,allocation_time) VALUES(?,?,?,?,?,NOW())")){s.setLong(1,id());s.setLong(2,projectId);s.setLong(3,payment);s.setLong(4,receivable);s.setBigDecimal(5,new BigDecimal(amount));s.executeUpdate();}}
    private static String validAllocationSum(long receivable){return "SELECT COALESCE(SUM(a.allocation_amount),0) FROM pm_payment_allocation a JOIN pm_payment p ON p.id=a.payment_id WHERE a.receivable_id="+receivable+" AND a.del_flag='0' AND p.del_flag='0' AND p.status='VALID'";}
    private static void delete(String table)throws SQLException{try(PreparedStatement s=admin.prepareStatement("DELETE FROM "+table+" WHERE project_id=?")){s.setLong(1,projectId);s.executeUpdate();}}
    private static int scalar(String sql)throws SQLException{try(Statement s=admin.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    private static String text(String sql)throws SQLException{try(Statement s=admin.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getString(1);}}
    private static String decimal(String sql)throws SQLException{try(Statement s=admin.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getBigDecimal(1).setScale(2).toPlainString();}}
    private static boolean tableExists(String table)throws SQLException{try(PreparedStatement s=admin.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")){s.setString(1,table);try(ResultSet r=s.executeQuery()){r.next();return r.getInt(1)==1;}}}
    private static Connection open()throws SQLException{return DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),System.getenv("ZBM_TEST_DB_USER"),System.getenv("ZBM_TEST_DB_PASSWORD"));}
    private static long id(){return Math.abs(UUID.randomUUID().getMostSignificantBits()>>>1);}
    private record Allocation(long receivableId,BigDecimal amount){}
    private record ReceivableState(long projectId,long roomId,BigDecimal amount,BigDecimal paid,String status){}
}
