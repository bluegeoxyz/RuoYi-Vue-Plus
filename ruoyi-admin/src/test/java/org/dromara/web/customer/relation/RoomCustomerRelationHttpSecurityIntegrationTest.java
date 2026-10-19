package org.dromara.web.customer.relation;

import org.dromara.DromaraApplication;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.property.customer.relation.domain.vo.RoomCustomerRelationVo;
import org.dromara.property.customer.relation.mapper.RoomCustomerRelationMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** 真实 HTTP、Sa-Token、ScopeService 与 MySQL 的房屋客户关系安全验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_HTTP_TEST_ENABLED", matches = "true")
@ResourceLock("zbm-http-integration-db")
@SpringBootTest(classes = DromaraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.flyway.enabled=false",
        "captcha.enable=false", "api-decrypt.enabled=false",
        "spring.datasource.dynamic.datasource.master.url=${ZBM_TEST_DB_URL}",
        "spring.datasource.dynamic.datasource.master.username=${ZBM_TEST_DB_USER}",
        "spring.datasource.dynamic.datasource.master.password=${ZBM_TEST_DB_PASSWORD}",
        "mybatis-encryptor.enable=true", "mybatis-encryptor.algorithm=AES",
        "mybatis-encryptor.password=${ZBM_TEST_AES_KEY}",
        "property.customer.security.enabled=true",
        "property.customer.security.hmac-key-base64=${ZBM_TEST_HMAC_KEY_BASE64}",
        "property.customer.security.key-version=http-test-v1"
    })
class RoomCustomerRelationHttpSecurityIntegrationTest {
    private static final long BASE = 913013000000000000L;
    private static final long PROJECT_A=BASE+1, PROJECT_B=BASE+2, ROOM_A=BASE+31, ROOM_B=BASE+32;
    private static final long CUSTOMER_A=BASE+41, CUSTOMER_B=BASE+42, CUSTOMER_NEW=BASE+43;
    private static final long REL_A=BASE+51, REL_B=BASE+52;
    private static final String PASSWORD_HASH="$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";
    @LocalServerPort private int port;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private RoomCustomerRelationMapper relationMapper;
    private final HttpClient http=HttpClient.newHttpClient();
    private final Map<String,String> tokens=new HashMap<>();

    @BeforeEach void prepare(){assertEquals("zbm_property_test",jdbc.queryForObject("SELECT DATABASE()",String.class));cleanup();seedResources();seedIdentity("a",10,PROJECT_A);seedIdentity("b",20,PROJECT_B);seedIdentity("none",30,null);seedIdentity("management",40,null);seedIdentity("customergrant",50,null);jdbc.update("INSERT INTO pm_project_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,?,?,'ALL','0',0,'0')",BASE+901,"USER",userId(40));jdbc.update("INSERT INTO pm_customer_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,?,?,'ENTERPRISE','0',0,'0')",BASE+902,"USER",userId(50));}
    @AfterEach void cleanup(){
        jdbc.execute("DROP TRIGGER IF EXISTS trg_task015_room_update_fail");
        jdbc.update("DELETE FROM pm_room_customer_rel WHERE project_id IN (?,?)",PROJECT_A,PROJECT_B);
        jdbc.update("DELETE FROM pm_customer WHERE id BETWEEN ? AND ?",CUSTOMER_A,CUSTOMER_NEW);
        jdbc.update("DELETE FROM pm_room WHERE id IN (?,?)",ROOM_A,ROOM_B);jdbc.update("DELETE FROM pm_unit WHERE id BETWEEN ? AND ?",BASE+21,BASE+22);jdbc.update("DELETE FROM pm_building WHERE id BETWEEN ? AND ?",BASE+11,BASE+12);
        jdbc.update("DELETE FROM pm_project_member WHERE project_id IN (?,?)",PROJECT_A,PROJECT_B);jdbc.update("DELETE FROM pm_project WHERE id IN (?,?)",PROJECT_A,PROJECT_B);
        jdbc.update("DELETE FROM pm_customer_management_grant WHERE principal_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM pm_project_management_grant WHERE principal_id BETWEEN ? AND ?",BASE+100,BASE+199);
        jdbc.update("DELETE FROM hr_employee WHERE id BETWEEN ? AND ?",BASE+200,BASE+299);jdbc.update("DELETE FROM sys_role_menu WHERE role_id BETWEEN ? AND ?",BASE+300,BASE+399);jdbc.update("DELETE FROM sys_user_role WHERE user_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM sys_role WHERE role_id BETWEEN ? AND ?",BASE+300,BASE+399);jdbc.update("DELETE FROM sys_user WHERE user_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM sys_login_info WHERE user_name LIKE 'r13c\\_%'");tokens.clear();
    }

