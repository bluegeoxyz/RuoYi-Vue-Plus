package org.dromara.property.resource.room.mysql;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** 使用真实事务验证客户关联删除保护与批量删除回滚。 */
@EnabledIfEnvironmentVariable(named = "ZBM_TEST_DB_URL", matches = ".+")
class RoomDeletionTransactionMySqlTest {
    private Connection admin; private long project,building,unit,customer,roomA,roomB; private String prefix;
    @BeforeEach void setUp() throws SQLException {admin=open();assertEquals("zbm_property_test",admin.getCatalog());project=id();building=id();unit=id();customer=id();roomA=id();roomB=id();prefix="D11_"+UUID.randomUUID().toString().replace("-","").substring(0,8);seed();}
    @AfterEach void clean() throws SQLException {if(admin==null)return;exec(admin,"DELETE FROM pm_room_customer_rel WHERE project_id=?",project);exec(admin,"DELETE FROM pm_customer WHERE id=?",customer);exec(admin,"DELETE FROM pm_room WHERE project_id=?",project);exec(admin,"DELETE FROM pm_unit WHERE id=?",unit);exec(admin,"DELETE FROM pm_building WHERE id=?",building);exec(admin,"DELETE FROM pm_project WHERE id=?",project);admin.close();}

    @Test void activeCustomerRelationRejectsRoomDeletion() throws SQLException {
        addRelation(roomA);
        assertThrows(SQLException.class,()->deleteLikeService(roomA));
        assertEquals(1,count("SELECT COUNT(*) FROM pm_room WHERE id=? AND del_flag='0'",roomA));
    }

    @Test void protectedRoomMakesWholeBatchRollback() throws SQLException {
        addRelation(roomB);
        assertThrows(SQLException.class,()->deleteLikeService(roomA,roomB));
        assertEquals(1,count("SELECT COUNT(*) FROM pm_room WHERE id=? AND del_flag='0'",roomA));
        assertEquals(1,count("SELECT COUNT(*) FROM pm_room WHERE id=? AND del_flag='0'",roomB));
    }

    private void deleteLikeService(long... ids) throws SQLException {
        try(Connection c=open()){c.setAutoCommit(false);try{
            for(long room:ids){try(var lock=c.prepareStatement("SELECT id FROM pm_room WHERE id=? AND del_flag='0' FOR UPDATE")){lock.setLong(1,room);try(var rs=lock.executeQuery()){if(!rs.next())throw new SQLException("房屋不存在");}}
                try(var relation=c.prepareStatement("SELECT EXISTS(SELECT 1 FROM pm_room_customer_rel WHERE room_id=? AND relation_status='ACTIVE' AND status='0' AND del_flag='0')")){relation.setLong(1,room);try(var rs=relation.executeQuery()){rs.next();if(rs.getBoolean(1))throw new SQLException("房屋存在有效客户关系");}}}
            for(long room:ids)exec(c,"UPDATE pm_room SET del_flag='1' WHERE id=?",room);c.commit();
        }catch(SQLException e){c.rollback();throw e;}}
    }
    private void seed()throws SQLException{exec(admin,"INSERT INTO pm_project(id,project_code,project_name,sort,status,del_flag) VALUES(?,?,?,0,'0','0')",project,prefix,prefix);exec(admin,"INSERT INTO pm_building(id,project_id,building_code,building_name,sort,status,del_flag) VALUES(?,?,?,?,0,'0','0')",building,project,prefix,"楼栋");exec(admin,"INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,sort,status,del_flag) VALUES(?,?,?,?,?,0,'0','0')",unit,project,building,prefix,"单元");exec(admin,"INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,status,del_flag) VALUES(? ,?,'PERSON','测试客户','0','0')",customer,prefix);insertRoom(roomA,"A");insertRoom(roomB,"B");}
    private void insertRoom(long room,String code)throws SQLException{exec(admin,"INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,sort,status,del_flag) VALUES(?,?,?,?,?,?,0,'0','0')",room,project,building,unit,prefix+code,code);}
    private void addRelation(long room)throws SQLException{exec(admin,"INSERT INTO pm_room_customer_rel(id,project_id,room_id,customer_id,relation_type,is_primary,start_date,relation_status,status,del_flag) VALUES(?,?,?,?, 'OWNER','1',?,'ACTIVE','0','0')",id(),project,room,customer,Date.valueOf(LocalDate.now()));}
    private int count(String sql,long value)throws SQLException{try(var s=admin.prepareStatement(sql)){s.setLong(1,value);try(var rs=s.executeQuery()){rs.next();return rs.getInt(1);}}}
    private Connection open()throws SQLException{return DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),System.getenv("ZBM_TEST_DB_USER"),System.getenv("ZBM_TEST_DB_PASSWORD"));}
    private static void exec(Connection c,String sql,Object... values)throws SQLException{try(var s=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);s.executeUpdate();}}
    private static long id(){return Math.abs(UUID.randomUUID().getMostSignificantBits()>>>1);}
}
