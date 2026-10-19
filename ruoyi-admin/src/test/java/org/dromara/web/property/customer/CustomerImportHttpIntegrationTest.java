package org.dromara.web.property.customer;

import org.dromara.DromaraApplication;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.importer.domain.CustomerImportRow;
import org.dromara.property.customer.importer.service.CustomerImportCommitHook;
import org.dromara.property.customer.security.CustomerIdentityFingerprintService;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Customer Import 真实 HTTP、Sa-Token、安全链与 MySQL 8 验收。 */
@EnabledIfEnvironmentVariable(named="ZBM_HTTP_TEST_ENABLED",matches="true")
@ResourceLock("zbm-http-integration-db")
@SpringBootTest(classes=DromaraApplication.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
    "spring.flyway.enabled=false",
    "captcha.enable=false","api-decrypt.enabled=false",
    "spring.datasource.dynamic.datasource.master.url=${ZBM_TEST_DB_URL}",
    "spring.datasource.dynamic.datasource.master.username=${ZBM_TEST_DB_USER}",
    "spring.datasource.dynamic.datasource.master.password=${ZBM_TEST_DB_PASSWORD}",
    "mybatis-encryptor.enable=true","mybatis-encryptor.algorithm=AES",
    "mybatis-encryptor.password=${ZBM_TEST_AES_KEY}","property.customer.security.enabled=true",
    "property.customer.security.hmac-key-base64=${ZBM_TEST_HMAC_KEY_BASE64}","property.customer.security.key-version=customer-import-test"})
@Import(CustomerImportHttpIntegrationTest.HookConfig.class)
class CustomerImportHttpIntegrationTest {
    private static final long BASE=914014100000000000L,MENU_IMPORT=BASE+900,MENU_ADD=BASE+901;
    private static final String HASH="$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";
    private static final AtomicReference<Runnable> BEFORE_COMMIT=new AtomicReference<>();
    private static final AtomicReference<java.util.function.BiConsumer<Integer,Customer>> BEFORE_INSERT=new AtomicReference<>();
    @LocalServerPort int port; @Autowired JdbcTemplate jdbc;
    private final HttpClient http=HttpClient.newHttpClient();private final Map<String,String> tokens=new HashMap<>();

    @BeforeEach void setUp(){assertEquals("zbm_property_test",jdbc.queryForObject("SELECT DATABASE()",String.class));cleanup();ensureMenu(MENU_IMPORT,"property:customer:import");ensureMenu(MENU_ADD,"property:customer:add");seed("allowed",10,true,false,true,false);seed("addOnly",20,false,true,true,false);seed("permissionOnly",30,true,false,false,false);seed("resourceOnly",40,false,false,false,false);seed("management",50,false,false,false,true);}
    @AfterEach void cleanup(){BEFORE_COMMIT.set(null);BEFORE_INSERT.set(null);jdbc.update("DELETE FROM pm_customer WHERE customer_name LIKE 'T14C%'");jdbc.update("DELETE FROM pm_customer_management_grant WHERE principal_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM pm_project_management_grant WHERE principal_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM sys_role_menu WHERE role_id BETWEEN ? AND ?",BASE+300,BASE+399);jdbc.update("DELETE FROM sys_user_role WHERE user_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM sys_role WHERE role_id BETWEEN ? AND ?",BASE+300,BASE+399);jdbc.update("DELETE FROM sys_user WHERE user_id BETWEEN ? AND ?",BASE+100,BASE+199);jdbc.update("DELETE FROM sys_menu WHERE menu_id IN (?,?)",MENU_IMPORT,MENU_ADD);jdbc.update("DELETE FROM sys_login_info WHERE user_name LIKE 't14c_%'");tokens.clear();}

