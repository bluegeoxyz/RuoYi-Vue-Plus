package org.dromara.property.customer.relation.context.domain.vo;

/** 客户关系业务可见的最小楼栋选项。 */
public record CustomerRelationBuildingOptionVo(Long id, Long projectId, String buildingCode, String buildingName) {
}
