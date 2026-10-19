package org.dromara.property.resource.room.mapper;

import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.domain.vo.RoomVo;

import java.util.List;

public interface RoomMapper extends BaseMapperPlus<Room, RoomVo> {
    @Select("SELECT * FROM pm_room WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    Room selectActiveByIdForUpdate(@Param("id") Long id);

    @Select("SELECT * FROM pm_room WHERE project_id=#{projectId} AND room_code=#{code} AND del_flag='0' FOR UPDATE")
    Room selectActiveByProjectAndCodeForUpdate(@Param("projectId") Long projectId, @Param("code") String code);

    @Select("SELECT id FROM pm_room_customer_rel FORCE INDEX (idx_pm_room_customer_room) WHERE room_id=#{id} " +
        "AND relation_status IN ('PENDING','ACTIVE') AND status='0' AND del_flag='0' FOR UPDATE")
    List<Long> selectEffectiveCustomerRelationIdsForUpdate(@Param("id") Long id);

    default long countActiveCode(Long projectId, String code, Long excludeId) {
        return lambda().eq(Room::getProjectId, projectId).eq(Room::getRoomCode, code).ne(excludeId != null, Room::getId, excludeId).count();
    }
}
