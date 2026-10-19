package org.dromara.property.customer.security;

import org.dromara.common.core.constant.Constants;
import org.dromara.common.encrypt.enums.AlgorithmType;
import org.dromara.common.encrypt.exception.FieldCryptoException;
import org.dromara.common.encrypt.properties.EncryptorProperties;
import org.dromara.common.encrypt.utils.EncryptUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CustomerFieldEncryptionServiceTest {
    private static final String AES_KEY = "0123456789abcdef0123456789abcdef";
    private CustomerFieldEncryptionService service;

    @BeforeEach
    void setUp() {
        CustomerSecurityProperties customer = new CustomerSecurityProperties();
        customer.setEnabled(true);
        customer.setKeyVersion("v1");
        customer.setHmacKeyBase64(Base64.getEncoder().encodeToString(
            "fedcba9876543210fedcba9876543210".getBytes()));
        EncryptorProperties encryptor = new EncryptorProperties();
        encryptor.setEnable(true);
        encryptor.setAlgorithm(AlgorithmType.AES);
        encryptor.setPassword(AES_KEY);
        service = new CustomerFieldEncryptionServiceImpl(customer, encryptor);
    }

    @Test
    void encryptsBeforeMapperAndUsesStandardPrefix() {
        String encrypted = service.encryptIdNo("110101199001011234");
        assertTrue(encrypted.startsWith(Constants.ENCRYPT_HEADER));
        assertEquals("110101199001011234", EncryptUtils.decryptByAes(
            encrypted.substring(Constants.ENCRYPT_HEADER.length()), AES_KEY));
    }

    @Test
    void validCiphertextIsNotEncryptedTwice() {
        String encrypted = service.encryptIdNo("110101199001011234");
        assertSame(encrypted, service.encryptIdNo(encrypted));
    }

    @Test
    void malformedCiphertextFailsClosedWithoutSensitiveMessage() {
        String malformed = Constants.ENCRYPT_HEADER + "secret-ciphertext";
        FieldCryptoException exception = assertThrows(FieldCryptoException.class,
            () -> service.encryptIdNo(malformed));
        assertFalse(exception.getMessage().contains(malformed));
        assertFalse(exception.getMessage().contains(AES_KEY));
    }
}
