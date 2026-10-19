package org.dromara.property.customer.security;

import org.dromara.common.core.exception.ServiceException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Locale;

/** 基于独立密钥的 Customer 身份 HMAC-SHA256 实现。 */
public class HmacSha256CustomerIdentityFingerprintService implements CustomerIdentityFingerprintService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final byte[] hmacKey;

    public HmacSha256CustomerIdentityFingerprintService(String hmacKeyBase64) {
        try {
            this.hmacKey = Base64.getDecoder().decode(hmacKeyBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Customer HMAC 密钥必须使用合法 Base64 编码", e);
        }
        if (hmacKey.length < 32) {
            throw new IllegalStateException("Customer HMAC-SHA256 密钥至少需要 32 字节");
        }
    }

    @Override
    public String normalizeIdType(String idType) {
        if (idType == null || idType.isBlank()) {
            throw new ServiceException("证件类型不能为空");
        }
        return Normalizer.normalize(idType, Normalizer.Form.NFKC).trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String normalizeIdNo(String idNo) {
        if (idNo == null || idNo.isBlank()) {
            throw new ServiceException("证件号码不能为空");
        }
        String normalized = Normalizer.normalize(idNo, Normalizer.Form.NFKC)
            .replaceAll("\\s+", "")
            .toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new ServiceException("证件号码不能为空");
        }
        return normalized;
    }

    @Override
    public byte[] fingerprint(String idType, String idNo) {
        String canonical = normalizeIdType(idType) + '\0' + normalizeIdNo(idNo);
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(hmacKey, HMAC_ALGORITHM));
            return mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Customer 身份指纹计算失败", e);
        }
    }
}
