package org.dromara.property.charge.context.domain.vo;

/** 房屋收费设置房屋选项。 */
public record ChargeBindingRoomOptionVo(Long id, Long projectId, Long buildingId, Long unitId,
                                        String roomCode, String roomNo, Integer floorNo) {
}
