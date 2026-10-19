package org.dromara.property.customer.security;

/** 计算 Customer 证件身份指纹。 */
public interface CustomerIdentityFingerprintService {
    String normalizeIdType(String idType);
    String normalizeIdNo(String idNo);
    byte[] fingerprint(String idType, String idNo);
}
