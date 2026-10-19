package org.dromara.property.resource.unit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.domain.bo.BuildingUnitBo;
import org.dromara.property.resource.unit.domain.vo.BuildingUnitVo;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.resource.unit.service.IBuildingUnitService;
import org.dromara.property.scope.ProjectBusinessAction;
import org.dromara.property.scope.PropertyBusinessDomain;
import org.dromara.property.scope.PropertyProjectScope;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 楼栋单元服务；数据库中的项目和楼栋归属是写操作的授权依据。
 */
@Service
@RequiredArgsConstructor
public class BuildingUnitServiceImpl implements IBuildingUnitService {
    private final BuildingUnitMapper unitMapper;
    private final BuildingMapper buildingMapper;
    private final ProjectMapper projectMapper;
    private final PropertyProjectScopeService scopeService;

    @Override
    public BuildingUnitVo queryById(Long id) {
        BuildingUnit unit = require(id);
        assertAccess(unit.getProjectId(), ProjectBusinessAction.READ);
        return unitMapper.selectVoById(id);
    }

    @Override
    public PageResult<BuildingUnitVo> queryPage(BuildingUnitBo bo, PageQuery pageQuery) {
        PropertyProjectScope scope = resolve(ProjectBusinessAction.READ);
        if (scope.type() == PropertyProjectScope.Type.NONE) {
            return PageResult.build(List.of(), 0L);
        }
        Page<BuildingUnitVo> page = unitMapper.selectVoPage(pageQuery.build(), query(bo, scope));
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<BuildingUnitVo> queryExport(BuildingUnitBo bo) {
        PropertyProjectScope scope = resolve(ProjectBusinessAction.EXPORT);
        if (scope.type() == PropertyProjectScope.Type.NONE) {
            return List.of();
        }
        return unitMapper.selectVoList(query(bo, scope));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insert(BuildingUnitBo bo) {
        assertAccess(bo.getProjectId(), ProjectBusinessAction.WRITE);
        validateProjectAndLockedBuilding(bo.getProjectId(), bo.getBuildingId());
        validateCode(bo.getBuildingId(), bo.getUnitCode(), null);
        try {
            return unitMapper.insert(BeanUtil.copyProperties(bo, BuildingUnit.class)) > 0;
        } catch (DuplicateKeyException e) {
            throw new ServiceException("同一楼栋下单元编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(BuildingUnitBo bo) {
        BuildingUnit current = require(bo.getId());
        assertAccess(current.getProjectId(), ProjectBusinessAction.WRITE);
        if (bo.getProjectId() != null && !bo.getProjectId().equals(current.getProjectId())) {
            throw new ServiceException("单元不允许跨项目迁移");
        }
        if (bo.getBuildingId() != null && !bo.getBuildingId().equals(current.getBuildingId())) {
            throw new ServiceException("单元不允许通过普通编辑变更所属楼栋");
        }
        validateCode(current.getBuildingId(), bo.getUnitCode(), current.getId());
        BuildingUnit update = BeanUtil.copyProperties(bo, BuildingUnit.class);
        update.setProjectId(current.getProjectId());
        update.setBuildingId(current.getBuildingId());
        try {
            return unitMapper.updateById(update) > 0;
        } catch (DuplicateKeyException e) {
            throw new ServiceException("同一楼栋下单元编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, String status) {
        BuildingUnit current = require(id);
        assertAccess(current.getProjectId(), ProjectBusinessAction.WRITE);
        BuildingUnit update = new BuildingUnit();
        update.setId(id);
        update.setStatus(status);
        return unitMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("单元ID不能为空");
        }
        for (Long id : ids) {
            BuildingUnit snapshot = require(id);
            Building building = buildingMapper.selectActiveByIdForUpdate(snapshot.getBuildingId());
            if (building == null) {
                throw new ServiceException("楼栋不存在");
            }
            BuildingUnit unit = unitMapper.selectActiveByIdForUpdate(id);
            if (unit == null) {
                throw new ServiceException("单元不存在");
            }
            if (!building.getId().equals(unit.getBuildingId()) || !building.getProjectId().equals(unit.getProjectId())) {
                throw new ServiceException("单元归属已发生变化，请重试");
            }
            assertAccess(unit.getProjectId(), ProjectBusinessAction.WRITE);
            unitMapper.selectRoomIdsForUpdate(id);
            if (unitMapper.existsActiveRoom(id)) {
                throw new ServiceException("单元存在未删除的房屋，不能删除");
            }
        }
        return unitMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<BuildingUnitVo> options(BuildingUnitBo bo) {
        PropertyProjectScope scope = resolve(ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) {
            return List.of();
        }
        return unitMapper.selectVoList(QueryBuilder.lambda(BuildingUnit.class)
            .select(BuildingUnit::getId, BuildingUnit::getProjectId, BuildingUnit::getBuildingId,
                BuildingUnit::getUnitCode, BuildingUnit::getUnitName)
            .eqIfPresent(BuildingUnit::getProjectId, bo.getProjectId())
            .eqIfPresent(BuildingUnit::getBuildingId, bo.getBuildingId())
            .eq(BuildingUnit::getStatus, "0")
            .in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS,
                BuildingUnit::getProjectId, scope.projectIds())
            .orderByAsc(BuildingUnit::getSort).orderByAsc(BuildingUnit::getId).build());
    }

    private LambdaQueryWrapper<BuildingUnit> query(BuildingUnitBo bo, PropertyProjectScope scope) {
        return QueryBuilder.lambda(BuildingUnit.class)
            .eqIfPresent(BuildingUnit::getProjectId, bo.getProjectId())
            .eqIfPresent(BuildingUnit::getBuildingId, bo.getBuildingId())
            .likeIfText(BuildingUnit::getUnitCode, bo.getUnitCode())
            .likeIfText(BuildingUnit::getUnitName, bo.getUnitName())
            .eqIfText(BuildingUnit::getStatus, bo.getStatus())
            .in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS,
                BuildingUnit::getProjectId, scope.projectIds())
            .orderByAsc(BuildingUnit::getSort).orderByAsc(BuildingUnit::getId).build();
    }

    private void validateProjectAndLockedBuilding(Long projectId, Long buildingId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || !"0".equals(project.getStatus())) {
            throw new ServiceException("项目不存在或已停用");
        }
        Building building = buildingMapper.selectActiveByIdForUpdate(buildingId);
        if (building == null || !"0".equals(building.getStatus())) {
            throw new ServiceException("楼栋不存在或已停用");
        }
        if (!projectId.equals(building.getProjectId())) {
            throw new ServiceException("楼栋不属于目标项目");
        }
    }

    private BuildingUnit require(Long id) {
        BuildingUnit unit = unitMapper.selectById(id);
        if (unit == null) {
            throw new ServiceException("单元不存在");
        }
        return unit;
    }

    private void validateCode(Long buildingId, String code, Long excludeId) {
        if (unitMapper.countActiveCode(buildingId, code, excludeId) > 0) {
            throw new ServiceException("同一楼栋下单元编码已存在");
        }
    }

    private PropertyProjectScope resolve(ProjectBusinessAction action) {
        return scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, action);
    }

    private void assertAccess(Long projectId, ProjectBusinessAction action) {
        scopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.RESOURCE, action);
    }
}
