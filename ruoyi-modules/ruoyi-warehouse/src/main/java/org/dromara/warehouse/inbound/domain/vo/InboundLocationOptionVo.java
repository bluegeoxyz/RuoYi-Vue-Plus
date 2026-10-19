package org.dromara.warehouse.inbound.domain.vo;

import java.io.Serializable;

/** 入库制单可选库位。 */
public record InboundLocationOptionVo(
    Long id,
    Long warehouseId,
    Long zoneId,
    String zoneName,
    String locationCode,
    String locationName
) implements Serializable {
}
