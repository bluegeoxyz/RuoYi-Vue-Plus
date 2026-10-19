package org.dromara.property.customer.domain.bo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Customer 普通资料修改参数；强身份字段必须走后续专门流程。 */
@Data
public class CustomerUpdateBo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "客户ID不能为空")
    private Long id;

    @NotNull(message = "版本号不能为空")
    private Integer version;

    @NotBlank(message = "客户名称不能为空")
    @Size(max = 128)
    private String customerName;

    @Size(max = 1)
    private String gender;

    @Size(max = 32)
    private String mobile;

    @Size(max = 32)
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128)
    private String email;

    private LocalDate birthDate;

    @Size(max = 500)
    private String remark;
}
