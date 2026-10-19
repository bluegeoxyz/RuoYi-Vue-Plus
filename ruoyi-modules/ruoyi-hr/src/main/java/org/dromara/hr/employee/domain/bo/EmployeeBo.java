package org.dromara.hr.employee.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.hr.employee.domain.Employee;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工档案业务对象。
 */
@Data
@AutoMapper(target = Employee.class, reverseConvertGenerate = false)
public class EmployeeBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "员工ID不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "员工编号不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32, message = "员工编号长度不能超过{max}个字符")
    private String employeeNo;

    @NotBlank(message = "员工姓名不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "员工姓名长度不能超过{max}个字符")
    private String employeeName;

    @NotNull(message = "所属部门不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long deptId;
    private Long postId;
    private String gender;

    @Size(max = 32, message = "手机号码长度不能超过{max}个字符")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过{max}个字符")
    private String email;
    private String idType;

    @Size(max = 64, message = "证件号码长度不能超过{max}个字符")
    private String idNo;
    private LocalDate birthDate;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    private String employmentType;

    @NotBlank(message = "员工状态不能为空", groups = {AddGroup.class, EditGroup.class})
    private String employmentStatus;
    private Integer sort;
    private String status;

    @Size(max = 500, message = "备注长度不能超过{max}个字符")
    private String remark;
    private Map<String, Object> params = new HashMap<>();
}
