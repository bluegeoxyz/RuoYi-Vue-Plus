package org.dromara.warehouse.inventory.transaction.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 库存流水查询与导出视图。 */
@Data
@ExcelIgnoreUnannotated
public class InventoryTransactionVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    @ExcelProperty("流水号") private String transactionNo;
    @ExcelProperty("流水类型") private String transactionType;
    private Long companyId;
    @ExcelProperty("所属公司") private String companyName;
    private Long projectId;
    @ExcelProperty("所属项目") private String projectName;
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
    @ExcelProperty("基础单位") private String baseUnitName;
    private String baseUnitSymbol;
    @ExcelProperty("变更前数量") private BigDecimal quantityBefore;
    @ExcelProperty("变更数量") private BigDecimal quantityChange;
    @ExcelProperty("变更后数量") private BigDecimal quantityAfter;
    private BigDecimal lockedBefore;
    private BigDecimal lockedChange;
    private BigDecimal lockedAfter;
    @ExcelProperty("来源类型") private String sourceType;
    private Long sourceId;
    @ExcelProperty("来源单号") private String sourceNo;
    @ExcelProperty("发生时间") private LocalDateTime occurredAt;
    private Long operatorId;
    @ExcelProperty("操作人") private String operatorName;
    @ExcelProperty("备注") private String remark;
}
