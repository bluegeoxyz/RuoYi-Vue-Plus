package org.dromara.common.encrypt.exception;

/**
 * 字段加解密失败异常。
 *
 * <p>异常消息不得包含字段原值、密文或密钥。</p>
 */
public class FieldCryptoException extends RuntimeException {

    public FieldCryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
