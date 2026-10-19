package org.dromara.property.customer.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** Customer 主档授权变更审计，不包含任何客户敏感资料。 */
@Data
@TableName("pm_customer_management_grant_audit")
public class CustomerManagementGrantAudit {
    @TableId
    private Long id;
    private Long grantId;
    private Long operatorId;
    private String action;
    private String principalType;
    private Long principalId;
    private String scopeType;
    private String beforeStatus;
    private String afterStatus;
    private String result;
    private String traceId;
    private LocalDateTime createTime;
    private String remark;
}
