package org.dromara.web.property.resource;

import org.dromara.DromaraApplication;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.property.resource.importer.domain.PropertyResourceImportRow;
import org.dromara.property.resource.importer.service.PropertyResourceImportCommitHook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** 真实 HTTP → Sa-Token → Scope → ImportService → Mapper → MySQL 验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_HTTP_TEST_ENABLED", matches = "true")
@ResourceLock("zbm-http-integration-db")
@SpringBootTest(classes = DromaraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.flyway.enabled=false", "captcha.enable=false", "api-decrypt.enabled=false",
        "spring.datasource.dynamic.datasource.master.url=${ZBM_TEST_DB_URL}",
        "spring.datasource.dynamic.datasource.master.username=${ZBM_TEST_DB_USER}",
        "spring.datasource.dynamic.datasource.master.password=${ZBM_TEST_DB_PASSWORD}",
        "mybatis-encryptor.enable=true", "mybatis-encryptor.algorithm=AES",
        "mybatis-encryptor.password=0123456789abcdef0123456789abcdef",
        "property.customer.security.enabled=true",
        "property.customer.security.hmac-key-base64=YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=",
        "property.customer.security.key-version=tree-import-test"})
@Import(PropertyResourceTreeImportHttpIntegrationTest.HookConfig.class)
class PropertyResourceTreeImportHttpIntegrationTest {
    private static final long BASE = 914014000000000000L;
    private static final long PROJECT_A = BASE + 1, PROJECT_B = BASE + 2;
    private static final long MENU_IMPORT = BASE + 900;
    private static final String HASH = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";
    private static final AtomicReference<HookAction> HOOK = new AtomicReference<>();
    private static final AtomicReference<Runnable> BEFORE_COMMIT = new AtomicReference<>();

    @LocalServerPort private int port;
    @Autowired private JdbcTemplate jdbc;
    private final HttpClient http = HttpClient.newHttpClient();
    private final Map<String, String> tokens = new HashMap<>();

    @BeforeEach void setUp() {
        assertEquals("zbm_property_test", jdbc.queryForObject("SELECT DATABASE()", String.class));
        cleanup();
        jdbc.update("INSERT INTO pm_project(id,project_code,project_name,sort,status,del_flag) VALUES(?,?,?,0,'0','0'),(?,?,?,0,'0','0')",
            PROJECT_A, "T14-A", "项目A", PROJECT_B, "T14-B", "项目B");
        ensurePermissionMenu();
        seedIdentity("allowed", 10, PROJECT_A, true, false);
        seedIdentity("scopeOnly", 20, PROJECT_A, false, false);
        seedIdentity("permissionOnly", 30, null, true, false);
        seedIdentity("management", 40, null, true, true);
    }

    @AfterEach void cleanup() {
        HOOK.set(null);
        BEFORE_COMMIT.set(null);
        jdbc.update("DELETE FROM pm_room WHERE project_id IN (?,?)", PROJECT_A, PROJECT_B);
        jdbc.update("DELETE FROM pm_unit WHERE project_id IN (?,?)", PROJECT_A, PROJECT_B);
        jdbc.update("DELETE FROM pm_building WHERE project_id IN (?,?)", PROJECT_A, PROJECT_B);
        jdbc.update("DELETE FROM pm_project_member WHERE project_id IN (?,?)", PROJECT_A, PROJECT_B);
        jdbc.update("DELETE FROM pm_project_management_grant WHERE principal_id BETWEEN ? AND ?", BASE + 100, BASE + 199);
        jdbc.update("DELETE FROM pm_project WHERE id IN (?,?)", PROJECT_A, PROJECT_B);
        jdbc.update("DELETE FROM hr_employee WHERE id BETWEEN ? AND ?", BASE + 200, BASE + 299);
        jdbc.update("DELETE FROM sys_role_menu WHERE role_id BETWEEN ? AND ?", BASE + 300, BASE + 399);
        jdbc.update("DELETE FROM sys_user_role WHERE user_id BETWEEN ? AND ?", BASE + 100, BASE + 199);
        jdbc.update("DELETE FROM sys_role WHERE role_id BETWEEN ? AND ?", BASE + 300, BASE + 399);
        jdbc.update("DELETE FROM sys_user WHERE user_id BETWEEN ? AND ?", BASE + 100, BASE + 199);
        jdbc.update("DELETE FROM sys_login_info WHERE user_name LIKE 't14_%'");
        jdbc.update("DELETE FROM sys_menu WHERE menu_id=?", MENU_IMPORT);
        tokens.clear();
    }

