package org.dromara.web.property.relation;

import org.dromara.DromaraApplication;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.property.customer.relation.importer.domain.RoomCustomerRelationImportRow;
import org.dromara.property.customer.relation.importer.service.RoomCustomerRelationImportCommitHook;
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
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** RoomCustomerRelation 导入真实 HTTP、Sa-Token 与 MySQL 原子验收。 */
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
        "property.customer.security.key-version=relation-import-test"})
@Import(RoomCustomerRelationImportHttpIntegrationTest.HookConfig.class)
class RoomCustomerRelationImportHttpIntegrationTest {
    private static final long BASE=914014400000000000L,A=BASE+1,B=BASE+2,BUILDING_A=BASE+11,UNIT_A=BASE+21;
    private static final long MENU_IMPORT=BASE+900,MENU_ADD=BASE+901;
    private static final String HASH="$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";
    private static final AtomicReference<Runnable> BEFORE_COMMIT=new AtomicReference<>();
    private static final AtomicReference<HookAction> BEFORE_INSERT=new AtomicReference<>();
    @LocalServerPort private int port;
    @Autowired private JdbcTemplate jdbc;
    private final HttpClient http=HttpClient.newHttpClient();
    private final Map<String,String> tokens=new HashMap<>();

    @BeforeEach void setUp(){assertEquals("zbm_property_test",jdbc.queryForObject("SELECT DATABASE()",String.class));cleanup();seedProjects();ensureMenu(MENU_IMPORT,"property:customer-relation:import");ensureMenu(MENU_ADD,"property:customerRelation:add");seedIdentity("allowed",10,A,true,false,false);seedIdentity("addOnly",20,A,false,true,false);seedIdentity("permissionOnly",30,null,true,false,false);seedIdentity("management",40,null,true,false,true);seedIdentity("customerGrant",50,null,true,false,false);jdbc.update("INSERT INTO pm_customer_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,'USER',?,'ENTERPRISE','0',0,'0')",BASE+750,userId(50));}
    @AfterEach void cleanup(){BEFORE_COMMIT.set(null);BEFORE_INSERT.set(null);jdbc.update("DELETE FROM pm_room_customer_rel WHERE project_id IN (?,?)",A,B);jdbc.update("DELETE FROM pm_customer WHERE customer_no LIKE 'R14D-%'");jdbc.update("DELETE FROM pm_room WHERE project_id IN (?,?)",A,B);jdbc.update("DELETE FROM pm_unit WHERE project_id IN (?,?)",A,B);jdbc.update("DELETE FROM pm_building WHERE project_id IN (?,?)",A,B);jdbc.update("DELETE FROM pm_project_member WHERE project_id IN (?,?)",A,B);jdbc.update("DELETE FROM pm_customer_management_grant WHERE principal_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM pm_project_management_grant WHERE principal_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM pm_project WHERE id IN (?,?)",A,B);jdbc.update("DELETE FROM hr_employee WHERE id BETWEEN ? AND ?",BASE+200,BASE+299);jdbc.update("DELETE FROM sys_role_menu WHERE role_id BETWEEN ? AND ?",BASE+300,BASE+399);jdbc.update("DELETE FROM sys_user_role WHERE user_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM sys_role WHERE role_id BETWEEN ? AND ?",BASE+300,BASE+399);jdbc.update("DELETE FROM sys_user WHERE user_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM sys_login_info WHERE user_name LIKE 'r14d_%'");jdbc.update("DELETE FROM sys_menu WHERE menu_id IN (?,?)",MENU_IMPORT,MENU_ADD);tokens.clear();}

