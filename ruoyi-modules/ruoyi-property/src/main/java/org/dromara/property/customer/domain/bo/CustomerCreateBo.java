package org.dromara.property.customer.domain.bo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Customer 主档新增参数，不接收任何安全元数据或审计字段。 */
@Data
public class CustomerCreateBo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "客户类型不能为空")
    @Size(max = 32)
    private String customerType;

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

    @Size(max = 32)
    private String idType;

    @Size(max = 128)
    private String idNo;

    private LocalDate birthDate;

    @Size(max = 500)
    private String remark;
}