    @Test void atomic01_duplicateRoomAtRow150WritesNothing() {
        List<PropertyResourceImportRow> rows = new ArrayList<>();
        for (int i = 1; i <= 200; i++) rows.add(row("B", "一号楼", "U", i == 150 ? "R149" : "R" + i));
        assertAtomicFailure(importRows("admin", PROJECT_A, rows), 200);
    }
    @Test void atomic02_buildingConflictWritesNothing() { assertAtomicFailure(importRows("admin", PROJECT_A, List.of(row("B","一号楼","U","R1"), row("B","壹号楼","U","R2"))), 2); }
    @Test void atomic03_unitConflictWritesNothing() { PropertyResourceImportRow b=row("B","一号楼","U","R2"); b.setUnitName("另一个单元"); assertAtomicFailure(importRows("admin",PROJECT_A,List.of(row("B","一号楼","U","R1"),b)),2); }
    @Test void atomic04_existingBuildingConflictWritesNothing() { insertBuilding(PROJECT_A, BASE+11,"B","数据库名称"); assertAtomicFailure(importRows("admin",PROJECT_A,List.of(row("B","Excel名称","U","R1"))),1); }
    @Test void atomic05_reusesMatchingParentsAndAddsOnlyRoom() { long b=BASE+11,u=BASE+21;insertBuilding(PROJECT_A,b,"B","一号楼");insertUnit(PROJECT_A,b,u,"U","一单元");assertAtomicSuccess(importRows("admin",PROJECT_A,List.of(row("B","一号楼","U","R1"))),1);assertEquals(1,count("pm_building"));assertEquals(1,count("pm_unit"));assertEquals(1,count("pm_room")); }
    @Test void atomic06_newTreeSucceeds() { assertAtomicSuccess(importRows("admin",PROJECT_A,List.of(row("B","一号楼","U","R1"))),1);assertEquals(1,count("pm_building"));assertEquals(1,count("pm_unit"));assertEquals(1,count("pm_room"));assertEquals("VACANT",jdbc.queryForObject("SELECT occupancy_status FROM pm_room WHERE project_id=? AND room_code='R1'",String.class,PROJECT_A)); }
    @Test void atomic07_commitUniqueConflictRollsBackEarlierRows() { HOOK.set((p,r)->{if("R2".equals(r.getRoomCode())) insertCompetingRoomSameTransaction(p,r);});assertAtomicFailure(importRows("admin",PROJECT_A,List.of(row("B","一号楼","U","R1"),row("B","一号楼","U","R2"))),2);assertEquals(0,count("pm_room")); }
    @Test void atomic08_validateThenConcurrentInsertRollsBackCurrentTransaction() { long b=BASE+11,u=BASE+21;insertBuilding(PROJECT_A,b,"B","一号楼");insertUnit(PROJECT_A,b,u,"U","一单元");BEFORE_COMMIT.set(()->insertCompetingRoomSeparateTransaction(PROJECT_A,row("B","一号楼","U","R2")));assertAtomicFailureResult(importRows("admin",PROJECT_A,List.of(row("B","一号楼","U","R1"),row("B","一号楼","U","R2"))),2);assertEquals(1,count("pm_room"));assertEquals("R2",jdbc.queryForObject("SELECT room_code FROM pm_room WHERE project_id=?",String.class,PROJECT_A)); }
    @Test void atomic09_neverReusesOtherProjectParents() { insertBuilding(PROJECT_B,BASE+12,"B","一号楼");insertUnit(PROJECT_B,BASE+12,BASE+22,"U","一单元");assertAtomicSuccess(importRows("admin",PROJECT_A,List.of(row("B","一号楼","U","R1"))),1);assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM pm_building WHERE project_id=?",Integer.class,PROJECT_A)); }

    @Test void security01_permissionAndResourceWriteSucceeds() { assertAtomicSuccess(importRows("allowed",PROJECT_A,List.of(row("B","一号楼","U","R1"))),1); }
    @Test void security02_permissionWithoutResourceWriteDenied() { assertDenied(importRows("permissionOnly",PROJECT_A,List.of(row("B","一号楼","U","R1")))); }
    @Test void security03_resourceWriteWithoutPermissionDenied() { assertDenied(importRows("scopeOnly",PROJECT_A,List.of(row("B","一号楼","U","R1")))); }
    @Test void security04_managementOnlyDenied() { assertDenied(importRows("management",PROJECT_A,List.of(row("B","一号楼","U","R1")))); }
    @Test void security05_projectAUserCannotImportProjectB() { assertDenied(importRows("allowed",PROJECT_B,List.of(row("B","一号楼","U","R1")))); }
    @Test void security06_revocationInvalidatesOldTokenImmediately() { String token=token("allowed");assertAtomicSuccess(importRowsWithToken(token,PROJECT_A,List.of(row("B","一号楼","U","R1"))),1);jdbc.update("DELETE FROM sys_role_menu WHERE role_id=? AND menu_id=?",roleId(10),MENU_IMPORT);assertDenied(importRowsWithToken(token,PROJECT_A,List.of(row("B","一号楼","U","R2")))); }

    @Test void templateContainsExactHeadersExampleAndNoIds() throws Exception {
        HttpResponse<byte[]> response=http.send(auth("allowed","/property/resource/tree-import/template").GET().build(),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200,response.statusCode());
        List<PropertyResourceImportRow> rows=ExcelBuilder.read(new ByteArrayInputStream(response.body()),PropertyResourceImportRow.class).validate(false).doReadSync();
        assertEquals(1,rows.size());assertEquals("1#",rows.getFirst().getBuildingCode());
        String workbookXml=readWorkbookXml(response.body());
        assertFalse(workbookXml.contains("projectId"));assertFalse(workbookXml.contains("buildingId"));
        assertFalse(workbookXml.contains("unitId"));assertFalse(workbookXml.contains("roomId"));
        for(String header:List.of("楼栋编码","楼栋名称","楼栋类型","单元编码","单元名称","房屋编码","房号","状态"))assertTrue(workbookXml.contains(header),header);
    }

    private HttpResponse<String> importRows(String user,long project,List<PropertyResourceImportRow> rows){return importRowsWithToken(token(user),project,rows);}
    private HttpResponse<String> importRowsWithToken(String token,long project,List<PropertyResourceImportRow> rows){try{byte[] xlsx=excel(rows);String boundary="----T14"+UUID.randomUUID();ByteArrayOutputStream body=new ByteArrayOutputStream();write(body,"--"+boundary+"\r\nContent-Disposition: form-data; name=\"projectId\"\r\n\r\n"+project+"\r\n");write(body,"--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"import.xlsx\"\r\nContent-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n");body.write(xlsx);write(body,"\r\n--"+boundary+"--\r\n");return http.send(HttpRequest.newBuilder(URI.create(url("/property/resource/tree-import"))).header("Authorization","Bearer "+token).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e").header("Content-Type","multipart/form-data; boundary="+boundary).POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());}catch(Exception e){throw new AssertionError(e);}}
    private byte[] excel(List<PropertyResourceImportRow> rows){try(ByteArrayOutputStream out=new ByteArrayOutputStream()){ExcelBuilder.of(rows,PropertyResourceImportRow.class).sheetName("房屋资源").toStream(out);return out.toByteArray();}catch(IOException e){throw new UncheckedIOException(e);}}
    @SuppressWarnings("unchecked") private Map<String,Object> data(HttpResponse<String> r){return (Map<String,Object>)JsonUtils.parseMap(r.body()).get("data");}
    private void assertAtomicFailure(HttpResponse<String> r,int total){assertAtomicFailureResult(r,total);assertEquals(0,count("pm_room"));}
    private void assertAtomicFailureResult(HttpResponse<String> r,int total){assertEquals(200,r.statusCode(),r.body());Map<String,Object>d=data(r);assertNotNull(d,r.body());assertEquals(total,((Number)d.get("totalRows")).intValue());assertEquals(0,((Number)d.get("successRows")).intValue());assertEquals(total,((Number)d.get("failedRows")).intValue());}
    private void assertAtomicSuccess(HttpResponse<String> r,int total){assertEquals(200,r.statusCode(),r.body());Map<String,Object>d=data(r);assertNotNull(d,r.body());assertEquals(total,((Number)d.get("successRows")).intValue());assertEquals(0,((Number)d.get("failedRows")).intValue());}
    private void assertDenied(HttpResponse<String> r){Object code=JsonUtils.parseMap(r.body()).get("code");assertTrue(r.statusCode()!=200||code==null||((Number)code).intValue()!=200,r.body());}
    private int count(String table){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE project_id IN (?,?)",Integer.class,PROJECT_A,PROJECT_B);}
    private PropertyResourceImportRow row(String bc,String bn,String uc,String rc){PropertyResourceImportRow r=new PropertyResourceImportRow();r.setBuildingCode(bc);r.setBuildingName(bn);r.setBuildingType("RESIDENTIAL");r.setBuildingSort(0);r.setUnitCode(uc);r.setUnitName("一单元");r.setUnitSort(0);r.setRoomCode(rc);r.setRoomNo(rc);r.setRoomType("RESIDENCE");r.setPropertyType("PRIVATE");r.setDeliveryStatus("UNDELIVERED");r.setOccupancyStatus("VACANT");r.setRoomSort(0);r.setStatus("0");return r;}
    private void insertBuilding(long project,long id,String code,String name){jdbc.update("INSERT INTO pm_building(id,project_id,building_code,building_name,building_type,status,del_flag) VALUES(?,?,?,?, 'RESIDENTIAL','0','0')",id,project,code,name);}
    private void insertUnit(long project,long building,long id,String code,String name){jdbc.update("INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,status,del_flag) VALUES(?,?,?,?,?,'0','0')",id,project,building,code,name);}
    private void insertCompetingRoomSameTransaction(long project,PropertyResourceImportRow row){Long b=jdbc.queryForObject("SELECT id FROM pm_building WHERE project_id=? AND building_code=? AND del_flag='0'",Long.class,project,row.getBuildingCode());Long u=jdbc.queryForObject("SELECT id FROM pm_unit WHERE building_id=? AND unit_code=? AND del_flag='0'",Long.class,b,row.getUnitCode());jdbc.update("INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,status,del_flag) VALUES(?,?,?,?,?,?, '0','0')",Math.abs(UUID.randomUUID().getMostSignificantBits()),project,b,u,row.getRoomCode(),row.getRoomNo());}
    private void insertCompetingRoomSeparateTransaction(long project,PropertyResourceImportRow row){try(var c=DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),System.getenv("ZBM_TEST_DB_USER"),System.getenv("ZBM_TEST_DB_PASSWORD"));var s=c.prepareStatement("INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,status,del_flag) SELECT ?,?,p.id,u.id,?,?,'0','0' FROM pm_building p JOIN pm_unit u ON u.building_id=p.id WHERE p.project_id=? AND p.building_code=? AND u.unit_code=?")){s.setLong(1,Math.abs(UUID.randomUUID().getMostSignificantBits()));s.setLong(2,project);s.setString(3,row.getRoomCode());s.setString(4,row.getRoomNo());s.setLong(5,project);s.setString(6,row.getBuildingCode());s.setString(7,row.getUnitCode());assertEquals(1,s.executeUpdate());}catch(Exception e){throw new AssertionError(e);}}
    private void ensurePermissionMenu(){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE menu_id=?",Integer.class,MENU_IMPORT);if(n==0)jdbc.update("INSERT INTO sys_menu(menu_id,parent_id,menu_name,order_num,path,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time) VALUES(?,2609252000000000303,'测试-资源树导入',99,'','N','N','F','0','0','property:resource:treeImport','#',1,NOW())",MENU_IMPORT);}
    private void seedIdentity(String name,int offset,Long project,boolean permission,boolean management){long user=userId(offset),role=roleId(offset),employee=employeeId(offset);jdbc.update("INSERT INTO sys_user(user_id,user_name,nick_name,user_type,password,status,del_flag,create_time) VALUES(?,?,?,'sys_user',?,'0','0',NOW())",user,"t14_"+name,name,HASH);jdbc.update("INSERT INTO sys_role(role_id,role_name,role_key,role_sort,data_scope,status,del_flag,create_time) VALUES(?,?,?,1,'1','0','0',NOW())",role,"t14_"+name,"t14_"+name);jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(?,?)",user,role);if(permission)jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(?,?)",role,MENU_IMPORT);jdbc.update("INSERT INTO hr_employee(id,employee_no,employee_name,dept_id,user_id,employment_status,sort,status,del_flag) VALUES(?,?,?,1,?,'ACTIVE',0,'0','0')",employee,"T14"+offset,name,user);if(project!=null)jdbc.update("INSERT INTO pm_project_member(id,project_id,employee_id,member_role,is_manager,start_date,member_status,sort,status,del_flag) VALUES(?,?,?,'MEMBER','0','2026-01-01','ACTIVE',0,'0','0')",BASE+500+offset,project,employee);if(management)jdbc.update("INSERT INTO pm_project_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,'USER',?,'ALL','0',0,'0')",BASE+700+offset,user);}
    @SuppressWarnings("unchecked") private String token(String name){return tokens.computeIfAbsent(name,n->{String username="admin".equals(n)?"admin":"t14_"+n;try{HttpResponse<String> r=http.send(HttpRequest.newBuilder(URI.create(url("/auth/login"))).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of("clientId","e5cd7e4891bf95d1d19206ce24a7b32e","grantType","password","username",username,"password","admin123")))).build(),HttpResponse.BodyHandlers.ofString());return String.valueOf(((Map<String,Object>)JsonUtils.parseMap(r.body()).get("data")).get("access_token"));}catch(Exception e){throw new AssertionError(e);}});}
    private HttpRequest.Builder auth(String user,String path){return HttpRequest.newBuilder(URI.create(url(path))).header("Authorization","Bearer "+token(user)).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e");}
    private String url(String path){return "http://127.0.0.1:"+port+path;} private long userId(int o){return BASE+100+o;}private long employeeId(int o){return BASE+200+o;}private long roleId(int o){return BASE+300+o;}
    private static void write(OutputStream out,String value)throws IOException{out.write(value.getBytes(StandardCharsets.UTF_8));}
    private String readWorkbookXml(byte[] bytes)throws IOException{
        File file=File.createTempFile("tree-import-template-",".xlsx");
        try{
            java.nio.file.Files.write(file.toPath(),bytes);
            try(var zip=new java.util.zip.ZipFile(file)){
                return readZipEntry(zip,"xl/sharedStrings.xml")+readZipEntry(zip,"xl/worksheets/sheet1.xml");
            }
        }finally{assertTrue(file.delete()||!file.exists());}
    }
    private String readZipEntry(java.util.zip.ZipFile zip,String name)throws IOException{var entry=zip.getEntry(name);assertNotNull(entry,name);try(var in=zip.getInputStream(entry)){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}

    @FunctionalInterface interface HookAction { void run(Long projectId, PropertyResourceImportRow row); }
    @TestConfiguration static class HookConfig { @Bean PropertyResourceImportCommitHook testHook(){return new PropertyResourceImportCommitHook(){@Override public void beforeCommit(Long p){Runnable action=BEFORE_COMMIT.getAndSet(null);if(action!=null)action.run();}@Override public void beforeRoomInsert(Long p,PropertyResourceImportRow r){HookAction action=HOOK.get();if(action!=null)action.run(p,r);}};} }
}
