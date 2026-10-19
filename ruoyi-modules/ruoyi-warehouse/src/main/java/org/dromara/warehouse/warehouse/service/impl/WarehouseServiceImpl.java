package org.dromara.warehouse.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import cn.hutool.core.bean.BeanUtil;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.api.ProjectReferenceDTO;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.system.domain.bo.SysDeptBo;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.transaction.mapper.InventoryTransactionMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.domain.bo.WarehouseBo;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseOptionVo;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseVo;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.warehouse.service.IWarehouseService;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 仓库服务实现。 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements IWarehouseService {
    private static final String DICT_SCOPE = "wm_warehouse_scope";
    private static final String DICT_STATUS = "sys_normal_disable";
    private final WarehouseMapper warehouseMapper;
    private final WarehouseZoneMapper zoneMapper;
    private final WarehouseAccessService accessService;
    private final ProjectReferenceService projectReferenceService;
    private final ISysDeptService deptService;
    private final DictService dictService;
    private final InventoryMapper inventoryMapper;
    private final InventoryTransactionMapper transactionMapper;

    @Override
    public WarehouseVo queryById(Long id) {
        accessService.requireWarehouse(id);
        WarehouseVo result = warehouseMapper.selectVoById(id);
        enrichProjects(List.of(result));
        return result;
    }

    @Override
    public PageResult<WarehouseVo> queryPageList(WarehouseBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Warehouse> wrapper = buildQueryWrapper(bo);
        Page<WarehouseVo> result = warehouseMapper.selectVoPage(pageQuery.build(), wrapper);
        enrichProjects(result.getRecords());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    private LambdaQueryWrapper<Warehouse> buildQueryWrapper(WarehouseBo bo) {
        LambdaQueryWrapper<Warehouse> wrapper = QueryBuilder.lambda(Warehouse.class)
            .eqIfPresent(Warehouse::getCompanyId, bo.getCompanyId())
            .eqIfPresent(Warehouse::getProjectId, bo.getProjectId())
            .eqIfText(Warehouse::getWarehouseScope, bo.getWarehouseScope())
            .likeIfText(Warehouse::getWarehouseCode, bo.getWarehouseCode())
            .likeIfText(Warehouse::getWarehouseName, bo.getWarehouseName())
            .eqIfText(Warehouse::getStatus, bo.getStatus())
            .orderByAsc(Warehouse::getSort).orderByDesc(Warehouse::getId).build();
        accessService.applyScope(wrapper);
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(WarehouseBo bo) {
        normalize(bo);
        validateBeforeSave(bo, null);
        Warehouse entity = BeanUtil.copyProperties(bo, Warehouse.class);
        try {
            boolean success = warehouseMapper.insert(entity) > 0;
            if (success) bo.setId(entity.getId());
            return success;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("同一公司内仓库编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(WarehouseBo bo) {
        accessService.requireWarehouse(bo.getId());
        normalize(bo);
        validateBeforeSave(bo, bo.getId());
        try {
            return warehouseMapper.updateById(BeanUtil.copyProperties(bo, Warehouse.class)) > 0;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("同一公司内仓库编码已存在");
        }
    }

    @Override
    public Boolean changeStatus(Long id, String status) {
        accessService.requireWarehouse(id);
        validateDict(DICT_STATUS, status, "状态");
        Warehouse update = new Warehouse(); update.setId(id); update.setStatus(status);
        return warehouseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        LinkedHashSet<Long> distinct = new LinkedHashSet<>(ids);
        distinct.forEach(accessService::requireWarehouse);
        if (transactionMapper.existsByWarehouseIds(distinct)) throw new ServiceException("仓库存在库存流水，不能删除，请停用仓库");
        if (inventoryMapper.existsByWarehouseIds(distinct)) throw new ServiceException("仓库存在库存余额，不能删除，请停用仓库");
        if (zoneMapper.existsByWarehouseIds(distinct)) throw new ServiceException("仓库下存在未删除库区，不能删除");
        return warehouseMapper.deleteByIds(distinct) > 0;
    }

    @Override
    public List<WarehouseOptionVo> selectOptions() {
        return warehouseMapper.selectOptions(accessService.accessibleWarehouseIds());
    }

    @Override
    public List<SysDeptVo> selectCompanyOptions() {
        SysDeptBo query = new SysDeptBo();
        query.setStatus(SystemConstants.NORMAL);
        return deptService.selectDeptList(query);
    }

    @Override
    public List<ProjectReferenceDTO> selectProjectOptions() {
        return projectReferenceService.selectWarehouseOptions();
    }

    private void normalize(WarehouseBo bo) {
        bo.setWarehouseScope(StringUtils.trim(bo.getWarehouseScope()).toUpperCase());
        bo.setWarehouseCode(StringUtils.trim(bo.getWarehouseCode()).toUpperCase());
        bo.setWarehouseName(StringUtils.trim(bo.getWarehouseName()));
        bo.setContactPhone(StringUtils.trim(bo.getContactPhone()));
        bo.setAddress(StringUtils.trim(bo.getAddress()));
        if (bo.getStatus() == null) bo.setStatus(SystemConstants.NORMAL);
        if (bo.getSort() == null) bo.setSort(0);
    }

    private void validateBeforeSave(WarehouseBo bo, Long excludeId) {
        validateDict(DICT_SCOPE, bo.getWarehouseScope(), "归属类型");
        validateDict(DICT_STATUS, bo.getStatus(), "状态");
        accessService.validateCompany(bo.getCompanyId());
        if ("COMPANY".equals(bo.getWarehouseScope())) {
            if (bo.getProjectId() != null) throw new ServiceException("公司级仓库不能选择项目");
        } else if ("PROJECT".equals(bo.getWarehouseScope())) {
            if (bo.getProjectId() == null) throw new ServiceException("项目仓库必须选择项目");
            accessService.validateProject(bo.getProjectId());
        }
        if (warehouseMapper.countByCode(bo.getCompanyId(), bo.getWarehouseCode(), excludeId) > 0) {
            throw new ServiceException("同一公司内仓库编码已存在");
        }
    }

    private void validateDict(String type, String value, String label) {
        Map<String, String> dict = dictService.getAllDictByDictType(type);
        if (StringUtils.isBlank(value) || !dict.containsKey(value)) throw new ServiceException(label + "不合法");
    }

    private void enrichProjects(List<WarehouseVo> rows) {
        List<Long> ids = rows.stream().map(WarehouseVo::getProjectId).filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return;
        Map<Long, ProjectReferenceDTO> projects = projectReferenceService.queryByIds(ids).stream()
            .collect(Collectors.toMap(ProjectReferenceDTO::id, Function.identity()));
        rows.forEach(row -> { ProjectReferenceDTO project = projects.get(row.getProjectId()); if (project != null) row.setProjectName(project.projectName()); });
    }
}
