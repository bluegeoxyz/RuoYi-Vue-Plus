package org.dromara.property.customer.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.property.customer.domain.CustomerManagementGrant;

import java.time.LocalDateTime;

/** Customer 企业授权视图。 */
@Data
@AutoMapper(target = CustomerManagementGrant.class)
public class CustomerManagementGrantVo {
    private Long id;
    private String principalType;
    private Long principalId;
    private String scopeType;
    private String status;
    private Integer version;
    private String delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    private String remark;
}
