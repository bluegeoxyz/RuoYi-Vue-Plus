package org.dromara.hr.employee.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 员工账号绑定请求。 */
@Data
public class EmployeeBindUserBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "系统用户ID不能为空")
    private Long userId;
}
