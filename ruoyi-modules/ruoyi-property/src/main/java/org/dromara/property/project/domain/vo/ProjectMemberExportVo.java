package org.dromara.property.project.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.time.LocalDate;

/** 物业项目成员导出视图。 */
@Data
@ExcelIgnoreUnannotated
public class ProjectMemberExportVo {

    @ExcelProperty("项目编码")
    private String projectCode;
    @ExcelProperty("项目名称")
    private String projectName;
    @ExcelProperty("员工编号")
    private String employeeNo;
    @ExcelProperty("员工姓名")
    private String employeeName;
    @ExcelProperty("部门")
    private String deptName;
    @ExcelProperty("岗位")
    private String postName;
    @ExcelProperty(value = "项目角色", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_member_role")
    private String memberRole;
    @ExcelProperty(value = "是否负责人", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=否,1=是")
    private String isManager;
    @ExcelProperty("开始日期")
    private LocalDate startDate;
    @ExcelProperty("结束日期")
    private LocalDate endDate;
    @ExcelProperty(value = "成员状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_member_status")
    private String memberStatus;
}
