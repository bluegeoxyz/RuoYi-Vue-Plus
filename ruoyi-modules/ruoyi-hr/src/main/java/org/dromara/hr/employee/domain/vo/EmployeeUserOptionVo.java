package org.dromara.hr.employee.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 员工绑定账号候选视图。 */
@Data
public class EmployeeUserOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private String nickName;
    private String deptName;
    private String status;
    private Boolean bound;
}
