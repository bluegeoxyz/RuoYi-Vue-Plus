package org.dromara.hr.employee.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDate;

/**
 * 员工档案实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee")
public class Employee extends BaseEntity {

    @TableId
    private Long id;
    private String employeeNo;
    private String employeeName;
    private Long deptId;
    private Long postId;
    private Long userId;
    private String gender;
    private String mobile;
    private String email;
    private String idType;
    private String idNo;
    private LocalDate birthDate;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    private String employmentType;
    private String employmentStatus;
    private Integer sort;
    private String status;
    private String remark;

    @TableLogic
    private String delFlag;
}
