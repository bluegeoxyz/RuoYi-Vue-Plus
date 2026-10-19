package org.dromara.web.customer;

import org.dromara.DromaraApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/** Full-application startup guard for mandatory Customer security. */
class CustomerSecurityApplicationStartupTest {

    @Test
    void missingCustomerSecurityStopsApplicationStartup() {
        ConfigurableApplicationContext context = null;
        try {
            context = new SpringApplicationBuilder(DromaraApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.main.banner-mode=off")
                .run(
                    "--mybatis-encryptor.enable=false",
                    "--property.customer.security.enabled=false",
                    "--springdoc.api-docs.enabled=false");
            fail("Customer 安全配置缺失时应用不应启动成功");
        } catch (Exception exception) {
            String messages = messages(exception);
            assertTrue(messages.contains("Customer security configuration incomplete"), messages);
            assertFalse(messages.contains("0123456789abcdef0123456789abcdef"));
        } finally {
            if (context != null) context.close();
        }
    }

    private String messages(Throwable throwable) {
        StringBuilder result = new StringBuilder();
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current.getMessage() != null) result.append(' ').append(current.getMessage());
        }
        return result.toString();
    }
}
