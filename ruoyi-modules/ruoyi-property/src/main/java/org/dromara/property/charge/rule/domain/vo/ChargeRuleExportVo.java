package org.dromara.property.charge.rule.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.*;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import java.time.LocalDate;

@Data
@ExcelIgnoreUnannotated
public class ChargeRuleExportVo {
    @ExcelProperty("所属项目")
    private String projectName;

    @ExcelProperty("收费项目")
    private String chargeItemName;

    @ExcelProperty("规则编码")
    private String ruleCode;

    @ExcelProperty("规则名称")
    private String ruleName;

    @ExcelProperty(value = "计费方式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_billing_method")
    private String billingMethod;

    @ExcelProperty(value = "计费周期", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_billing_cycle")
    private String billingCycle;

    @ExcelProperty(value = "周期模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_cycle_mode")
    private String cycleMode;

    @ExcelProperty("计费标准")
    private String billingStandard;

    @ExcelProperty("生效日期")
    private LocalDate effectiveStartDate;

    @ExcelProperty("失效日期")
    private LocalDate effectiveEndDate;

    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    @ExcelProperty("排序")
    private Integer sort;

    @ExcelProperty("备注")
    private String remark;
}
