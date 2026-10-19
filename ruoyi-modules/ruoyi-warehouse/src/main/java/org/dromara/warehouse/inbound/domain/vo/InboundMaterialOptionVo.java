package org.dromara.warehouse.inbound.domain.vo;

import java.io.Serializable;

/** 入库制单可选物料及其基础单位。 */
public record InboundMaterialOptionVo(
    Long id,
    Long companyId,
    String materialCode,
    String materialName,
    Long baseUnitId,
    String specification,
    String model,
    String unitName,
    String unitSymbol,
    Integer precisionScale
) implements Serializable {
}
