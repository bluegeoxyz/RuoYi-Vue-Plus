package org.dromara.property.customer.relation.context.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.relation.context.domain.vo.*;
import org.dromara.property.customer.relation.context.service.CustomerRelationContextOptionService;
import org.dromara.property.customer.relation.domain.vo.CustomerReferenceOptionVo;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.*;
import org.springframework.stereotype.Service;

import java.util.List;

/** 客户关系引用数据实现；能力校验与项目/资源归属校验缺一不可。 */
@Service
@RequiredArgsConstructor
public class CustomerRelationContextOptionServiceImpl implements CustomerRelationContextOptionService {
    private final BusinessContextAccessService contextAccessService;
    private final PropertyProjectScopeService projectScopeService;
    private final ProjectMapper projectMapper;
    private final BuildingMapper buildingMapper;
    private final BuildingUnitMapper unitMapper;
    private final RoomMapper roomMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerRelationProjectOptionVo> projects(BusinessContext context) {
        contextAccessService.requireAccess(context, ReferenceType.PROJECT);
        PropertyProjectScope scope = projectScopeService.resolveBusinessScope(
            PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        var query = QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getProjectShortName)
            .eq(Project::getStatus, "0");
        if (scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS) query.in(Project::getId, scope.projectIds());
        return projectMapper.selectList(query.orderByAsc(Project::getSort).orderByAsc(Project::getId).build()).stream()
            .map(p -> new CustomerRelationProjectOptionVo(p.getId(), p.getProjectCode(), p.getProjectName(), p.getProjectShortName()))
            .toList();
    }

    @Override
    public List<CustomerRelationBuildingOptionVo> buildings(BusinessContext context, Long projectId) {
        contextAccessService.requireAccess(context, ReferenceType.BUILDING);
        requireProject(projectId);
        return buildingMapper.selectList(QueryBuilder.lambda(Building.class)
            .select(Building::getId, Building::getProjectId, Building::getBuildingCode, Building::getBuildingName)
            .eq(Building::getProjectId, projectId).eq(Building::getStatus, "0")
            .orderByAsc(Building::getSort).orderByAsc(Building::getId).build()).stream()
            .map(b -> new CustomerRelationBuildingOptionVo(b.getId(), b.getProjectId(), b.getBuildingCode(), b.getBuildingName()))
            .toList();
    }

    @Override
    public List<CustomerRelationUnitOptionVo> units(BusinessContext context, Long projectId, Long buildingId) {
        contextAccessService.requireAccess(context, ReferenceType.UNIT);
        requireBuilding(projectId, buildingId);
        return unitMapper.selectList(QueryBuilder.lambda(BuildingUnit.class)
            .select(BuildingUnit::getId, BuildingUnit::getProjectId, BuildingUnit::getBuildingId,
                BuildingUnit::getUnitCode, BuildingUnit::getUnitName)
            .eq(BuildingUnit::getProjectId, projectId).eq(BuildingUnit::getBuildingId, buildingId)
            .eq(BuildingUnit::getStatus, "0").orderByAsc(BuildingUnit::getSort).orderByAsc(BuildingUnit::getId).build()).stream()
            .map(u -> new CustomerRelationUnitOptionVo(u.getId(), u.getProjectId(), u.getBuildingId(), u.getUnitCode(), u.getUnitName()))
            .toList();
    }

    @Override
    public List<CustomerRelationRoomOptionVo> rooms(BusinessContext context, Long projectId, Long buildingId, Long unitId) {
        contextAccessService.requireAccess(context, ReferenceType.ROOM);
        requireUnit(projectId, buildingId, unitId);
        return roomMapper.selectList(QueryBuilder.lambda(Room.class)
            .select(Room::getId, Room::getProjectId, Room::getBuildingId, Room::getUnitId,
                Room::getRoomCode, Room::getRoomNo, Room::getFloorNo)
            .eq(Room::getProjectId, projectId).eq(Room::getBuildingId, buildingId).eq(Room::getUnitId, unitId)
            .eq(Room::getStatus, "0").orderByAsc(Room::getSort).orderByAsc(Room::getId).build()).stream()
            .map(r -> new CustomerRelationRoomOptionVo(r.getId(), r.getProjectId(), r.getBuildingId(), r.getUnitId(),
                r.getRoomCode(), r.getRoomNo(), r.getFloorNo())).toList();
    }

    @Override
    public List<CustomerReferenceOptionVo> customers(BusinessContext context, Long projectId, String keyword) {
        contextAccessService.requireAccess(context, ReferenceType.CUSTOMER);
        requireProject(projectId);
        if (StringUtils.isBlank(keyword) || keyword.trim().length() < 2) return List.of();
        return customerMapper.selectList(QueryBuilder.lambda(Customer.class)
            .and(q -> q.like(Customer::getCustomerNo, keyword.trim()).or().like(Customer::getCustomerName, keyword.trim()))
            .eq(Customer::getLifecycleStatus, "ACTIVE").eq(Customer::getStatus, "0").last("LIMIT 20").build()).stream()
            .map(c -> new CustomerReferenceOptionVo(c.getId(), c.getCustomerNo(), c.getCustomerType(),
                maskName(c), maskMobile(c.getMobile()))).toList();
    }

    private void requireProject(Long projectId) {
        if (projectId == null) throw new ServiceException("项目不能为空");
        projectScopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.OPTION);
        Project project = projectMapper.selectById(projectId);
        if (project == null || !"0".equals(project.getStatus())) throw new ServiceException("项目无效、已停用或不在授权范围");
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
            || !"0".equals(unit.getStatus())) throw new ServiceException("单元无效、已停用或不属于当前楼栋");
    }

    private String maskName(Customer customer) {
        if ("INDIVIDUAL".equals(customer.getCustomerType())) {
            return DesensitizedUtil.desensitized(customer.getCustomerName(), DesensitizedUtil.DesensitizedType.CHINESE_NAME);
        }
        return customer.getCustomerName();
    }

    private String maskMobile(String mobile) {
        return StringUtils.isBlank(mobile) ? mobile
            : DesensitizedUtil.desensitized(mobile, DesensitizedUtil.DesensitizedType.MOBILE_PHONE);
    }
}
