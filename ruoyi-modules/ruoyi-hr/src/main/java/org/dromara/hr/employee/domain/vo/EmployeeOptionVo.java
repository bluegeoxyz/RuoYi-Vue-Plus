package org.dromara.hr.employee.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 员工下拉选项。 */
@Data
public class EmployeeOptionVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String employeeNo;
    private String employeeName;
    private Long deptId;
    private String deptName;
    private String postName;
}
