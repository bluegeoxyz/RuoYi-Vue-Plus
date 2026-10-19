package org.dromara.warehouse.inventory.transaction.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 不可变库存流水事实实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_inventory_transaction")
public class InventoryTransaction extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private String transactionNo;
    private String transactionType;
    private Long companyId;
    private Long projectId;
    private Long warehouseId;
    private Long locationId;
    private Long materialId;
    private BigDecimal quantityBefore;
    private BigDecimal quantityChange;
    private BigDecimal quantityAfter;
    private BigDecimal lockedBefore;
    private BigDecimal lockedChange;
    private BigDecimal lockedAfter;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private LocalDateTime occurredAt;
    private Long operatorId;
    private String remark;
}
