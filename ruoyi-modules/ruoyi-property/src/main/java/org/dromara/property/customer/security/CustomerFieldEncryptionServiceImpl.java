package org.dromara.property.customer.security;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.encrypt.exception.FieldCryptoException;
import org.dromara.common.encrypt.properties.EncryptorProperties;
import org.dromara.common.encrypt.utils.EncryptUtils;
import org.springframework.stereotype.Service;

/** Customer 证件字段 AES 加密实现。 */
@Service
@RequiredArgsConstructor
public class CustomerFieldEncryptionServiceImpl implements CustomerFieldEncryptionService {

    private final CustomerSecurityProperties customerProperties;
    private final EncryptorProperties encryptorProperties;

    @Override
    public String encryptIdNo(String plaintext) {
        CustomerSecurityConfiguration.validateConfiguration(customerProperties, encryptorProperties);
        if (StringUtils.isBlank(plaintext)) {
            throw new FieldCryptoException("Customer 证件字段加密失败", null);
        }
        try {
            if (plaintext.startsWith(Constants.ENCRYPT_HEADER)) {
                EncryptUtils.decryptByAes(plaintext.substring(Constants.ENCRYPT_HEADER.length()),
                    encryptorProperties.getPassword());
                return plaintext;
            }
            return Constants.ENCRYPT_HEADER
                + EncryptUtils.encryptByAes(plaintext, encryptorProperties.getPassword());
        } catch (Exception e) {
            throw new FieldCryptoException("Customer 证件字段加密失败", e);
        }
    }
}
