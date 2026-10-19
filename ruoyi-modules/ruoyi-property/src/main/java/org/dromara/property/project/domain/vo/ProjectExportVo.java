package org.dromara.property.project.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.time.LocalDate;

/** 物业项目导出视图。 */
@Data
@ExcelIgnoreUnannotated
public class ProjectExportVo {

    @ExcelProperty("项目编码")
    private String projectCode;
    @ExcelProperty("项目名称")
    private String projectName;
    @ExcelProperty("项目简称")
    private String projectShortName;
    @ExcelProperty(value = "项目类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_project_type")
    private String projectType;
    @ExcelProperty("负责人")
    private String managerEmployeeName;
    @ExcelProperty("服务电话")
    private String servicePhone;
    @ExcelProperty("所在地区编码")
    private String regionCode;
    @ExcelProperty("详细地址")
    private String address;
    @ExcelProperty("交付日期")
    private LocalDate deliveryDate;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;
}
