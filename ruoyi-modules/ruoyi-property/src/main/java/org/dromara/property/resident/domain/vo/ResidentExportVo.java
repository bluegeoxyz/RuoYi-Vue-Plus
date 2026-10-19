package org.dromara.property.resident.domain.vo;
import lombok.Data; import org.apache.fesod.sheet.annotation.*; import org.dromara.common.excel.annotation.ExcelDictFormat; import org.dromara.common.excel.convert.ExcelDictConvert; import java.time.LocalDate;
/** 住户项目视图脱敏导出。 */
@Data @ExcelIgnoreUnannotated public class ResidentExportVo {
 @ExcelProperty("客户编号") private String customerNo; @ExcelProperty("姓名") private String maskedCustomerName; @ExcelProperty("手机号") private String maskedMobile;
 @ExcelProperty(value="关系类型",converter=ExcelDictConvert.class) @ExcelDictFormat(dictType="pm_relation_type") private String relationType;
 @ExcelProperty("楼栋") private String buildingName; @ExcelProperty("单元") private String unitName; @ExcelProperty("房屋") private String roomNo;
 @ExcelProperty(value="关系状态",converter=ExcelDictConvert.class) @ExcelDictFormat(dictType="pm_relation_status") private String relationStatus;
 @ExcelProperty("迁入日期") private LocalDate moveInDate; @ExcelProperty("迁出日期") private LocalDate moveOutDate;
}
