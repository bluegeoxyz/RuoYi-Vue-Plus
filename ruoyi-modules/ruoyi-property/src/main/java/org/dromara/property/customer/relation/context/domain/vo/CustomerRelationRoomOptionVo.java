package org.dromara.property.customer.relation.context.domain.vo;

/** 客户关系业务可见的最小房屋选项。 */
public record CustomerRelationRoomOptionVo(Long id, Long projectId, Long buildingId, Long unitId,
                                           String roomCode, String roomNo, Integer floorNo) {
}
