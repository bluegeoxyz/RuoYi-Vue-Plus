package org.dromara.property.resource.room.service;

import java.util.*;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.resource.room.domain.bo.RoomBo;
import org.dromara.property.resource.room.domain.vo.RoomVo;

public interface IRoomService {
    RoomVo queryById(Long id);

    PageResult<RoomVo> queryPage(RoomBo bo, PageQuery pq);

    List<RoomVo> queryExport(RoomBo bo);

    boolean insert(RoomBo bo);

    boolean update(RoomBo bo);

    boolean changeStatus(Long id, String status);

    boolean delete(Collection<Long> ids);

    List<RoomVo> options(RoomBo bo);
}
