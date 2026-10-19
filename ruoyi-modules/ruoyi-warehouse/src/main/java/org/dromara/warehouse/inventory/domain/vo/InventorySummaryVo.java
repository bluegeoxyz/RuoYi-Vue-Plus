package org.dromara.warehouse.inventory.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 仓库物料库存汇总视图。 */
@Data
@ExcelIgnoreUnannotated
public class InventorySummaryVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long companyId;
    @ExcelProperty("所属公司") private String companyName;
    private Long warehouseId;
    @ExcelProperty("仓库") private String warehouseName;
    private Long materialId;
    @ExcelProperty("物料编码") private String materialCode;
    @ExcelProperty("物料名称") private String materialName;
    @ExcelProperty("规格") private String specification;
    @ExcelProperty("型号") private String model;
    private Long categoryId;
    private String categoryName;
    @ExcelProperty("基础单位") private String baseUnitName;
    private String baseUnitSymbol;
    @ExcelProperty("当前数量") private BigDecimal quantity;
    @ExcelProperty("锁定数量") private BigDecimal lockedQuantity;
    @ExcelProperty("可用数量") private BigDecimal availableQuantity;
    @ExcelProperty("安全库存") private BigDecimal safetyStock;
    private Boolean lowStock;
}
