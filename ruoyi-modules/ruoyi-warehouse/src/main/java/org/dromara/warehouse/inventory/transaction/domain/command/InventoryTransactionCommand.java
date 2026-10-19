package org.dromara.warehouse.inventory.transaction.domain.command;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 正式库存业务写入流水使用的内部命令。 */
@Data
public class InventoryTransactionCommand {
    private String transactionType;
    private Long warehouseId;
    private Long locationId;
    private Long materialId;
    private BigDecimal quantityBefore;
    private BigDecimal quantityChange;
    private BigDecimal quantityAfter;
    private BigDecimal lockedBefore = BigDecimal.ZERO;
    private BigDecimal lockedChange = BigDecimal.ZERO;
    private BigDecimal lockedAfter = BigDecimal.ZERO;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private LocalDateTime occurredAt;
    private Long operatorId;
    private String remark;
}