    @Test void mysql01_normal100(){List<CustomerImportRow> rows=new ArrayList<>();for(int i=0;i<100;i++)rows.add(row(i,true));assertSuccess(importRows("admin",rows),100);assertEquals(100,count());}
    @Test void mysql02_invalidRow50(){List<CustomerImportRow> rows=new ArrayList<>();for(int i=0;i<100;i++)rows.add(row(i,true));rows.get(49).setCustomerName("");assertFailure(importRows("admin",rows),100);assertEquals(0,count());}
    @Test void mysql03_duplicateInsideExcel(){List<CustomerImportRow> rows=new ArrayList<>();for(int i=0;i<100;i++)rows.add(row(i,true));rows.get(79).setIdNo(rows.get(9).getIdNo().toLowerCase());assertFailure(importRows("admin",rows),100);assertEquals(0,count());}
    @Test void mysql04_existingActive(){insertExisting("ACTIVE","0","DUP-A");assertFailure(importRows("admin",List.of(identityRow("DUP-A"))),1);}
    @Test void mysql05_inactiveStillConflicts(){insertExisting("INACTIVE","0","DUP-I");assertFailure(importRows("admin",List.of(identityRow("DUP-I"))),1);}
    @Test void mysql06_cancelledStillConflicts(){insertExisting("CANCELLED","0","DUP-C");assertFailure(importRows("admin",List.of(identityRow("DUP-C"))),1);}
    @Test void mysql07_logicalDeleteReleases(){insertExisting("ACTIVE","1","REUSE");assertSuccess(importRows("admin",List.of(identityRow("REUSE"))),1);assertEquals(2,countAll());}
    @Test void mysql08_multipleWithoutIdentity(){assertSuccess(importRows("admin",List.of(row(1,false),row(2,false))),2);assertEquals(2,count());}
    @Test void mysql09_aesCiphertext(){String plain="AES-CHECK";assertSuccess(importRows("admin",List.of(identityRow(plain))),1);String stored=jdbc.queryForObject("SELECT id_no FROM pm_customer WHERE customer_name='T14C-AES-CHECK' AND del_flag='0'",String.class);assertTrue(stored.startsWith("ENC_"));assertNotEquals(plain,stored);}
    @Test void mysql10_fingerprint(){String value="ＦＰ １２3";assertSuccess(importRows("admin",List.of(identityRow(value))),1);byte[] actual=jdbc.queryForObject("SELECT identity_fingerprint FROM pm_customer WHERE customer_name='T14C-ＦＰ １２3'",byte[].class);assertArrayEquals(fingerprint("ID_CARD","FP123"),actual);}
    @Test void mysql11_validateThenConcurrentIdentity(){BEFORE_COMMIT.set(()->insertExistingSeparate("ACTIVE","0","RACE"));assertFailure(importRows("admin",List.of(identityRow("SAFE"),identityRow("RACE"))),2);assertEquals(1,count());assertEquals("T14C-DB-RACE",jdbc.queryForObject("SELECT customer_name FROM pm_customer WHERE customer_name LIKE 'T14C%' AND del_flag='0'",String.class));}
    @Test void mysql12_midWriteFailureRollsBack(){BEFORE_INSERT.set((i,c)->{if(i==79)jdbc.update("INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,status,lifecycle_status,version,del_flag) VALUES(?,?,?,?, '0','ACTIVE',0,'0')",c.getId(),"FORCE-DUP","PERSON","T14C-FORCE");});List<CustomerImportRow> rows=new ArrayList<>();for(int i=0;i<100;i++)rows.add(row(i,false));assertFailure(importRows("admin",rows),100);assertEquals(0,count());}

    @Test void httpA_importAndGrantAllowed(){assertSuccess(importRows("allowed",List.of(row(1,false))),1);}
    @Test void httpB_addWithoutImportDenied(){assertDenied(importRows("addOnly",List.of(row(1,false))));}
    @Test void httpC_importWithoutGrantDenied(){assertDenied(importRows("permissionOnly",List.of(row(1,false))));}
    @Test void httpD_resourceWriteOnlyDenied(){assertDenied(importRows("resourceOnly",List.of(row(1,false))));}
    @Test void httpE_managementOnlyDenied(){assertDenied(importRows("management",List.of(row(1,false))));}
    @Test void httpF_permissionRevocationInvalidatesOldToken(){String t=token("allowed");assertSuccess(importRowsWithToken(t,List.of(row(1,false))),1);jdbc.update("DELETE FROM sys_role_menu WHERE role_id=? AND menu_id=?",roleId(10),MENU_IMPORT);assertDenied(importRowsWithToken(t,List.of(row(2,false))));}
    @Test void httpG_grantRevocationInvalidatesOldToken(){String t=token("allowed");assertSuccess(importRowsWithToken(t,List.of(row(1,false))),1);jdbc.update("DELETE FROM pm_customer_management_grant WHERE principal_id=?",userId(10));assertDenied(importRowsWithToken(t,List.of(row(2,false))));}
    @Test void httpPiiFailureResponseNeverLeaksIdentity(){String secret="SECRET-ID-9988";insertExisting("ACTIVE","0",secret);HttpResponse<String> r=importRows("allowed",List.of(identityRow(secret)));assertFalse(r.body().contains(secret));assertFalse(r.body().contains("ENC_"));assertFalse(r.body().contains(System.getenv("ZBM_TEST_AES_KEY")));}
    @Test void templateHasExactSafeColumns()throws Exception{HttpResponse<byte[]> r=http.send(auth("allowed","/property/customer/import/template").GET().build(),HttpResponse.BodyHandlers.ofByteArray());assertEquals(200,r.statusCode());List<CustomerImportRow> rows=ExcelBuilder.read(new ByteArrayInputStream(r.body()),CustomerImportRow.class).validate(false).doReadSync();assertEquals(1,rows.size());assertEquals("示例客户",rows.getFirst().getCustomerName());String raw=new String(r.body(),StandardCharsets.ISO_8859_1);assertFalse(raw.contains("identityFingerprint"));assertFalse(raw.contains("customerNo"));}

