package org.dromara.warehouse.context.domain.vo;

import java.io.Serializable;

/** 仓库单据上下文中的可选物料及基础单位。 */
public record DocumentMaterialOptionVo(Long id, Long companyId, String materialCode, String materialName,
                                       Long baseUnitId, String specification, String model, String unitName,
                                       String unitSymbol, Integer precisionScale) implements Serializable {
}
