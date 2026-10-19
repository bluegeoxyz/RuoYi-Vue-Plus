package org.dromara.property.resident.context.domain.vo;

import java.io.Serializable;

/** 住户查询单元选项。 */
public record ResidentUnitOptionVo(Long id, Long projectId, Long buildingId, String unitCode,
                                   String unitName) implements Serializable {
}
