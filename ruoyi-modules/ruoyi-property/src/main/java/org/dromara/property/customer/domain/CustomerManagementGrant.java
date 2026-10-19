package org.dromara.property.customer.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/** 企业 Customer 主档授权。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_customer_management_grant")
public class CustomerManagementGrant extends BaseEntity {
    @TableId
    private Long id;
    private String principalType;
    private Long principalId;
    private String scopeType;
    private String status;
    @Version
    private Integer version;
    @TableLogic
    private String delFlag;
}
