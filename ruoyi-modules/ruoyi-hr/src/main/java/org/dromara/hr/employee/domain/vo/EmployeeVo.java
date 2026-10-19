package org.dromara.hr.employee.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.sensitive.annotation.Sensitive;
import org.dromara.common.sensitive.core.SensitiveStrategy;
import org.dromara.hr.employee.domain.Employee;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工档案视图对象。
 */
@Data
@AutoMapper(target = Employee.class)
public class EmployeeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String employeeNo;
    private String employeeName;
    private Long deptId;
    private String deptName;
    private Long postId;
    private String postName;
    private Long userId;
    private String userName;
    private String nickName;
    private String userStatus;
    private String gender;

    @Sensitive(strategy = SensitiveStrategy.PHONE, perms = "hr:employee:edit")
    private String mobile;

    @Sensitive(strategy = SensitiveStrategy.EMAIL, perms = "hr:employee:edit")
    private String email;
    private String idType;

    @Sensitive(strategy = SensitiveStrategy.ID_CARD, perms = "hr:employee:edit")
    private String idNo;
    private LocalDate birthDate;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    private String employmentType;
    private String employmentStatus;
    private Integer sort;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
