package org.dromara.property.resident.context.service;

import org.dromara.property.resident.context.domain.vo.ResidentBuildingOptionVo;
import org.dromara.property.resident.context.domain.vo.ResidentRoomOptionVo;
import org.dromara.property.resident.context.domain.vo.ResidentUnitOptionVo;

import java.util.List;

/** 住户只读页面引用数据服务。 */
public interface ResidentContextOptionService {

    List<ResidentBuildingOptionVo> buildings(Long projectId);

    List<ResidentUnitOptionVo> units(Long projectId, Long buildingId);

    List<ResidentRoomOptionVo> rooms(Long projectId, Long buildingId, Long unitId);
}
