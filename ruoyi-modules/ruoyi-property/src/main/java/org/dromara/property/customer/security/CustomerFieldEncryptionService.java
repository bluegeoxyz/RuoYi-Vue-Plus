package org.dromara.property.customer.security;

/** Customer 敏感字段写入前加密服务。 */
public interface CustomerFieldEncryptionService {

    /**
     * 加密规范化后的证件号码。
     *
     * @param plaintext 证件号码明文，或已存在的标准密文
     * @return 带 ENC_ 前缀的标准密文
     */
    String encryptIdNo(String plaintext);
}
