package org.dromara.property.resource.building.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

/** 楼栋业务导出对象，不包含内部 ID。 */
@Data
@ExcelIgnoreUnannotated
public class BuildingExportVo {
    @ExcelProperty("楼栋编码") private String buildingCode;
    @ExcelProperty("楼栋名称") private String buildingName;
    @ExcelProperty(value = "楼栋类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_building_type") private String buildingType;
    @ExcelProperty("地上层数") private Integer aboveGroundFloors;
    @ExcelProperty("地下层数") private Integer undergroundFloors;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable") private String status;
    @ExcelProperty("备注") private String remark;
}
