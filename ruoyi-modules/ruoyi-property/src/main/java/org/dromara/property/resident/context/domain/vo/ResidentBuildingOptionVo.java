package org.dromara.property.resident.context.domain.vo;

import java.io.Serializable;

/** 住户查询楼栋选项。 */
public record ResidentBuildingOptionVo(Long id, Long projectId, String buildingCode,
                                       String buildingName) implements Serializable {
}
