package org.dromara.warehouse.inbound.domain.vo;

import java.io.Serializable;

/** 入库制单可选仓库。 */
public record InboundWarehouseOptionVo(
    Long id,
    Long companyId,
    Long projectId,
    String warehouseScope,
    String warehouseCode,
    String warehouseName
) implements Serializable {
}
