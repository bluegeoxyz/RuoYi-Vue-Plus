package org.dromara.warehouse.inventory.transaction.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 库存流水查询条件。 */
@Data
public class InventoryTransactionBo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long companyId;
    private Long warehouseId;
    private Long zoneId;
    private Long locationId;
    private String materialCode;
    private String materialName;
    private String transactionType;
    private String sourceType;
    private String sourceNo;
    private LocalDateTime beginOccurredAt;
    private LocalDateTime endOccurredAt;
}
