package org.dromara.property.resident.context.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resident.context.domain.vo.ResidentBuildingOptionVo;
import org.dromara.property.resident.context.domain.vo.ResidentRoomOptionVo;
import org.dromara.property.resident.context.domain.vo.ResidentUnitOptionVo;
import org.dromara.property.resident.context.service.ResidentContextOptionService;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.ProjectBusinessAction;
import org.dromara.property.scope.PropertyBusinessDomain;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.springframework.stereotype.Service;

import java.util.List;

/** 住户查询选项实现；同时校验业务能力、项目范围和资源父子归属。 */
@Service
@RequiredArgsConstructor
public class ResidentContextOptionServiceImpl implements ResidentContextOptionService {

    private final BusinessContextAccessService contextAccessService;
    private final PropertyProjectScopeService projectScopeService;
    private final ProjectMapper projectMapper;
    private final BuildingMapper buildingMapper;
    private final BuildingUnitMapper unitMapper;
    private final RoomMapper roomMapper;

    @Override
    public List<ResidentBuildingOptionVo> buildings(Long projectId) {
        contextAccessService.requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.BUILDING);
        requireProject(projectId);
        return buildingMapper.selectList(QueryBuilder.lambda(Building.class)
            .select(Building::getId, Building::getProjectId, Building::getBuildingCode, Building::getBuildingName)
            .eq(Building::getProjectId, projectId).eq(Building::getStatus, "0")
            .orderByAsc(Building::getSort).orderByAsc(Building::getId).build()).stream()
            .map(building -> new ResidentBuildingOptionVo(building.getId(), building.getProjectId(),
                building.getBuildingCode(), building.getBuildingName()))
            .toList();
    }

    @Override
    public List<ResidentUnitOptionVo> units(Long projectId, Long buildingId) {
        contextAccessService.requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.UNIT);
        requireBuilding(projectId, buildingId);
        return unitMapper.selectList(QueryBuilder.lambda(BuildingUnit.class)
            .select(BuildingUnit::getId, BuildingUnit::getProjectId, BuildingUnit::getBuildingId,
                BuildingUnit::getUnitCode, BuildingUnit::getUnitName)
            .eq(BuildingUnit::getProjectId, projectId).eq(BuildingUnit::getBuildingId, buildingId)
            .eq(BuildingUnit::getStatus, "0").orderByAsc(BuildingUnit::getSort)
            .orderByAsc(BuildingUnit::getId).build()).stream()
            .map(unit -> new ResidentUnitOptionVo(unit.getId(), unit.getProjectId(), unit.getBuildingId(),
                unit.getUnitCode(), unit.getUnitName()))
            .toList();
    }

    @Override
    public List<ResidentRoomOptionVo> rooms(Long projectId, Long buildingId, Long unitId) {
        contextAccessService.requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.ROOM);
        requireUnit(projectId, buildingId, unitId);
        return roomMapper.selectList(QueryBuilder.lambda(Room.class)
            .select(Room::getId, Room::getProjectId, Room::getBuildingId, Room::getUnitId,
                Room::getRoomCode, Room::getRoomNo, Room::getFloorNo)
            .eq(Room::getProjectId, projectId).eq(Room::getBuildingId, buildingId)
            .eq(Room::getUnitId, unitId).eq(Room::getStatus, "0")
            .orderByAsc(Room::getSort).orderByAsc(Room::getId).build()).stream()
            .map(room -> new ResidentRoomOptionVo(room.getId(), room.getProjectId(), room.getBuildingId(),
                room.getUnitId(), room.getRoomCode(), room.getRoomNo(), room.getFloorNo()))
            .toList();
    }

    private void requireProject(Long projectId) {
        if (projectId == null) throw new ServiceException("项目不能为空");
        projectScopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.CUSTOMER_RELATION,
            ProjectBusinessAction.OPTION);
        Project project = projectMapper.selectById(projectId);
        if (project == null || !"0".equals(project.getStatus())) {
            throw new ServiceException("项目无效、已停用或不在授权范围");
        }
    }

    private void requireBuilding(Long projectId, Long buildingId) {
        requireProject(projectId);
        Building building = buildingId == null ? null : buildingMapper.selectById(buildingId);
        if (building == null || !projectId.equals(building.getProjectId()) || !"0".equals(building.getStatus())) {
            throw new ServiceException("楼栋无效、已停用或不属于当前项目");
        }
    }

    private void requireUnit(Long projectId, Long buildingId, Long unitId) {
        requireBuilding(projectId, buildingId);
        BuildingUnit unit = unitId == null ? null : unitMapper.selectById(unitId);
        if (unit == null || !projectId.equals(unit.getProjectId()) || !buildingId.equals(unit.getBuildingId())
            || !"0".equals(unit.getStatus())) {
            throw new ServiceException("单元无效、已停用或不属于当前楼栋");
        }
    }
}