    @Test void listDetailOptionsAndDisplayAreProjectIsolatedAndMasked(){
        RoomCustomerRelationVo mapperDetail=relationMapper.selectDisplayById(REL_A);assertNotNull(mapperDetail);assertEquals("张*",mapperDetail.getCustomerName());assertEquals("138****8000",mapperDetail.getMobile());
        String a=get("a","/property/customer-relation/list?pageNum=1&pageSize=10").body();assertSuccess(a);assertTrue(a.contains(String.valueOf(REL_A)));assertFalse(a.contains(String.valueOf(REL_B)));assertTrue(a.contains("A栋"));assertTrue(a.contains("张*"));assertTrue(a.contains("138****8000"));assertFalse(a.contains("13800138000"));
        String detail=get("a","/property/customer-relation/"+REL_A).body();assertSuccess(detail);assertTrue(detail.contains("张*"));assertTrue(detail.contains("138****8000"));assertFalse(detail.contains("13800138000"));assertFalse(detail.contains("idNo"));assertFalse(detail.contains("identityFingerprint"));assertFalse(detail.contains("piiKeyVersion"));assertFalse(detail.contains("customerGrant"));assertFalse(detail.contains("audit"));
        assertDenied(get("a","/property/customer-relation/"+REL_B));assertEmptyScope(get("management","/property/customer-relation/list?pageNum=1&pageSize=10"));assertEmptyScope(get("customergrant","/property/customer-relation/list?pageNum=1&pageSize=10"));assertEmptyScope(get("none","/property/customer-relation/list?pageNum=1&pageSize=10"));
        String options=get("a","/property/customer-relation/project-options").body();assertSuccess(options);assertTrue(options.contains("HTTP-R13-A"));assertFalse(options.contains("HTTP-R13-B"));
        assertEmptyScope(get("management","/property/customer-relation/project-options"));
        String candidates=get("a","/property/customer-relation/customer-options?projectId="+PROJECT_A+"&keyword=HTTP-CUST").body();assertSuccess(candidates);assertFalse(candidates.contains("13800138000"));assertFalse(candidates.contains("identityFingerprint"));
    }

