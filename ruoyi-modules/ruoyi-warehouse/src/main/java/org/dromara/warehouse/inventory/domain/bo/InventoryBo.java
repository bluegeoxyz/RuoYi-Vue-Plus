package org.dromara.warehouse.inventory.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 实时库存查询条件。 */
@Data
public class InventoryBo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long companyId;
    private Long warehouseId;
    private Long zoneId;
    private Long locationId;
    private String materialCode;
    private String materialName;
    private Long categoryId;
    private Boolean zeroStock;
    /** 仅查询安全库存阈值已启用且可用库存低于阈值的仓库物料汇总。 */
    private Boolean lowStock;
}
