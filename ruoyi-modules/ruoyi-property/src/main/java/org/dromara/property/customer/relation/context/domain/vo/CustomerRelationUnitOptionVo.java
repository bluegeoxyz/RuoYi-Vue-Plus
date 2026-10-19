package org.dromara.property.customer.relation.context.domain.vo;

/** 客户关系业务可见的最小单元选项。 */
public record CustomerRelationUnitOptionVo(Long id, Long projectId, Long buildingId, String unitCode, String unitName) {
}
