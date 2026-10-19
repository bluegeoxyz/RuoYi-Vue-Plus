package org.dromara.warehouse.inbound.domain.vo;

import java.io.Serializable;

/** 入库制单可选库区。 */
public record InboundZoneOptionVo(
    Long id,
    Long warehouseId,
    String zoneCode,
    String zoneName
) implements Serializable {
}