    @Test void addEditLifecycleAndDeleteEnforcePersistedOwnership(){
        assertEquals("VACANT",occupancy(ROOM_A));
        assertDenied(json("a","POST","/property/customer-relation",Map.of("projectId",PROJECT_B,"roomId",ROOM_A,"customerId",CUSTOMER_NEW,"relationType","TENANT","startDate","2026-10-01")));
        jdbc.update("UPDATE pm_customer SET lifecycle_status='INACTIVE',status='1' WHERE id=?",CUSTOMER_NEW);
        assertDenied(json("a","POST","/property/customer-relation",Map.of("projectId",PROJECT_A,"roomId",ROOM_A,"customerId",CUSTOMER_NEW,"relationType","TENANT","startDate","2026-10-01")));
        jdbc.update("UPDATE pm_customer SET lifecycle_status='ACTIVE',status='0' WHERE id=?",CUSTOMER_NEW);
        assertSuccess(json("a","POST","/property/customer-relation",Map.of("projectId",PROJECT_A,"roomId",ROOM_A,"customerId",CUSTOMER_NEW,"relationType","TENANT","startDate","2026-10-01")).body());
        long created=jdbc.queryForObject("SELECT id FROM pm_room_customer_rel WHERE room_id=? AND customer_id=? AND del_flag='0'",Long.class,ROOM_A,CUSTOMER_NEW);
        assertDenied(json("a","PUT","/property/customer-relation",Map.of("id",created,"roomId",ROOM_B,"isPrimary","1","remark","forged")));
        assertSuccess(json("a","PUT","/property/customer-relation",Map.of("id",created,"isPrimary","1","remark","allowed")).body());
        assertSuccess(json("a","PUT","/property/customer-relation/move-in",Map.of("id",created,"moveInDate","2026-10-02")).body());assertEquals("OCCUPIED",occupancy(ROOM_A));assertDenied(json("a","PUT","/property/customer-relation/move-in",Map.of("id",created,"moveInDate","2026-10-02")));
        assertDenied(json("a","PUT","/property/customer-relation/move-in",Map.of("id",created,"moveInDate","2026-10-03")));
        assertSuccess(json("a","PUT","/property/customer-relation/move-out",Map.of("id",created,"moveOutDate","2026-10-04")).body());assertEquals("VACANT",occupancy(ROOM_A));assertDenied(json("a","PUT","/property/customer-relation/move-out",Map.of("id",created,"moveOutDate","2026-10-04")));
        assertDenied(delete("a","/property/customer-relation/"+created));
        assertDenied(delete("a","/property/customer-relation/"+REL_A+","+REL_B));assertEquals("0",jdbc.queryForObject("SELECT del_flag FROM pm_room_customer_rel WHERE id=?",String.class,REL_A));
        assertSuccess(delete("a","/property/customer-relation/"+REL_A).body());
    }

    @Test void multipleActiveRelationsKeepRoomOccupiedUntilLastMoveOut(){
        assertSuccess(json("a","PUT","/property/customer-relation/move-in",Map.of("id",REL_A,"moveInDate","2026-10-01")).body());
        assertSuccess(json("a","POST","/property/customer-relation",Map.of("projectId",PROJECT_A,"roomId",ROOM_A,"customerId",CUSTOMER_NEW,"relationType","TENANT","startDate","2026-10-01")).body());
        long second=jdbc.queryForObject("SELECT id FROM pm_room_customer_rel WHERE room_id=? AND customer_id=? AND del_flag='0'",Long.class,ROOM_A,CUSTOMER_NEW);
        assertSuccess(json("a","PUT","/property/customer-relation/move-in",Map.of("id",second,"moveInDate","2026-10-02")).body());
        assertEquals("OCCUPIED",occupancy(ROOM_A));
        assertSuccess(json("a","PUT","/property/customer-relation/move-out",Map.of("id",REL_A,"moveOutDate","2026-10-03")).body());
        assertEquals("OCCUPIED",occupancy(ROOM_A));
        assertSuccess(json("a","PUT","/property/customer-relation/move-out",Map.of("id",second,"moveOutDate","2026-10-04")).body());
        assertEquals("VACANT",occupancy(ROOM_A));
    }