    @Test void atomic01_oneHundredValidRowsSucceed(){List<RoomCustomerRelationImportRow> rows=seedRows(100,A);success(importRows("admin",A,rows),100);assertEquals(100,countRelations(A));assertEquals(100,jdbc.queryForObject("SELECT COUNT(*) FROM pm_room_customer_rel WHERE project_id=? AND relation_status='PENDING'",Integer.class,A));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM pm_room WHERE project_id=? AND occupancy_status<>'VACANT'",Integer.class,A));}
    @Test void atomic02_missingRoomWritesZero(){List<RoomCustomerRelationImportRow> rows=seedRows(3,A);rows.get(1).setRoomCode("MISSING");failure(importRows("admin",A,rows),3);assertEquals(0,countRelations(A));}
    @Test void atomic03_missingCustomerWritesZero(){List<RoomCustomerRelationImportRow> rows=seedRows(3,A);rows.get(1).setCustomerNo("R14D-MISSING");failure(importRows("admin",A,rows),3);assertEquals(0,countRelations(A));}
    @Test void atomic04_fileDuplicateWritesZero(){List<RoomCustomerRelationImportRow> rows=seedRows(2,A);rows.set(1,copy(rows.getFirst()));failure(importRows("admin",A,rows),2);assertEquals(0,countRelations(A));}
    @Test void atomic05_existingEffectiveRelationWritesZero(){List<RoomCustomerRelationImportRow> rows=seedRows(2,A);insertRelation(BASE+5000,roomId(0),customerId(0),"OWNER","ACTIVE","1");failure(importRows("admin",A,rows),2);assertEquals(1,countRelations(A));}
    @Test void atomic06_inactiveAndCancelledCustomersWriteZero(){List<RoomCustomerRelationImportRow> rows=seedRows(2,A);jdbc.update("UPDATE pm_customer SET lifecycle_status='INACTIVE',status='1' WHERE id=?",customerId(0));jdbc.update("UPDATE pm_customer SET lifecycle_status='CANCELLED',status='1' WHERE id=?",customerId(1));failure(importRows("admin",A,rows),2);assertEquals(0,countRelations(A));}
    @Test void atomic07_crossProjectHierarchyWritesZero(){List<RoomCustomerRelationImportRow> rows=seedRows(1,B);failure(importRows("admin",A,rows),1);assertEquals(0,countRelations(A));}
    @Test void atomic08_primaryEffectiveConflictWritesZero(){List<RoomCustomerRelationImportRow> rows=seedRows(1,A);insertRelation(BASE+5001,roomId(0),customerId(0),"OWNER","PENDING","1");failure(importRows("admin",A,rows),1);assertEquals(1,countRelations(A));}
    @Test void atomic09_validateThenCompetingRelationRollsBack(){List<RoomCustomerRelationImportRow> rows=seedRows(2,A);BEFORE_COMMIT.set(()->insertRelationSeparate(BASE+5100,roomId(1),customerId(1)));failure(importRows("admin",A,rows),2);assertEquals(1,countRelations(A));}
    @Test void atomic10_row80InsertFailureRollsBackFirst79(){List<RoomCustomerRelationImportRow> rows=seedRows(100,A);BEFORE_INSERT.set((i,r)->{if(i==79)throw new IllegalStateException("test insert failure");});failure(importRows("admin",A,rows),100);assertEquals(0,countRelations(A));}
    @Test void atomic11_roomDeleteRaceCreatesNoOrphan(){List<RoomCustomerRelationImportRow> rows=seedRows(1,A);BEFORE_INSERT.set((i,r)->jdbc.update("UPDATE pm_room SET del_flag='1' WHERE id=?",r.getRoomId()));failure(importRows("admin",A,rows),1);assertEquals(0,countRelations(A));assertEquals("0",jdbc.queryForObject("SELECT del_flag FROM pm_room WHERE id=?",String.class,roomId(0)));}
    @Test void atomic12_customerDisableRaceCreatesNoRelation(){List<RoomCustomerRelationImportRow> rows=seedRows(1,A);BEFORE_INSERT.set((i,r)->disableCustomerSeparate(r.getCustomerId()));failure(importRows("admin",A,rows),1);assertEquals(0,countRelations(A));assertEquals("INACTIVE",jdbc.queryForObject("SELECT lifecycle_status FROM pm_customer WHERE id=?",String.class,customerId(0)));}

    @Test void securityA_importAndScopeSucceeds(){success(importRows("allowed",A,seedRows(1,A)),1);}
    @Test void securityB_addWithoutImportDenied(){denied(importRows("addOnly",A,seedRows(1,A)));}
    @Test void securityC_importWithoutScopeDenied(){denied(importRows("permissionOnly",A,seedRows(1,A)));}
    @Test void securityD_managementAllOnlyDenied(){denied(importRows("management",A,seedRows(1,A)));}
    @Test void securityE_customerGrantOnlyDenied(){denied(importRows("customerGrant",A,seedRows(1,A)));}
    @Test void securityF_projectAUserCannotImportProjectB(){denied(importRows("allowed",B,seedRows(1,B)));}
    @Test void securityG_permissionRevocationInvalidatesOldToken(){List<RoomCustomerRelationImportRow> rows=seedRows(2,A);String t=token("allowed");success(importRows(t,A,List.of(rows.getFirst()),true),1);jdbc.update("DELETE FROM sys_role_menu WHERE role_id=? AND menu_id=?",roleId(10),MENU_IMPORT);denied(importRows(t,A,List.of(rows.get(1)),true));}
    @Test void securityH_scopeRevocationInvalidatesOldToken(){List<RoomCustomerRelationImportRow> rows=seedRows(2,A);String t=token("allowed");success(importRows(t,A,List.of(rows.getFirst()),true),1);jdbc.update("UPDATE pm_project_member SET del_flag='1' WHERE project_id=? AND employee_id=?",A,employeeId(10));denied(importRows(t,A,List.of(rows.get(1)),true));}
    @Test void superAdminUsesUnifiedBypass(){success(importRows("admin",A,seedRows(1,A)),1);}
    @Test void templateUsesBusinessKeysAndChineseLabels()throws Exception{HttpResponse<byte[]> r=http.send(auth("allowed","/property/customer-relation/import/template").GET().build(),HttpResponse.BodyHandlers.ofByteArray());assertEquals(200,r.statusCode());List<RoomCustomerRelationImportRow> rows=ExcelBuilder.read(new ByteArrayInputStream(r.body()),RoomCustomerRelationImportRow.class).validate(false).doReadSync();assertEquals(1,rows.size());assertEquals("产权人/业主",rows.getFirst().getRelationType());String xml=workbook(r.body());for(String h:List.of("楼栋编码","单元编码","房屋编码","客户编号","关系类型","是否主要关系","开始日期","备注"))assertTrue(xml.contains(h));for(String id:List.of("projectId","buildingId","unitId","roomId","customerId","relationId"))assertFalse(xml.contains(id));}

    private void seedProjects(){jdbc.update("INSERT INTO pm_project(id,project_code,project_name,sort,status,del_flag) VALUES(?,?,?,0,'0','0'),(?,?,?,0,'0','0')",A,"R14D-A","项目A",B,"R14D-B","项目B");}
    private List<RoomCustomerRelationImportRow> seedRows(int n,long project){long building=project==A?BUILDING_A:BASE+12,unit=project==A?UNIT_A:BASE+22;jdbc.update("INSERT INTO pm_building(id,project_id,building_code,building_name,status,del_flag) VALUES(?,?,?,?, '0','0')",building,project,"B","一号楼");jdbc.update("INSERT INTO pm_unit(id,project_id,building_id,unit_code,unit_name,status,del_flag) VALUES(?,?,?,?,?,'0','0')",unit,project,building,"U","一单元");List<RoomCustomerRelationImportRow> rows=new ArrayList<>();for(int i=0;i<n;i++){long room=roomId(i),customer=customerId(i);jdbc.update("INSERT INTO pm_room(id,project_id,building_id,unit_id,room_code,room_no,status,del_flag) VALUES(?,?,?,?,?,?, '0','0')",room,project,building,unit,"R"+i,String.valueOf(100+i));jdbc.update("INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,lifecycle_status,status,version,del_flag) VALUES(?,?,'PERSON',?,'ACTIVE','0',0,'0')",customer,"R14D-C"+i,"客户"+i);RoomCustomerRelationImportRow r=new RoomCustomerRelationImportRow();r.setBuildingCode("B");r.setUnitCode("U");r.setRoomCode("R"+i);r.setCustomerNo("R14D-C"+i);r.setRelationType("产权人/业主");r.setIsPrimary("是");r.setStartDate(LocalDate.of(2026,1,1));rows.add(r);}return rows;}
    private RoomCustomerRelationImportRow copy(RoomCustomerRelationImportRow x){RoomCustomerRelationImportRow r=new RoomCustomerRelationImportRow();r.setBuildingCode(x.getBuildingCode());r.setUnitCode(x.getUnitCode());r.setRoomCode(x.getRoomCode());r.setCustomerNo(x.getCustomerNo());r.setRelationType(x.getRelationType());r.setIsPrimary(x.getIsPrimary());r.setStartDate(x.getStartDate());return r;}
    private void insertRelation(long id,long room,long customer,String type,String state,String primary){jdbc.update("INSERT INTO pm_room_customer_rel(id,project_id,room_id,customer_id,relation_type,is_primary,start_date,relation_status,status,del_flag) VALUES(?,?,?,?,?,?,CURRENT_DATE,?,'0','0')",id,A,room,customer,type,primary,state);}
    private void insertRelationSeparate(long id,long room,long customer){try(var c=DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),System.getenv("ZBM_TEST_DB_USER"),System.getenv("ZBM_TEST_DB_PASSWORD"));var s=c.prepareStatement("INSERT INTO pm_room_customer_rel(id,project_id,room_id,customer_id,relation_type,is_primary,start_date,relation_status,status,del_flag) VALUES(?,?,?,?,'OWNER','1',CURRENT_DATE,'PENDING','0','0')")){s.setLong(1,id);s.setLong(2,A);s.setLong(3,room);s.setLong(4,customer);s.executeUpdate();}catch(Exception e){throw new AssertionError(e);}}
    private void disableCustomerSeparate(long id){try(var c=DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),System.getenv("ZBM_TEST_DB_USER"),System.getenv("ZBM_TEST_DB_PASSWORD"));var s=c.prepareStatement("UPDATE pm_customer SET lifecycle_status='INACTIVE',status='1' WHERE id=?")){s.setLong(1,id);s.executeUpdate();}catch(Exception e){throw new AssertionError(e);}}
    private HttpResponse<String> importRows(String user,long project,List<RoomCustomerRelationImportRow> rows){return importRows(token(user),project,rows,true);}
    private HttpResponse<String> importRows(String token,long project,List<RoomCustomerRelationImportRow> rows,boolean direct){try{byte[] xlsx=excel(rows);String b="----R14D"+UUID.randomUUID();ByteArrayOutputStream body=new ByteArrayOutputStream();write(body,"--"+b+"\r\nContent-Disposition: form-data; name=\"projectId\"\r\n\r\n"+project+"\r\n");write(body,"--"+b+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"relations.xlsx\"\r\nContent-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n");body.write(xlsx);write(body,"\r\n--"+b+"--\r\n");return http.send(HttpRequest.newBuilder(URI.create(url("/property/customer-relation/import"))).header("Authorization","Bearer "+token).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e").header("Content-Type","multipart/form-data; boundary="+b).POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());}catch(Exception e){throw new AssertionError(e);}}
    private byte[] excel(List<RoomCustomerRelationImportRow> rows){try(ByteArrayOutputStream out=new ByteArrayOutputStream()){ExcelBuilder.of(rows,RoomCustomerRelationImportRow.class).sheetName("关系").toStream(out);return out.toByteArray();}catch(IOException e){throw new UncheckedIOException(e);}}
    @SuppressWarnings("unchecked") private Map<String,Object> data(HttpResponse<String> r){return (Map<String,Object>)JsonUtils.parseMap(r.body()).get("data");}
    private void success(HttpResponse<String> r,int n){assertEquals(200,r.statusCode(),r.body());Map<String,Object>d=data(r);assertNotNull(d,r.body());assertEquals(n,((Number)d.get("successRows")).intValue());assertEquals(0,((Number)d.get("failedRows")).intValue());}
    private void failure(HttpResponse<String> r,int n){assertEquals(200,r.statusCode(),r.body());Map<String,Object>d=data(r);assertNotNull(d,r.body());assertEquals(n,((Number)d.get("totalRows")).intValue());assertEquals(0,((Number)d.get("successRows")).intValue());assertEquals(n,((Number)d.get("failedRows")).intValue());}
    private void denied(HttpResponse<String> r){Object code=JsonUtils.parseMap(r.body()).get("code");assertTrue(r.statusCode()!=200||code==null||((Number)code).intValue()!=200,r.body());}
    private int countRelations(long project){return jdbc.queryForObject("SELECT COUNT(*) FROM pm_room_customer_rel WHERE project_id=? AND del_flag='0'",Integer.class,project);}
    private void ensureMenu(long id,String permission){jdbc.update("INSERT INTO sys_menu(menu_id,parent_id,menu_name,order_num,path,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time) VALUES(?,2609252000000000303,?,99,'','N','N','F','0','0',?,'#',1,NOW())",id,"测试-"+permission,permission);}
    private void seedIdentity(String name,int offset,Long project,boolean imp,boolean add,boolean management){long user=userId(offset),role=roleId(offset),employee=employeeId(offset);jdbc.update("INSERT INTO sys_user(user_id,user_name,nick_name,user_type,password,status,del_flag,create_time) VALUES(?,?,?,'sys_user',?,'0','0',NOW())",user,"r14d_"+name,name,HASH);jdbc.update("INSERT INTO sys_role(role_id,role_name,role_key,role_sort,data_scope,status,del_flag,create_time) VALUES(?,?,?,1,'1','0','0',NOW())",role,"r14d_"+name,"r14d_"+name);jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(?,?)",user,role);if(imp)jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(?,?)",role,MENU_IMPORT);if(add)jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(?,?)",role,MENU_ADD);jdbc.update("INSERT INTO hr_employee(id,employee_no,employee_name,dept_id,user_id,employment_status,sort,status,del_flag) VALUES(?,?,?,1,?,'ACTIVE',0,'0','0')",employee,"R14D"+offset,name,user);if(project!=null)jdbc.update("INSERT INTO pm_project_member(id,project_id,employee_id,member_role,is_manager,start_date,member_status,sort,status,del_flag) VALUES(?,?,?,'MEMBER','0','2026-01-01','ACTIVE',0,'0','0')",BASE+600+offset,project,employee);if(management)jdbc.update("INSERT INTO pm_project_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,'USER',?,'ALL','0',0,'0')",BASE+700+offset,user);}
    @SuppressWarnings("unchecked") private String token(String name){return tokens.computeIfAbsent(name,n->{String u="admin".equals(n)?"admin":"r14d_"+n;try{HttpResponse<String>r=http.send(HttpRequest.newBuilder(URI.create(url("/auth/login"))).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of("clientId","e5cd7e4891bf95d1d19206ce24a7b32e","grantType","password","username",u,"password","admin123")))).build(),HttpResponse.BodyHandlers.ofString());return String.valueOf(((Map<String,Object>)JsonUtils.parseMap(r.body()).get("data")).get("access_token"));}catch(Exception e){throw new AssertionError(e);}});}
    private HttpRequest.Builder auth(String user,String path){return HttpRequest.newBuilder(URI.create(url(path))).header("Authorization","Bearer "+token(user)).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e");}
    private String workbook(byte[] bytes)throws IOException{File f=File.createTempFile("r14d-",".xlsx");try{java.nio.file.Files.write(f.toPath(),bytes);try(var z=new java.util.zip.ZipFile(f)){return zip(z,"xl/sharedStrings.xml")+zip(z,"xl/worksheets/sheet1.xml");}}finally{assertTrue(f.delete()||!f.exists());}}
    private String zip(java.util.zip.ZipFile z,String name)throws IOException{var e=z.getEntry(name);assertNotNull(e);try(var in=z.getInputStream(e)){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
    private String url(String p){return "http://127.0.0.1:"+port+p;}private long roomId(int i){return BASE+1000+i;}private long customerId(int i){return BASE+2000+i;}private long userId(int i){return BASE+100+i;}private long employeeId(int i){return BASE+200+i;}private long roleId(int i){return BASE+300+i;}private static void write(OutputStream o,String s)throws IOException{o.write(s.getBytes(StandardCharsets.UTF_8));}
    @FunctionalInterface interface HookAction{void run(int index,RoomCustomerRelationImportRow row);}
    @TestConfiguration static class HookConfig{@Bean RoomCustomerRelationImportCommitHook hook(){return new RoomCustomerRelationImportCommitHook(){@Override public void beforeCommit(Long p){Runnable r=BEFORE_COMMIT.getAndSet(null);if(r!=null)r.run();}@Override public void beforeInsert(int i,RoomCustomerRelationImportRow r){HookAction a=BEFORE_INSERT.get();if(a!=null)a.run(i,r);}};}}
}
