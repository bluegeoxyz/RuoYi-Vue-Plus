package org.dromara.warehouse.context.domain.vo;

import java.io.Serializable;

/** 仓库单据上下文中的可选库区。 */
public record DocumentZoneOptionVo(Long id, Long warehouseId, String zoneCode,
                                   String zoneName) implements Serializable {
}
