package org.dromara.warehouse.context.domain.vo;

import java.io.Serializable;

/** 仓库单据上下文中的可选仓库。 */
public record DocumentWarehouseOptionVo(Long id, Long companyId, Long projectId, String warehouseScope,
                                        String warehouseCode, String warehouseName) implements Serializable {
}
