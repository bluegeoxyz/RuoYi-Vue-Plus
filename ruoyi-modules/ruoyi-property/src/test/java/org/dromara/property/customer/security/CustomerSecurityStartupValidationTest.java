package org.dromara.property.customer.security;

import org.dromara.common.encrypt.properties.EncryptorProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CustomerSecurityStartupValidationTest {
    private static final String AES_KEY = "0123456789abcdef0123456789abcdef";
    private static final String HMAC_KEY = Base64.getEncoder().encodeToString(
        "fedcba9876543210fedcba9876543210".getBytes());

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfiguration.class);

    @Test void validConfigurationStarts() {
        runner.withPropertyValues(valid()).run(context -> {
            assertNull(context.getStartupFailure());
            assertTrue(context.containsBean("customerSecurityStartupValidator"));
            assertTrue(context.containsBean("customerIdentityFingerprintService"));
        });
    }

    @Test void disabledAesFailsStartup() { assertFails("mybatis-encryptor.enable=false"); }
    @Test void nonAesAlgorithmFailsStartup() { assertFails("mybatis-encryptor.algorithm=BASE64"); }
    @Test void missingAesKeyFailsStartup() { assertFails("mybatis-encryptor.password="); }
    @Test void invalidAesKeyLengthFailsStartup() { assertFails("mybatis-encryptor.password=short-key"); }
    @Test void disabledCustomerSecurityFailsStartup() { assertFails("property.customer.security.enabled=false"); }
    @Test void missingHmacKeyFailsStartup() { assertFails("property.customer.security.hmac-key-base64="); }
    @Test void malformedHmacBase64FailsStartup() { assertFails("property.customer.security.hmac-key-base64=not-base64!!"); }
    @Test void shortHmacKeyFailsStartup() {
        assertFails("property.customer.security.hmac-key-base64="
            + Base64.getEncoder().encodeToString("too-short".getBytes()));
    }
    @Test void missingKeyVersionFailsStartup() { assertFails("property.customer.security.key-version="); }
    @Test void reusedAesAndHmacKeyFailsStartup() {
        assertFails("property.customer.security.hmac-key-base64="
            + Base64.getEncoder().encodeToString(AES_KEY.getBytes()));
    }

    private void assertFails(String override) {
        String[] properties = valid();
        String[] configured = java.util.Arrays.copyOf(properties, properties.length + 1);
        configured[properties.length] = override;
        runner.withPropertyValues(configured).run(context -> {
            Throwable failure = context.getStartupFailure();
            assertNotNull(failure);
            assertTrue(messages(failure).contains("Customer security configuration incomplete"));
            assertFalse(messages(failure).contains(AES_KEY));
            assertFalse(messages(failure).contains(HMAC_KEY));
        });
    }

    private String[] valid() {
        return new String[]{
            "mybatis-encryptor.enable=true",
            "mybatis-encryptor.algorithm=AES",
            "mybatis-encryptor.password=" + AES_KEY,
            "property.customer.security.enabled=true",
            "property.customer.security.hmac-key-base64=" + HMAC_KEY,
            "property.customer.security.key-version=v1"
        };
    }

    private String messages(Throwable throwable) {
        StringBuilder result = new StringBuilder();
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current.getMessage() != null) result.append(' ').append(current.getMessage());
        }
        return result.toString();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({EncryptorProperties.class, CustomerSecurityProperties.class})
    @Import(CustomerSecurityConfiguration.class)
    static class TestConfiguration {
    }
}
