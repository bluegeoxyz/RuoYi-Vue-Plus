package org.dromara.property.customer.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Customer 敏感数据安全配置。 */
@Data
@ConfigurationProperties(prefix = "property.customer.security")
public class CustomerSecurityProperties {
    private boolean enabled;
    private String hmacKeyBase64;
    private String keyVersion;
}
