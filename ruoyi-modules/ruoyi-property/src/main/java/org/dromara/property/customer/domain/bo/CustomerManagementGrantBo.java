package org.dromara.property.customer.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.property.customer.domain.CustomerManagementGrant;

/** Customer 企业授权请求。 */
@Data
@AutoMapper(target = CustomerManagementGrant.class, reverseConvertGenerate = false)
public class CustomerManagementGrantBo {
    private Long id;
    @NotBlank
    private String principalType;
    @NotNull
    private Long principalId;
    private String scopeType;
    private Integer version;
    private String status;
    private String remark;
}
