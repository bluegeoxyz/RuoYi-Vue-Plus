package org.dromara.property.customer.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Customer 生命周期变更参数。 */
@Data
public class CustomerLifecycleBo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "客户ID不能为空")
    private Long id;

    @NotNull(message = "版本号不能为空")
    private Integer version;

    @NotBlank(message = "生命周期状态不能为空")
    @Pattern(regexp = "ACTIVE|INACTIVE|CANCELLED", message = "生命周期状态不合法")
    private String lifecycleStatus;
}
