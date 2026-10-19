package org.dromara.warehouse.context.domain.vo;

import java.io.Serializable;

/** 仓库单据上下文中的可选库位。 */
public record DocumentLocationOptionVo(Long id, Long warehouseId, Long zoneId, String zoneName,
                                       String locationCode, String locationName) implements Serializable {
}
