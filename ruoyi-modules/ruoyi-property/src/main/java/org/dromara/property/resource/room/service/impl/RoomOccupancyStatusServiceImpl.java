package org.dromara.property.resource.room.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.customer.relation.mapper.RoomCustomerRelationMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.room.service.RoomOccupancyStatusService;
import org.springframework.stereotype.Service;

/** 房屋入住状态统一计算实现。 */
@Service
@RequiredArgsConstructor
public class RoomOccupancyStatusServiceImpl implements RoomOccupancyStatusService {
    private final RoomCustomerRelationMapper relationMapper;
    private final RoomMapper roomMapper;

    @Override
    public String recalculate(Long roomId) {
        String status = relationMapper.selectActiveOccupancyRelationIdsForUpdate(roomId).isEmpty() ? "VACANT" : "OCCUPIED";
        Room update = new Room();
        update.setId(roomId);
        update.setOccupancyStatus(status);
        if (roomMapper.updateById(update) != 1) {
            throw new ServiceException("房屋入住状态更新失败");
        }
        return status;
    }
}
