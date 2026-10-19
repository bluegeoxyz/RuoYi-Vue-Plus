package org.dromara.property.customer.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** Customer 敏感资料访问审计。禁止写入证件、联系方式、密钥或指纹输入。 */
@Data
@TableName("pm_customer_sensitive_access_audit")
public class CustomerSensitiveAccessAudit {
    @TableId
    private Long id;
    private Long operatorId;
    private Long customerId;
    private String accessAction;
    private String accessPurpose;
    private String fieldLevel;
    private String decision;
    private String traceId;
    private Long projectId;
    private LocalDateTime createTime;
    private String remark;
}
