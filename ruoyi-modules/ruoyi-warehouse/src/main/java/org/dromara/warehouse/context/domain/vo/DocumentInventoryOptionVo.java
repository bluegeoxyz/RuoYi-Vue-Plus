package org.dromara.warehouse.context.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/** 仓库单据上下文中单一仓库、库位、物料维度的库存快照。 */
public record DocumentInventoryOptionVo(Long warehouseId, Long locationId, Long materialId,
                                        BigDecimal quantity, BigDecimal lockedQuantity,
                                        BigDecimal availableQuantity) implements Serializable {
}
