package org.dromara.web.customer;

import org.dromara.DromaraApplication;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.encrypt.utils.EncryptUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.property.customer.domain.vo.CustomerMaskedExportVo;
import org.dromara.property.customer.domain.vo.CustomerSensitiveExportVo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** 真实 HTTP、Sa-Token、Service、Mapper 与隔离 MySQL 的 Customer 安全验收。 */
@EnabledIfEnvironmentVariable(named = "ZBM_HTTP_TEST_ENABLED", matches = "true")
@ResourceLock("zbm-http-integration-db")
@SpringBootTest(classes = DromaraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.flyway.enabled=false",
        "captcha.enable=false",
        "api-decrypt.enabled=false",
        "spring.datasource.dynamic.datasource.master.url=${ZBM_TEST_DB_URL}",
        "spring.datasource.dynamic.datasource.master.username=${ZBM_TEST_DB_USER}",
        "spring.datasource.dynamic.datasource.master.password=${ZBM_TEST_DB_PASSWORD}",
        "mybatis-encryptor.enable=true",
        "mybatis-encryptor.algorithm=AES",
        "mybatis-encryptor.password=${ZBM_TEST_AES_KEY}",
        "property.customer.security.enabled=true",
        "property.customer.security.hmac-key-base64=${ZBM_TEST_HMAC_KEY_BASE64}",
        "property.customer.security.key-version=http-test-v1"
    })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerHttpSecurityIntegrationTest {
    private static final long BASE = 912013000000000000L;
    private static final long CUSTOMER_ID = BASE + 1;
    private static final String PASSWORD_HASH = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";
    private static final String RAW_ID_NO = "110101199001011234";
    private static final String RAW_MOBILE = "13800138000";

    @LocalServerPort private int port;
    @Autowired private JdbcTemplate jdbc;
    private final HttpClient http = HttpClient.newHttpClient();
    private final Map<String, String> tokens = new HashMap<>();

    @BeforeEach
    void prepare() {
        cleanup();
        seedCustomer();
        seedIdentity("direct", 10, false, true, false);
        seedIdentity("rolegrant", 20, true, false, false);
        seedIdentity("nogrant", 30, false, false, false);
        seedIdentity("management", 40, false, false, false);
        seedIdentity("piionly", 50, false, false, true);
        seedIdentity("sensitive", 60, false, true, false);
        seedIdentity("exportnogrant", 70, false, false, false);
        seedIdentity("viewwithgrant", 80, false, true, true);
        grantPiiExportToRole(60);
        grantPiiExportToRole(70);
        jdbc.update("INSERT INTO pm_project_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,?,?,?, '0',0,'0')",
            BASE + 900, "USER", userId(40), "ALL");
    }

    @AfterEach
    void cleanup() {
        jdbc.execute("DROP TRIGGER IF EXISTS trg_customer_http_audit_fail");
        jdbc.update("DELETE FROM pm_customer_sensitive_access_audit WHERE customer_id=? OR operator_id BETWEEN ? AND ? OR trace_id=?",
            CUSTOMER_ID, BASE + 100, BASE + 299, "customer-export-http-test");
        jdbc.update("DELETE FROM pm_customer_management_grant WHERE principal_id BETWEEN ? AND ?", BASE + 100, BASE + 399);
        jdbc.update("DELETE FROM pm_project_management_grant WHERE principal_id BETWEEN ? AND ?", BASE + 100, BASE + 299);
        jdbc.update("DELETE FROM sys_role_menu WHERE role_id BETWEEN ? AND ?", BASE + 300, BASE + 399);
        jdbc.update("DELETE FROM sys_user_role WHERE user_id BETWEEN ? AND ?", BASE + 100, BASE + 299);
        jdbc.update("DELETE FROM sys_role WHERE role_id BETWEEN ? AND ?", BASE + 300, BASE + 399);
        jdbc.update("DELETE FROM sys_user WHERE user_id BETWEEN ? AND ?", BASE + 100, BASE + 299);
        jdbc.update("DELETE FROM sys_login_info WHERE user_name LIKE 'c12\\_%'");
        jdbc.update("DELETE FROM pm_customer WHERE customer_name LIKE 'HTTP-WRITE-%'");
        jdbc.update("DELETE FROM pm_customer WHERE id=?", CUSTOMER_ID);
        tokens.clear();
    }

    @Test
    @Order(1)
    void directAndRoleGrantCanReadButOtherScopesCannot() {
        assertBusinessSuccess(get("admin", "/property/customer/" + CUSTOMER_ID));
        assertBusinessSuccess(get("direct", "/property/customer/list?pageNum=1&pageSize=10"));
        assertBusinessSuccess(get("rolegrant", "/property/customer/" + CUSTOMER_ID));
        assertBusinessDenied(get("nogrant", "/property/customer/" + CUSTOMER_ID));
        assertBusinessDenied(get("management", "/property/customer/list?pageNum=1&pageSize=10"));
    }

    @Test
    @Order(2)
    void ordinaryResponsesAreMaskedAndPiiRequiresBothGrantAndPermission() {
        String ordinary = get("direct", "/property/customer/" + CUSTOMER_ID).body();
        assertFalse(ordinary.contains(RAW_ID_NO));
        assertFalse(ordinary.contains(RAW_MOBILE));
        assertFalse(ordinary.contains("identityFingerprint"));
        assertFalse(ordinary.contains("piiKeyVersion"));

        assertBusinessDenied(get("piionly", "/property/customer/" + CUSTOMER_ID + "/sensitive"));
        grantPiiToRole(10);
        String sensitive = get("direct", "/property/customer/" + CUSTOMER_ID + "/sensitive").body();
        assertTrue(sensitive.contains(RAW_ID_NO));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM pm_customer_sensitive_access_audit WHERE customer_id=? AND decision='ALLOW'", Integer.class, CUSTOMER_ID));
    }

    @Test
    @Order(3)
    void revokedGrantAndDisabledRoleRejectOriginalTokenImmediately() {
        assertBusinessSuccess(get("direct", "/property/customer/" + CUSTOMER_ID));
        jdbc.update("UPDATE pm_customer_management_grant SET del_flag='1' WHERE principal_type='USER' AND principal_id=?", userId(10));
        assertBusinessDenied(get("direct", "/property/customer/" + CUSTOMER_ID));

        assertBusinessSuccess(get("rolegrant", "/property/customer/" + CUSTOMER_ID));
        jdbc.update("UPDATE sys_role SET status='1' WHERE role_id=?", roleId(20));
        assertBusinessDenied(get("rolegrant", "/property/customer/" + CUSTOMER_ID));
    }

    @Test
    @Order(4)
    void auditFailurePreventsSensitivePayload() {
        grantPiiToRole(10);
        jdbc.execute("CREATE TRIGGER trg_customer_http_audit_fail BEFORE INSERT ON pm_customer_sensitive_access_audit FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='forced audit failure'");
        HttpResponse<String> response = get("direct", "/property/customer/" + CUSTOMER_ID + "/sensitive");
        assertFalse(response.body().contains(RAW_ID_NO));
        Number code = (Number) JsonUtils.parseMap(response.body()).get("code");
        assertTrue(response.statusCode() != 200 || code == null || code.intValue() != 200);
    }

    @Test
    @Order(5)
    void writeApisUseGrantEncryptionOptimisticLockAndImmediateRevocation() {
        Map<String, Object> create = new LinkedHashMap<>();
        create.put("customerType", "PERSON");
        create.put("customerName", "HTTP-WRITE-张三");
        create.put("mobile", "13900139000");
        create.put("idType", "ID_CARD");
        create.put("idNo", "110101199202023456");

        assertBusinessDenied(json("nogrant", "POST", "/property/customer", create));
        assertBusinessDenied(json("management", "POST", "/property/customer", create));
        assertBusinessDenied(json("nogrant", "PUT", "/property/customer",
            Map.of("id", CUSTOMER_ID, "version", 0, "customerName", "HTTP-WRITE-越权")));
        assertBusinessDenied(json("management", "PUT", "/property/customer",
            Map.of("id", CUSTOMER_ID, "version", 0, "customerName", "HTTP-WRITE-管理越权")));
        HttpResponse<String> createdResponse = json("direct", "POST", "/property/customer", create);
        assertBusinessSuccess(createdResponse);
        @SuppressWarnings("unchecked")
        Map<String, Object> created = (Map<String, Object>) JsonUtils.parseMap(createdResponse.body()).get("data");
        long id = Long.parseLong(String.valueOf(created.get("id")));
        assertFalse(createdResponse.body().contains("110101199202023456"));
        String stored = jdbc.queryForObject("SELECT id_no FROM pm_customer WHERE id=?", String.class, id);
        assertNotNull(stored);
        assertTrue(stored.startsWith(Constants.ENCRYPT_HEADER));
        assertEquals(32, Objects.requireNonNull(jdbc.queryForObject(
            "SELECT OCTET_LENGTH(identity_fingerprint) FROM pm_customer WHERE id=?", Integer.class, id)));

        Map<String, Object> update = Map.of("id", id, "version", 0,
            "customerName", "HTTP-WRITE-李四", "mobile", "13900139001");
        assertBusinessSuccess(json("direct", "PUT", "/property/customer", update));
        assertBusinessDenied(json("direct", "PUT", "/property/customer", update));
        assertEquals("110101199202023456", getDecryptedIdNo(id));

        assertBusinessSuccess(json("direct", "PUT", "/property/customer/changeStatus",
            Map.of("id", id, "version", 1, "lifecycleStatus", "INACTIVE")));
        assertEquals("1:INACTIVE", jdbc.queryForObject(
            "SELECT CONCAT(status, ':', lifecycle_status) FROM pm_customer WHERE id=?", String.class, id));

        Map<String, Object> identityless = Map.of("customerType", "PERSON", "customerName", "HTTP-WRITE-可删除");
        HttpResponse<String> deletableResponse = json("direct", "POST", "/property/customer", identityless);
        assertBusinessSuccess(deletableResponse);
        @SuppressWarnings("unchecked")
        Map<String, Object> deletable = (Map<String, Object>) JsonUtils.parseMap(deletableResponse.body()).get("data");
        long deletableId = Long.parseLong(String.valueOf(deletable.get("id")));
        assertBusinessSuccess(delete("direct", "/property/customer/" + deletableId + "?version=0"));
        assertEquals("1", jdbc.queryForObject("SELECT del_flag FROM pm_customer WHERE id=?", String.class, deletableId));

        jdbc.update("UPDATE pm_customer_management_grant SET del_flag='1' WHERE principal_type='USER' AND principal_id=?", userId(10));
        assertBusinessDenied(json("direct", "PUT", "/property/customer/changeStatus",
            Map.of("id", id, "version", 2, "lifecycleStatus", "ACTIVE")));
    }

    @Test
    @Order(6)
    void maskedAndSensitiveExcelExportsEnforceIndependentPermissionsAndContent() {
        HttpResponse<byte[]> masked = export("direct", "/property/customer/export?customerNo=HTTP-CUSTOMER");
        assertEquals(200, masked.statusCode());
        assertTrue(masked.headers().firstValue("Content-Type").orElse("").contains("spreadsheetml"));
        List<CustomerMaskedExportVo> maskedRows = ExcelBuilder.read(
                new ByteArrayInputStream(masked.body()), CustomerMaskedExportVo.class)
            .validate(false).headRowNumber(1).doReadSync();
        assertEquals(1, maskedRows.size());
        assertNotEquals(RAW_ID_NO, maskedRows.getFirst().getIdNo());
        assertNotEquals(RAW_MOBILE, maskedRows.getFirst().getMobile());
        assertInternalSecurityColumnsAbsent(masked.body());

        assertExportDenied(export("direct", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER"));
        assertExportDenied(export("piionly", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER"));
        assertExportDenied(export("exportnogrant", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER"));
        assertExportDenied(export("viewwithgrant", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER"));
        assertExportDenied(export("management", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER"));

        HttpResponse<byte[]> sensitive = export("sensitive", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER");
        assertEquals(200, sensitive.statusCode());
        List<CustomerSensitiveExportVo> sensitiveRows = ExcelBuilder.read(
                new ByteArrayInputStream(sensitive.body()), CustomerSensitiveExportVo.class)
            .validate(false).headRowNumber(1).doReadSync();
        assertEquals(1, sensitiveRows.size());
        assertEquals(RAW_ID_NO, sensitiveRows.getFirst().getIdNo());
        assertEquals(RAW_MOBILE, sensitiveRows.getFirst().getMobile());
        assertInternalSecurityColumnsAbsent(sensitive.body());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM pm_customer_sensitive_access_audit " +
            "WHERE operator_id=? AND access_action='PII_EXPORT' AND decision='ALLOW'", Integer.class, userId(60)));

        HttpResponse<byte[]> adminExport = export("admin", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER");
        assertEquals(200, adminExport.statusCode());
        Long adminUserId = jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name='admin'", Long.class);
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM pm_customer_sensitive_access_audit " +
            "WHERE operator_id=? AND access_action='PII_EXPORT' AND decision='ALLOW'", Integer.class,
            adminUserId) > 0);
    }

    @Test
    @Order(7)
    void sensitiveExportRevocationAndAuditFailureFailClosedForOriginalToken() {
        assertEquals(200, export("sensitive", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER").statusCode());
        jdbc.update("UPDATE pm_customer_management_grant SET del_flag='1' WHERE principal_type='USER' AND principal_id=?",
            userId(60));
        assertExportDenied(export("sensitive", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER"));

        grantPiiExportToRole(80);
        assertEquals(200, export("viewwithgrant",
            "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER").statusCode());
        jdbc.update("UPDATE sys_role SET status='1' WHERE role_id=?", roleId(80));
        assertExportDenied(export("viewwithgrant",
            "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER"));

        jdbc.execute("CREATE TRIGGER trg_customer_http_audit_fail BEFORE INSERT ON pm_customer_sensitive_access_audit " +
            "FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='forced audit failure'");
        HttpResponse<byte[]> failed = export("admin", "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER");
        assertExportDenied(failed);
        assertFalse(new String(failed.body(), java.nio.charset.StandardCharsets.UTF_8).contains(RAW_ID_NO));
    }

    @Test
    @Order(8)
    void malformedCiphertextCannotProduceSuccessfulSensitiveWorkbook() {
        jdbc.update("UPDATE pm_customer SET id_no='ENC_invalid-ciphertext' WHERE id=?", CUSTOMER_ID);

        HttpResponse<byte[]> response = export("admin",
            "/property/customer/export-sensitive?customerNo=HTTP-CUSTOMER");

        assertExportDenied(response);
        assertFalse(new String(response.body(), java.nio.charset.StandardCharsets.UTF_8).contains("invalid-ciphertext"));
    }

    private void seedCustomer() {
        String encrypted = Constants.ENCRYPT_HEADER + EncryptUtils.encryptByAes(RAW_ID_NO, System.getenv("ZBM_TEST_AES_KEY"));
        jdbc.update("INSERT INTO pm_customer(id,customer_no,customer_type,customer_name,mobile,phone,email,id_type,id_no,pii_key_version,lifecycle_status,status,del_flag) VALUES(?,?,?,?,?,?,?,?,?,?,'ACTIVE','0','0')",
            CUSTOMER_ID, "HTTP-CUSTOMER", "PERSON", "张三", RAW_MOBILE, "01012345678", "customer@example.com", "ID_CARD", encrypted, "http-test-v1");
    }

    private void seedIdentity(String name, int offset, boolean roleGrant, boolean userGrant, boolean pii) {
        long user = userId(offset), role = roleId(offset);
        jdbc.update("INSERT INTO sys_user(user_id,user_name,nick_name,user_type,password,status,del_flag,create_time) VALUES(?,?,?,'sys_user',?,'0','0',NOW())", user, "c12_" + name, name, PASSWORD_HASH);
        jdbc.update("INSERT INTO sys_role(role_id,role_name,role_key,role_sort,data_scope,status,del_flag,create_time) VALUES(?,?,?,1,'1','0','0',NOW())", role, "c12_" + name, "c12_" + name);
        jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(?,?)", user, role);
        addPermission(role, "property:customer:list");
        addPermission(role, "property:customer:query");
        addPermission(role, "property:customer:add");
        addPermission(role, "property:customer:edit");
        addPermission(role, "property:customer:remove");
        addPermission(role, "property:customer:export");
        if (pii) addPermission(role, "property:customer:pii:view");
        if (userGrant) insertGrant("USER", user);
        if (roleGrant) insertGrant("ROLE", role);
    }

    private void grantPiiToRole(int offset) {
        addPermission(roleId(offset), "property:customer:pii:view");
        tokens.remove(offset == 10 ? "direct" : "rolegrant");
    }

    private void grantPiiExportToRole(int offset) {
        addPermission(roleId(offset), "property:customer:pii:export");
        tokens.clear();
    }

    private void addPermission(long role, String permission) {
        Long menu = jdbc.queryForObject("SELECT menu_id FROM sys_menu WHERE perms=? AND status='0' LIMIT 1", Long.class, permission);
        jdbc.update("INSERT IGNORE INTO sys_role_menu(role_id,menu_id) VALUES(?,?)", role, menu);
    }

    private void insertGrant(String type, long principal) {
        jdbc.update("INSERT INTO pm_customer_management_grant(id,principal_type,principal_id,scope_type,status,version,del_flag) VALUES(?,?,?,'ENTERPRISE','0',0,'0')", Math.abs(new Random().nextLong()), type, principal);
    }

    @SuppressWarnings("unchecked")
    private String login(String name) {
        String username = "admin".equals(name) ? "admin" : "c12_" + name;
        Map<String, Object> body = Map.of("clientId", "e5cd7e4891bf95d1d19206ce24a7b32e", "grantType", "password", "username", username, "password", "admin123");
        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/auth/login")))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(body))).build();
        HttpResponse<String> response = send(request);
        assertEquals(200, response.statusCode());
        Map<String, Object> responseBody = JsonUtils.parseMap(response.body());
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        assertNotNull(data, "登录响应必须包含 token 数据");
        return String.valueOf(data.get("access_token"));
    }

    private HttpResponse<String> get(String identity, String path) {
        String token = tokens.computeIfAbsent(identity, this::login);
        return send(HttpRequest.newBuilder(URI.create(url(path)))
            .header("Authorization", "Bearer " + token)
            .header("clientid", "e5cd7e4891bf95d1d19206ce24a7b32e")
            .GET().build());
    }

    private HttpResponse<String> json(String identity, String method, String path, Object body) {
        String token = tokens.computeIfAbsent(identity, this::login);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path)))
            .header("Authorization", "Bearer " + token)
            .header("clientid", "e5cd7e4891bf95d1d19206ce24a7b32e")
            .header("Content-Type", "application/json");
        return send(builder.method(method, HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(body))).build());
    }

    private HttpResponse<String> delete(String identity, String path) {
        String token = tokens.computeIfAbsent(identity, this::login);
        return send(HttpRequest.newBuilder(URI.create(url(path)))
            .header("Authorization", "Bearer " + token)
            .header("clientid", "e5cd7e4891bf95d1d19206ce24a7b32e")
            .DELETE().build());
    }

    private HttpResponse<byte[]> export(String identity, String path) {
        String token = tokens.computeIfAbsent(identity, this::login);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url(path)))
            .header("Authorization", "Bearer " + token)
            .header("clientid", "e5cd7e4891bf95d1d19206ce24a7b32e")
            .header("X-Trace-Id", "customer-export-http-test")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.noBody()).build();
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (Exception e) {
            throw new AssertionError("HTTP export request failed", e);
        }
    }

    private void assertExportDenied(HttpResponse<byte[]> response) {
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertFalse(contentType.contains("spreadsheetml"), "拒绝响应不得返回 Excel");
        String body = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(body.contains(RAW_ID_NO));
        if (response.statusCode() == 200 && !body.isBlank()) {
            Number code = (Number) JsonUtils.parseMap(body).get("code");
            assertTrue(code == null || code.intValue() != 200, body);
        }
    }

    private void assertInternalSecurityColumnsAbsent(byte[] workbookBytes) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(workbookBytes))) {
            var header = workbook.getSheetAt(0).getRow(0);
            List<String> names = new ArrayList<>();
            header.forEach(cell -> names.add(cell.getStringCellValue()));
            String joined = String.join(",", names).toLowerCase(Locale.ROOT);
            assertFalse(joined.contains("fingerprint"));
            assertFalse(joined.contains("pii_key_version"));
            assertFalse(joined.contains("审计"));
            assertFalse(joined.contains("授权"));
        } catch (Exception e) {
            throw new AssertionError("导出文件必须是可读取的 XLSX", e);
        }
    }

    private String getDecryptedIdNo(long id) {
        String encrypted = jdbc.queryForObject("SELECT id_no FROM pm_customer WHERE id=?", String.class, id);
        assertNotNull(encrypted);
        return EncryptUtils.decryptByAes(encrypted.substring(Constants.ENCRYPT_HEADER.length()), System.getenv("ZBM_TEST_AES_KEY"));
    }

    private void assertBusinessDenied(HttpResponse<String> response) {
        String body = response.body();
        assertFalse(body.contains(RAW_ID_NO));
        Number code = (Number) JsonUtils.parseMap(body).get("code");
        assertTrue(response.statusCode() != 200 || code == null || code.intValue() != 200, body);
    }

    private void assertBusinessSuccess(HttpResponse<String> response) {
        assertEquals(200, response.statusCode());
        Number code = (Number) JsonUtils.parseMap(response.body()).get("code");
        assertNotNull(code, response.body());
        assertEquals(200, code.intValue(), response.body());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new AssertionError("HTTP request failed", e);
        }
    }

    private String url(String path) { return "http://127.0.0.1:" + port + path; }
    private long userId(int offset) { return BASE + 100 + offset; }
    private long roleId(int offset) { return BASE + 300 + offset; }
}
