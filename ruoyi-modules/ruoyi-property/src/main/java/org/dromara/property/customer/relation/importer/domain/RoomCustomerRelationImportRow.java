package org.dromara.property.customer.relation.importer.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.importer.ImportRow;

import java.time.LocalDate;

/** 房屋客户关系 Excel 导入行，只使用稳定业务编码定位关联对象。 */
@Data
public class RoomCustomerRelationImportRow implements ImportRow {
    @ExcelIgnore private Integer rowNumber;
    @ExcelIgnore private Long roomId;
    @ExcelIgnore private Long customerId;
    @NotBlank(message = "楼栋编码不能为空") @Size(max = 64) @ExcelProperty("楼栋编码") private String buildingCode;
    @NotBlank(message = "单元编码不能为空") @Size(max = 64) @ExcelProperty("单元编码") private String unitCode;
    @NotBlank(message = "房屋编码不能为空") @Size(max = 64) @ExcelProperty("房屋编码") private String roomCode;
    @NotBlank(message = "客户编号不能为空") @Size(max = 64) @ExcelProperty("客户编号") private String customerNo;
    @NotBlank(message = "关系类型不能为空") @Size(max = 32) @ExcelProperty("关系类型") private String relationType;
    @NotBlank(message = "是否主要关系不能为空") @Size(max = 1) @ExcelProperty("是否主要关系") private String isPrimary;
    @NotNull(message = "开始日期不能为空") @ExcelProperty("开始日期") private LocalDate startDate;
    @Size(max = 500) @ExcelProperty("备注") private String remark;
}
