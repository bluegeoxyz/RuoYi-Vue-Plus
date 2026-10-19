package org.dromara.property.resource.building.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.domain.bo.BuildingBo;
import org.dromara.property.resource.building.domain.vo.BuildingVo;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.building.service.IBuildingService;
import org.dromara.property.scope.PropertyBusinessDomain;
import org.dromara.property.scope.ProjectBusinessAction;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.scope.PropertyProjectScope;

/**
 * 楼栋服务；数据库记录的项目归属是所有写操作的授权依据。
 */
@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements IBuildingService {
    private final BuildingMapper buildingMapper;
    private final ProjectMapper projectMapper;
    private final PropertyProjectScopeService scopeService;

    @Override
    public BuildingVo queryById(Long id) {
        Building building = require(id);
        scopeService.assertBusinessAccess(building.getProjectId(), PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ);
        return buildingMapper.selectVoById(id);
    }

    @Override
    public PageResult<BuildingVo> queryPage(BuildingBo bo, PageQuery pageQuery) {
        PropertyProjectScope scope = scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ);
        if (scope.type() == PropertyProjectScope.Type.NONE) return PageResult.build(List.of(), 0L);
        Page<BuildingVo> page = buildingMapper.selectVoPage(pageQuery.build(), query(bo, scope));
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<BuildingVo> queryExport(BuildingBo bo) {
        PropertyProjectScope scope = scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.EXPORT);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        return buildingMapper.selectVoList(query(bo, scope));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insert(BuildingBo bo) {
        scopeService.assertBusinessAccess(bo.getProjectId(), PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
        if (projectMapper.selectById(bo.getProjectId()) == null) throw new ServiceException("项目不存在");
        validateCode(bo.getProjectId(), bo.getBuildingCode(), null);
        try {
            return buildingMapper.insert(BeanUtil.copyProperties(bo, Building.class)) > 0;
        } catch (DuplicateKeyException e) {
            throw new ServiceException("同一项目下楼栋编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(BuildingBo bo) {
        Building current = require(bo.getId());
        scopeService.assertBusinessAccess(current.getProjectId(), PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
        if (bo.getProjectId() != null && !bo.getProjectId().equals(current.getProjectId()))
            throw new ServiceException("楼栋不允许跨项目迁移");
        validateCode(current.getProjectId(), bo.getBuildingCode(), current.getId());
        Building update = BeanUtil.copyProperties(bo, Building.class);
        update.setProjectId(current.getProjectId());
        try {
            return buildingMapper.updateById(update) > 0;
        } catch (DuplicateKeyException e) {
            throw new ServiceException("同一项目下楼栋编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, String status) {
        Building current = require(id);
        scopeService.assertBusinessAccess(current.getProjectId(), PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
        Building update = new Building();
        update.setId(id);
        update.setStatus(status);
        return buildingMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Collection<Long> ids) {
        for (Long id : ids) {
            Building building = buildingMapper.selectActiveByIdForUpdate(id);
            if (building == null) throw new ServiceException("楼栋不存在");
            scopeService.assertBusinessAccess(building.getProjectId(), PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
            buildingMapper.selectUnitIdsForUpdate(id);
            buildingMapper.selectRoomIdsForUpdate(id);
            ensureNoResourceChildren(id);
        }
        return buildingMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<BuildingVo> options(BuildingBo bo) {
        PropertyProjectScope scope = scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        return buildingMapper.selectVoList(QueryBuilder.lambda(Building.class).select(Building::getId, Building::getProjectId, Building::getBuildingCode, Building::getBuildingName)
            .eqIfPresent(Building::getProjectId, bo.getProjectId()).eq(Building::getStatus, "0")
            .in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS, Building::getProjectId, scope.projectIds()).orderByAsc(Building::getSort).build());
    }

    @Override
    public List<ProjectOptionVo> projectOptions() {
        PropertyProjectScope scope = scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        if (scope.type() == PropertyProjectScope.Type.ALL) return projectMapper.selectOptions();
        return projectMapper.selectVoList(QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getProjectShortName)
            .in(Project::getId, scope.projectIds()).eq(Project::getStatus, "0")
            .orderByAsc(Project::getSort).orderByAsc(Project::getId).build(), ProjectOptionVo.class);
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Building> query(BuildingBo bo, PropertyProjectScope scope) {
        return QueryBuilder.lambda(Building.class).eqIfPresent(Building::getProjectId, bo.getProjectId()).likeIfText(Building::getBuildingCode, bo.getBuildingCode())
            .likeIfText(Building::getBuildingName, bo.getBuildingName()).eqIfText(Building::getStatus, bo.getStatus())
            .in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS, Building::getProjectId, scope.projectIds()).orderByAsc(Building::getSort).build();
    }

    private Building require(Long id) {
        Building building = buildingMapper.selectById(id);
        if (building == null) throw new ServiceException("楼栋不存在");
        return building;
    }

    private void validateCode(Long projectId, String code, Long excludeId) {
        if (buildingMapper.countActiveCode(projectId, code, excludeId) > 0)
            throw new ServiceException("同一项目下楼栋编码已存在");
    }

    private void ensureNoResourceChildren(Long buildingId) {
        if (buildingMapper.existsActiveChildren(buildingId))
            throw new ServiceException("楼栋存在未删除的单元或房屋，不能删除");
    }
}
