package org.dromara.warehouse.inventory.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 库位级实时库存视图。 */
@Data
@ExcelIgnoreUnannotated
public class InventoryVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long companyId;
    @ExcelProperty("所属公司") private String companyName;
    private Long warehouseId;
    @ExcelProperty("仓库") private String warehouseName;
    private Long zoneId;
    @ExcelProperty("库区") private String zoneName;
    private Long locationId;
    @ExcelProperty("库位编码") private String locationCode;
    @ExcelProperty("库位名称") private String locationName;
    private Long materialId;
    @ExcelProperty("物料编码") private String materialCode;
    @ExcelProperty("物料名称") private String materialName;
    @ExcelProperty("规格") private String specification;
    @ExcelProperty("型号") private String model;
    private Long categoryId;
    private String categoryName;
    private Long baseUnitId;
    @ExcelProperty("基础单位") private String baseUnitName;
    private String baseUnitSymbol;
    @ExcelProperty("当前数量") private BigDecimal quantity;
    @ExcelProperty("锁定数量") private BigDecimal lockedQuantity;
    @ExcelProperty("可用数量") private BigDecimal availableQuantity;
    @ExcelProperty("安全库存") private BigDecimal safetyStock;
    private Integer version;
}
