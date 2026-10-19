package org.dromara.hr.employee.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 员工档案导出对象，不导出证件号码。 */
@Data
@ExcelIgnoreUnannotated
public class EmployeeExportVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("员工编号")
    private String employeeNo;
    @ExcelProperty("员工姓名")
    private String employeeName;
    @ExcelProperty("所属部门")
    private String deptName;
    @ExcelProperty("主岗位")
    private String postName;
    @ExcelProperty(value = "性别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_user_gender")
    private String gender;
    @ExcelProperty("手机号码")
    private String mobile;
    @ExcelProperty("邮箱")
    private String email;
    @ExcelProperty(value = "用工类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "hr_employment_type")
    private String employmentType;
    @ExcelProperty(value = "员工状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "hr_employment_status")
    private String employmentStatus;
    @ExcelProperty("入职日期")
    private LocalDate entryDate;
    @ExcelProperty("离职日期")
    private LocalDate leaveDate;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;
}
