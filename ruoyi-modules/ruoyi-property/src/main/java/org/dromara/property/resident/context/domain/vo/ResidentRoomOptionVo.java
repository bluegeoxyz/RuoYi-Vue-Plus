package org.dromara.property.resident.context.domain.vo;

import java.io.Serializable;

/** 住户查询房屋选项。 */
public record ResidentRoomOptionVo(Long id, Long projectId, Long buildingId, Long unitId,
                                   String roomCode, String roomNo, Integer floorNo) implements Serializable {
}
