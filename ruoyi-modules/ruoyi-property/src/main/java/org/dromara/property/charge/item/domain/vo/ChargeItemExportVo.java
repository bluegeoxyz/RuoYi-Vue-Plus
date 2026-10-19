package org.dromara.property.charge.item.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

/** 收费项目业务导出，不暴露内部 ID。 */
@Data
@ExcelIgnoreUnannotated
public class ChargeItemExportVo {
    @ExcelProperty("所属项目") private String projectName;
    @ExcelProperty("收费项目编码") private String itemCode;
    @ExcelProperty("收费项目名称") private String itemName;
    @ExcelProperty(value = "收费类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_charge_type") private String chargeType;
    @ExcelProperty(value = "收费分类", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_charge_category") private String chargeCategory;
    @ExcelProperty(value = "收费载体", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_charge_object_type") private String chargeObjectType;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable") private String status;
    @ExcelProperty("排序") private Integer sort;
    @ExcelProperty("备注") private String remark;
}
