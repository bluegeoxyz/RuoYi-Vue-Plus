package org.dromara.web.system;

import org.dromara.DromaraApplication;
import org.dromara.common.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 健康检查匿名访问与业务接口鉴权的真实 HTTP 回归测试。
 */
@EnabledIfEnvironmentVariable(named = "ZBM_HTTP_TEST_ENABLED", matches = "true")
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
class HealthEndpointHttpIntegrationTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void anonymousHealthIsMinimalAndBusinessApiStillRequiresLogin() throws Exception {
        HttpResponse<String> health = get("/actuator/health");

        assertEquals(200, health.statusCode(), health.body());
        Map<String, Object> healthBody = JsonUtils.parseMap(health.body());
        assertEquals("UP", healthBody.get("status"));
        assertFalse(healthBody.containsKey("components"), health.body());
        assertFalse(healthBody.containsKey("details"), health.body());

        HttpResponse<String> protectedResponse = get("/system/user/list");
        Map<String, Object> protectedBody = JsonUtils.parseMap(protectedResponse.body());
        Object code = protectedBody.get("code");
        assertTrue(protectedResponse.statusCode() == 401
                || code instanceof Number number && number.intValue() == 401,
            protectedResponse.body());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port + path))
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