    private HttpResponse<String> importRows(String user,List<CustomerImportRow> rows){return importRowsWithToken(token(user),rows);}private HttpResponse<String> importRowsWithToken(String token,List<CustomerImportRow> rows){try{byte[] xlsx=excel(rows);String b="----T14C"+UUID.randomUUID();ByteArrayOutputStream body=new ByteArrayOutputStream();write(body,"--"+b+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"import.xlsx\"\r\nContent-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n");body.write(xlsx);write(body,"\r\n--"+b+"--\r\n");return http.send(HttpRequest.newBuilder(URI.create(url("/property/customer/import"))).header("Authorization","Bearer "+token).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e").header("Content-Type","multipart/form-data; boundary="+b).POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());}catch(Exception e){throw new AssertionError(e);}}
    private byte[] excel(List<CustomerImportRow> rows){try(ByteArrayOutputStream out=new ByteArrayOutputStream()){ExcelBuilder.of(rows,CustomerImportRow.class).sheetName("客户导入").toStream(out);return out.toByteArray();}catch(IOException e){throw new UncheckedIOException(e);}}
    @SuppressWarnings("unchecked")private Map<String,Object> data(HttpResponse<String> r){return (Map<String,Object>)JsonUtils.parseMap(r.body()).get("data");}
    private void assertSuccess(HttpResponse<String> r,int n){assertEquals(200,r.statusCode(),r.body());Map<String,Object>d=data(r);assertNotNull(d,r.body());assertEquals(n,((Number)d.get("successRows")).intValue());assertEquals(0,((Number)d.get("failedRows")).intValue());}
    private void assertFailure(HttpResponse<String> r,int n){assertEquals(200,r.statusCode(),r.body());Map<String,Object>d=data(r);assertNotNull(d,r.body());assertEquals(0,((Number)d.get("successRows")).intValue());assertEquals(n,((Number)d.get("failedRows")).intValue());}
    private void assertDenied(HttpResponse<String> r){Object c=JsonUtils.parseMap(r.body()).get("code");assertTrue(r.statusCode()!=200||c==null||((Number)c).intValue()!=200,r.body());}
    private CustomerImportRow row(int i,boolean identity){CustomerImportRow r=new CustomerImportRow();r.setCustomerType("PERSON");r.setCustomerName("T14C-"+i);r.setGender("0");r.setBirthDate(LocalDate.of(1990,1,1));r.setMobile("1380000"+String.format("%04d",i));r.setEmail("t14c"+i+"@example.invalid");if(identity){r.setIdType("ID_CARD");r.setIdNo("T14C-ID-"+i);}return r;}
    private CustomerImportRow identityRow(String id){CustomerImportRow r=row(Math.abs(id.hashCode()%10000),false);r.setCustomerName("T14C-"+id);r.setIdType("ID_CARD");r.setIdNo(id);return r;}
    private int count(){return jdbc.queryForObject("SELECT COUNT(*) FROM pm_customer WHERE customer_name LIKE 'T14C%' AND del_flag='0'",Integer.class);}private int countAll(){return jdbc.queryForObject("SELECT COUNT(*) FROM pm_customer WHERE customer_name LIKE 'T14C%'",Integer.class);}
    private byte[] fingerprint(String type,String no){try{Mac m=Mac.getInstance("HmacSHA256");m.init(new SecretKeySpec(Base64.getDecoder().decode(System.getenv("ZBM_TEST_HMAC_KEY_BASE64")),"HmacSHA256"));return m.doFinal((type+'\0'+no).getBytes(StandardCharsets.UTF_8));}catch(Exception e){throw new AssertionError(e);}}
    private void insertExisting(String life,String del,String id){String normalized=id.strip().toUpperCase();long key=Math.abs(UUID.randomUUID().getMostSignificantBits());jdbc.update("INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,id_type,id_no,identity_fingerprint,pii_key_version,lifecycle_status,status,version,del_flag) VALUES(?,?,?,?,?,'ENC_dummy',?,'test',?, '0',0,?)",key,"T"+key,"PERSON","T14C-DB-"+id,"ID_CARD",fingerprint("ID_CARD",normalized),life,del);}
    private void insertExistingSeparate(String life,String del,String id){long key=Math.abs(UUID.randomUUID().getMostSignificantBits());try(var c=DriverManager.getConnection(System.getenv("ZBM_TEST_DB_URL"),System.getenv("ZBM_TEST_DB_USER"),System.getenv("ZBM_TEST_DB_PASSWORD"));var s=c.prepareStatement("INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,id_type,id_no,identity_fingerprint,pii_key_version,lifecycle_status,status,version,del_flag) VALUES(?,?,?,?,?,'ENC_dummy',?,'test',?,'0',0,?)")){s.setLong(1,key);s.setString(2,"T"+key);s.setString(3,"PERSON");s.setString(4,"T14C-DB-"+id);s.setString(5,"ID_CARD");s.setBytes(6,fingerprint("ID_CARD",id));s.setString(7,life);s.setString(8,del);s.executeUpdate();}catch(Exception e){throw new AssertionError(e);}}
    private void ensureMenu(long id,String perm){jdbc.update("INSERT INTO sys_menu(menu_id,parent_id,menu_name,order_num,path,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time) VALUES(?,2609252000000000401,?,99,'','N','N','F','0','0',?,'#',1,NOW())",id,"T14C-"+perm,perm);}
    private void seed(String name,int o,boolean imp,boolean add,boolean grant,boolean management){long u=userId(o),r=roleId(o);jdbc.update("INSERT INTO sys_user(user_id,user_name,nick_name,user_type,password,status,del_flag,create_time) VALUES(?,?,?,'sys_user',?,'0','0',NOW())",u,"t14c_"+name,name,HASH);jdbc.update("INSERT INTO sys_role(role_id,role_name,role_key,role_sort,data_scope,status,del_flag,create_time) VALUES(?,?,?,1,'1','0','0',NOW())",r,"t14c_"+name,"t14c_"+name);jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(?,?)",u,r);if(imp)jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(?,?)",r,MENU_IMPORT);if(add)jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(?,?)",r,MENU_ADD);if(grant)jdbc.update("INSERT INTO pm_customer_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,'USER',?,'ENTERPRISE','0',0,'0')",BASE+700+o,u);if(management)jdbc.update("INSERT INTO pm_project_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,'USER',?,'ALL','0',0,'0')",BASE+800+o,u);}
    @SuppressWarnings("unchecked")private String token(String n){return tokens.computeIfAbsent(n,x->{String user="admin".equals(x)?"admin":"t14c_"+x;try{HttpResponse<String> r=http.send(HttpRequest.newBuilder(URI.create(url("/auth/login"))).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of("clientId","e5cd7e4891bf95d1d19206ce24a7b32e","grantType","password","username",user,"password","admin123")))).build(),HttpResponse.BodyHandlers.ofString());return String.valueOf(((Map<String,Object>)JsonUtils.parseMap(r.body()).get("data")).get("access_token"));}catch(Exception e){throw new AssertionError(e);}});}
    private HttpRequest.Builder auth(String u,String p){return HttpRequest.newBuilder(URI.create(url(p))).header("Authorization","Bearer "+token(u)).header("clientid","e5cd7e4891bf95d1d19206ce24a7b32e");}private String url(String p){return"http://127.0.0.1:"+port+p;}private long userId(int o){return BASE+100+o;}private long roleId(int o){return BASE+300+o;}private static void write(OutputStream o,String v)throws IOException{o.write(v.getBytes(StandardCharsets.UTF_8));}
    @TestConfiguration static class HookConfig{@Bean CustomerImportCommitHook hook(){return new CustomerImportCommitHook(){public void beforeCommit(){Runnable r=BEFORE_COMMIT.getAndSet(null);if(r!=null)r.run();}public void beforeInsert(int i,Customer c){var h=BEFORE_INSERT.get();if(h!=null)h.accept(i,c);}};}}
}
