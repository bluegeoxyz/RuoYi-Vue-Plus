package org.dromara.property.customer.security;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.encrypt.enums.AlgorithmType;
import org.dromara.common.encrypt.properties.EncryptorProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Customer 身份 HMAC 指纹单元测试")
class CustomerIdentityFingerprintServiceTest {

    private static final String HMAC_KEY = Base64.getEncoder().encodeToString(
        "independent-customer-hmac-key-32bytes".getBytes(StandardCharsets.UTF_8));

    private final CustomerIdentityFingerprintService service =
        new HmacSha256CustomerIdentityFingerprintService(HMAC_KEY);

    @Test
    @DisplayName("同一规范化身份生成稳定指纹")
    void shouldGenerateStableFingerprintForNormalizedIdentity() {
        byte[] first = service.fingerprint(" id_card ", "１２３４ ５６x");
        byte[] second = service.fingerprint("ID_CARD", "123456X");

        assertArrayEquals(first, second);
        assertEquals(32, first.length);
    }

    @Test
    @DisplayName("不同证件类型具有不同身份域")
    void shouldSeparateIdentityTypes() {
        byte[] idCard = service.fingerprint("ID_CARD", "123456X");
        byte[] passport = service.fingerprint("PASSPORT", "123456X");

        assertFalse(java.security.MessageDigest.isEqual(idCard, passport));
    }

    @Test
    @DisplayName("空身份和弱 HMAC 密钥被拒绝")
    void shouldRejectBlankIdentityAndWeakKey() {
        assertThrows(ServiceException.class, () -> service.fingerprint(" ", "123"));
        assertThrows(ServiceException.class, () -> service.fingerprint("ID_CARD", " \t "));
        String weakKey = Base64.getEncoder().encodeToString("weak".getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalStateException.class,
            () -> new HmacSha256CustomerIdentityFingerprintService(weakKey));
    }

    @Test
    @DisplayName("启动配置拒绝非 AES、缺失版本和复用密钥")
    void shouldValidateIndependentProductionKeysAtStartup() {
        CustomerSecurityProperties customer = new CustomerSecurityProperties();
        customer.setHmacKeyBase64(HMAC_KEY);
        customer.setKeyVersion("v1");
        EncryptorProperties encryptor = new EncryptorProperties();
        encryptor.setEnable(true);
        encryptor.setAlgorithm(AlgorithmType.BASE64);
        encryptor.setPassword("1234567890abcdef");
        assertThrows(IllegalStateException.class,
            () -> new CustomerSecurityConfiguration(customer, encryptor).customerIdentityFingerprintService());

        encryptor.setAlgorithm(AlgorithmType.AES);
        customer.setKeyVersion(" ");
        assertThrows(IllegalStateException.class,
            () -> new CustomerSecurityConfiguration(customer, encryptor).customerIdentityFingerprintService());

        String sharedKey = "1234567890abcdef1234567890abcdef";
        encryptor.setPassword(sharedKey);
        customer.setKeyVersion("v1");
        customer.setHmacKeyBase64(Base64.getEncoder().encodeToString(sharedKey.getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalStateException.class,
            () -> new CustomerSecurityConfiguration(customer, encryptor).customerIdentityFingerprintService());
    }
}
