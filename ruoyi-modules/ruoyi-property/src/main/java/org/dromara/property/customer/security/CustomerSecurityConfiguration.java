package org.dromara.property.customer.security;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.encrypt.enums.AlgorithmType;
import org.dromara.common.encrypt.properties.EncryptorProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.InitializingBean;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** Customer 敏感数据安全组件配置。 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableConfigurationProperties(CustomerSecurityProperties.class)
public class CustomerSecurityConfiguration {
    private final CustomerSecurityProperties customerProperties;
    private final EncryptorProperties encryptorProperties;

    /**
     * V2 Customer 模块始终启用，安全配置因此是应用启动的强制前置条件。
     */
    @Bean
    public InitializingBean customerSecurityStartupValidator() {
        return () -> validateConfiguration(customerProperties, encryptorProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "property.customer.security", name = "enabled", havingValue = "true")
    public CustomerIdentityFingerprintService customerIdentityFingerprintService() {
        validateConfiguration(customerProperties, encryptorProperties);
        return new HmacSha256CustomerIdentityFingerprintService(customerProperties.getHmacKeyBase64());
    }

    static void validateConfiguration(CustomerSecurityProperties customerProperties,
                                      EncryptorProperties encryptorProperties) {
        if (!Boolean.TRUE.equals(encryptorProperties.getEnable())) {
            throw new IllegalStateException("Customer security configuration incomplete: mybatis-encryptor 必须启用");
        }
        if (encryptorProperties.getAlgorithm() != AlgorithmType.AES) {
            throw new IllegalStateException("Customer security configuration incomplete: 加密算法必须为 AES");
        }
        String aesKey = encryptorProperties.getPassword();
        int byteLength = aesKey == null ? 0 : aesKey.getBytes(StandardCharsets.UTF_8).length;
        if (StringUtils.isBlank(aesKey)) {
            throw new IllegalStateException("Customer security configuration incomplete: AES 密钥不能为空");
        }
        if ((byteLength != 16 && byteLength != 24 && byteLength != 32) || aesKey.length() != byteLength) {
            throw new IllegalStateException("Customer security configuration incomplete: AES 密钥长度必须为 16、24 或 32 字节 ASCII");
        }
        if (!customerProperties.isEnabled()) {
            throw new IllegalStateException("Customer security configuration incomplete: Customer Security 必须启用");
        }
        if (StringUtils.isBlank(customerProperties.getHmacKeyBase64())) {
            throw new IllegalStateException("Customer security configuration incomplete: HMAC 密钥不能为空");
        }
        if (StringUtils.isBlank(customerProperties.getKeyVersion())) {
            throw new IllegalStateException("Customer security configuration incomplete: PII 密钥版本不能为空");
        }
        byte[] hmacKey;
        try {
            hmacKey = Base64.getDecoder().decode(customerProperties.getHmacKeyBase64());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Customer security configuration incomplete: HMAC 密钥必须使用合法 Base64 编码", e);
        }
        if (hmacKey.length < 32) {
            throw new IllegalStateException("Customer security configuration incomplete: HMAC-SHA256 密钥至少需要 32 字节");
        }
        if (MessageDigest.isEqual(aesKey.getBytes(StandardCharsets.UTF_8), hmacKey)) {
            throw new IllegalStateException("Customer security configuration incomplete: AES 与 HMAC 密钥必须相互独立");
        }
    }
}