    @Test void concurrentMoveOutsSerializeAndLeaveRoomVacant() throws Exception {
        assertSuccess(json("a","PUT","/property/customer-relation/move-in",Map.of("id",REL_A,"moveInDate","2026-10-01")).body());
        assertSuccess(json("a","POST","/property/customer-relation",Map.of("projectId",PROJECT_A,"roomId",ROOM_A,"customerId",CUSTOMER_NEW,"relationType","TENANT","startDate","2026-10-01")).body());
        long second=jdbc.queryForObject("SELECT id FROM pm_room_customer_rel WHERE room_id=? AND customer_id=? AND del_flag='0'",Long.class,ROOM_A,CUSTOMER_NEW);
        assertSuccess(json("a","PUT","/property/customer-relation/move-in",Map.of("id",second,"moveInDate","2026-10-02")).body());
        try (ExecutorService executor=Executors.newFixedThreadPool(2)) {
            Future<HttpResponse<String>> first=executor.submit(() -> json("a","PUT","/property/customer-relation/move-out",Map.of("id",REL_A,"moveOutDate","2026-10-03")));
            Future<HttpResponse<String>> last=executor.submit(() -> json("a","PUT","/property/customer-relation/move-out",Map.of("id",second,"moveOutDate","2026-10-04")));
            assertSuccess(first.get(20,TimeUnit.SECONDS));
            assertSuccess(last.get(20,TimeUnit.SECONDS));
        }
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM pm_room_customer_rel WHERE room_id=? AND relation_status='ACTIVE' AND status='0' AND del_flag='0'",Integer.class,ROOM_A));
        assertEquals("VACANT",occupancy(ROOM_A));
    }

    @Test void roomUpdateFailureRollsBackRelationTransition(){
        jdbc.execute("CREATE TRIGGER trg_task015_room_update_fail BEFORE UPDATE ON pm_room FOR EACH ROW BEGIN IF NEW.id="+ROOM_A+" THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='TASK-015 forced room failure'; END IF; END");
        try {
            assertDenied(json("a","PUT","/property/customer-relation/move-in",Map.of("id",REL_A,"moveInDate","2026-10-01")));
        } finally {
            jdbc.execute("DROP TRIGGER IF EXISTS trg_task015_room_update_fail");
        }
        assertEquals("PENDING",jdbc.queryForObject("SELECT relation_status FROM pm_room_customer_rel WHERE id=?",String.class,REL_A));
        assertEquals("VACANT",occupancy(ROOM_A));
    }

    @Test void membershipRevocationAndRoleDisableInvalidateOriginalToken(){
        String token=token("a");assertSuccess(getWithToken(token,"/property/customer-relation/list?pageNum=1&pageSize=10").body());
        jdbc.update("UPDATE pm_project_member SET del_flag='1' WHERE project_id=? AND employee_id=?",PROJECT_A,employeeId(10));
        String revoked=getWithToken(token,"/property/customer-relation/list?pageNum=1&pageSize=10").body();assertSuccess(revoked);assertFalse(revoked.contains(String.valueOf(REL_A)));
        jdbc.update("UPDATE pm_project_member SET del_flag='0' WHERE project_id=? AND employee_id=?",PROJECT_A,employeeId(10));
        assertSuccess(json("admin","PUT","/system/role/authUser/cancel",Map.of("roleId",roleId(10),"userId",userId(10))).body());
        assertDenied(getWithToken(token,"/property/customer-relation/list?pageNum=1&pageSize=10"));
    }

