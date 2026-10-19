package org.dromara.property.resource.importer.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.importer.ImportRow;

import java.math.BigDecimal;

/** 房屋树形导入行；项目及各级 ID 均由服务端上下文确定。 */
@Data
public class PropertyResourceImportRow implements ImportRow {
    @ExcelIgnore private Integer rowNumber;
    @NotBlank(message = "楼栋编码不能为空") @Size(max = 32) @ExcelProperty("楼栋编码") private String buildingCode;
    @NotBlank(message = "楼栋名称不能为空") @Size(max = 64) @ExcelProperty("楼栋名称") private String buildingName;
    @NotBlank(message = "楼栋类型不能为空") @ExcelProperty("楼栋类型") private String buildingType;
    @ExcelProperty("楼栋地上楼层") private Integer aboveGroundFloors;
    @ExcelProperty("楼栋地下楼层") private Integer undergroundFloors;
    @ExcelProperty("楼栋排序") private Integer buildingSort;
    @NotBlank(message = "单元编码不能为空") @Size(max = 32) @ExcelProperty("单元编码") private String unitCode;
    @NotBlank(message = "单元名称不能为空") @Size(max = 64) @ExcelProperty("单元名称") private String unitName;
    @ExcelProperty("单元楼层数") private Integer floorCount;
    @ExcelProperty("单元排序") private Integer unitSort;
    @NotBlank(message = "房屋编码不能为空") @Size(max = 64) @ExcelProperty("房屋编码") private String roomCode;
    @NotBlank(message = "房号不能为空") @Size(max = 32) @ExcelProperty("房号") private String roomNo;
    @ExcelProperty("房屋楼层") private Integer floorNo;
    @ExcelProperty("房屋类型") private String roomType;
    @ExcelProperty("产权属性") private String propertyType;
    @DecimalMin(value = "0", message = "建筑面积不能小于0") @ExcelProperty("建筑面积") private BigDecimal buildingArea;
    @DecimalMin(value = "0", message = "使用面积不能小于0") @ExcelProperty("使用面积") private BigDecimal usableArea;
    @ExcelProperty("交付状态") private String deliveryStatus;
    @ExcelProperty("入住状态") private String occupancyStatus;
    @ExcelProperty("房屋排序") private Integer roomSort;
    @NotBlank(message = "状态不能为空") @ExcelProperty("状态") private String status;
}
