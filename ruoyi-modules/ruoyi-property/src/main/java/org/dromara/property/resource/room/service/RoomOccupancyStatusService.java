package org.dromara.property.resource.room.service;

/** 根据有效房屋客户关系维护房屋入住状态。 */
public interface RoomOccupancyStatusService {

    /**
     * 按数据库中未删除且正常的 ACTIVE 关系重新计算房屋入住状态。
     * 调用方必须已经按照统一协议锁定 Room。
     *
     * @param roomId 房屋 ID
     * @return 重新计算后的状态：OCCUPIED 或 VACANT
     */
    String recalculate(Long roomId);
}