    @Test void crossProjectLifecycleAndRevokedPermissionAreRejectedImmediately(){
        assertDenied(json("a","PUT","/property/customer-relation/move-out",Map.of("id",REL_B,"moveOutDate","2026-10-02")));
        String token=token("a");
        assertSuccess(getWithToken(token,"/property/customer-relation/"+REL_A));
        Long menu=jdbc.queryForObject("SELECT menu_id FROM sys_menu WHERE perms='property:customerRelation:moveIn' AND status='0' LIMIT 1",Long.class);
        jdbc.update("DELETE FROM sys_role_menu WHERE role_id=? AND menu_id=?",roleId(10),menu);
        HttpRequest request=HttpRequest.newBuilder(URI.create(url("/property/customer-relation/move-in")))
            .header("Authorization","Bearer "+token).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e")
            .header("Content-Type","application/json").PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of("id",REL_A,"moveInDate","2026-10-01")))).build();
        assertDenied(send(request));
        assertEquals("PENDING",jdbc.queryForObject("SELECT relation_status FROM pm_room_customer_rel WHERE id=?",String.class,REL_A));
    }

    @Test void excelContainsOnlyAuthorizedMaskedProjection(){
        HttpResponse<byte[]> response=export("a","/property/customer-relation/export?projectId="+PROJECT_A);assertEquals(200,response.statusCode());
        List<RoomCustomerRelationVo> rows=ExcelBuilder.read(new ByteArrayInputStream(response.body()),RoomCustomerRelationVo.class).validate(false).headRowNumber(1).doReadSync();assertEquals(1,rows.size());assertEquals("HTTP-CUST-A",rows.getFirst().getCustomerNo());assertNotEquals("13800138000",rows.getFirst().getMobile());
        String binary=new String(response.body(),java.nio.charset.StandardCharsets.ISO_8859_1);assertFalse(binary.contains("13800138000"));assertFalse(binary.toLowerCase(Locale.ROOT).contains("fingerprint"));assertFalse(binary.contains("pii_key_version"));
    }

    private void seedResources(){
        jdbc.update("INSERT INTO pm_project(id,project_code,project_name,sort,status,del_flag) VALUES(?,?,?,0,'0','0'),(?,?,?,0,'0','0')",PROJECT_A,"HTTP-R13-A","项目A",PROJECT_B,"HTTP-R13-B","项目B");
        jdbc.update("INSERT INTO pm_building(id,project_id,building_code,building_name,sort,status,del_flag) VALUES(?,?,?,?,0,'0','0'),(?,?,?,?,0,'0','0')",BASE+11,PROJECT_A,"A-B","A栋",BASE+12,PROJECT_B,"B-B","B栋");
        jdbc.update("INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,sort,status,del_flag) VALUES(?,?,?,?,?,0,'0','0'),(?,?,?,?,?,0,'0','0')",BASE+21,PROJECT_A,BASE+11,"A-U","A单元",BASE+22,PROJECT_B,BASE+12,"B-U","B单元");
        jdbc.update("INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,occupancy_status,sort,status,del_flag) VALUES(?,?,?,?,?,?,'VACANT',0,'0','0'),(?,?,?,?,?,?,'OCCUPIED',0,'0','0')",ROOM_A,PROJECT_A,BASE+11,BASE+21,"A-R","101",ROOM_B,PROJECT_B,BASE+12,BASE+22,"B-R","201");
        jdbc.update("INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,mobile,lifecycle_status,status,del_flag) VALUES(?,?,?,?,?,'ACTIVE','0','0'),(?,?,?,?,?,'ACTIVE','0','0'),(?,?,?,?,?,'ACTIVE','0','0')",CUSTOMER_A,"HTTP-CUST-A","PERSON","张三","13800138000",CUSTOMER_B,"HTTP-CUST-B","PERSON","李四","13900139000",CUSTOMER_NEW,"HTTP-CUST-N","PERSON","王五","13700137000");
        jdbc.update("INSERT INTO pm_room_customer_rel(id,project_id,room_id,customer_id,relation_type,is_primary,start_date,relation_status,status,del_flag) VALUES(?,?,?,?, 'OWNER','1','2026-10-01','PENDING','0','0'),(?,?,?,?, 'OWNER','1','2026-10-01','ACTIVE','0','0')",REL_A,PROJECT_A,ROOM_A,CUSTOMER_A,REL_B,PROJECT_B,ROOM_B,CUSTOMER_B);
    }
    private void seedIdentity(String name,int offset,Long project){long user=userId(offset),role=roleId(offset),employee=employeeId(offset);jdbc.update("INSERT INTO sys_user(user_id,user_name,nick_name,user_type,password,status,del_flag,create_time) VALUES(?,?,?,'sys_user',?,'0','0',NOW())",user,"r13c_"+name,name,PASSWORD_HASH);jdbc.update("INSERT INTO sys_role(role_id,role_name,role_key,role_sort,data_scope,status,del_flag,create_time) VALUES(?,?,?,1,'1','0','0',NOW())",role,"r13c_"+name,"r13c_"+name);jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(?,?)",user,role);for(String p:List.of("list","query","add","edit","remove","export","moveIn","moveOut"))addPermission(role,"property:customerRelation:"+p);jdbc.update("INSERT INTO hr_employee(id,employee_no,employee_name,dept_id,user_id,employment_status,sort,status,del_flag) VALUES(?,?,?,1,?,'ACTIVE',0,'0','0')",employee,"R13C"+offset,name,user);if(project!=null)jdbc.update("INSERT INTO pm_project_member(id,project_id,employee_id,member_role,is_manager,start_date,member_status,sort,status,del_flag) VALUES(?,?,?,'MEMBER','0','2026-01-01','ACTIVE',0,'0','0')",BASE+500+offset,project,employee);}
    private void addPermission(long role,String perm){Long menu=jdbc.queryForObject("SELECT menu_id FROM sys_menu WHERE perms=? AND status='0' LIMIT 1",Long.class,perm);jdbc.update("INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(?,?)",role,menu);}
    @SuppressWarnings("unchecked") private String token(String name){return tokens.computeIfAbsent(name,n->{String username="admin".equals(n)?"admin":"r13c_"+n;HttpResponse<String> r=send(HttpRequest.newBuilder(URI.create(url("/auth/login"))).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of("clientId","e5cd7e4891bf95d1d19206ce24a7b32e","grantType","password","username",username,"password","admin123")))).build());assertEquals(200,r.statusCode());return String.valueOf(((Map<String,Object>)JsonUtils.parseMap(r.body()).get("data")).get("access_token"));});}
    private HttpRequest.Builder auth(String name,String path){return HttpRequest.newBuilder(URI.create(url(path))).header("Authorization","Bearer "+token(name)).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e");}
    private HttpResponse<String> get(String name,String path){return send(auth(name,path).GET().build());}private HttpResponse<String> getWithToken(String token,String path){return send(HttpRequest.newBuilder(URI.create(url(path))).header("Authorization","Bearer "+token).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e").GET().build());}
    private HttpResponse<String> json(String name,String method,String path,Object body){return send(auth(name,path).header("Content-Type","application/json").method(method,HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(body))).build());}private HttpResponse<String> delete(String name,String path){return send(auth(name,path).DELETE().build());}
    private HttpResponse<byte[]> export(String name,String path){try{return http.send(auth(name,path).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofByteArray());}catch(Exception e){throw new AssertionError(e);}}
    private HttpResponse<String> send(HttpRequest request){try{return http.send(request,HttpResponse.BodyHandlers.ofString());}catch(Exception e){throw new AssertionError(e);}}
    private void assertSuccess(HttpResponse<String> r){assertSuccess(r.body());}private void assertSuccess(String body){assertEquals(200,((Number)JsonUtils.parseMap(body).get("code")).intValue(),body);}private void assertEmptyScope(HttpResponse<String> r){assertSuccess(r);assertFalse(r.body().contains("HTTP-R13-A"),r.body());assertFalse(r.body().contains("HTTP-R13-B"),r.body());}private void assertDenied(HttpResponse<String> r){Object code=JsonUtils.parseMap(r.body()).get("code");assertTrue(r.statusCode()!=200||code==null||((Number)code).intValue()!=200,r.body());}
    private String url(String path){return "http://127.0.0.1:"+port+path;}private long userId(int o){return BASE+100+o;}private long employeeId(int o){return BASE+200+o;}private long roleId(int o){return BASE+300+o;}
    private String occupancy(long roomId){return jdbc.queryForObject("SELECT occupancy_status FROM pm_room WHERE id=?",String.class,roomId);}
}
