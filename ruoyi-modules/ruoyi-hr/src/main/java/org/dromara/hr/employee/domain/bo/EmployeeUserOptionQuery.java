package org.dromara.hr.employee.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 员工绑定账号候选查询条件。 */
@Data
public class EmployeeUserOptionQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userName;
    private String nickName;
    private String phoneNumber;
}
