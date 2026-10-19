package org.dromara.warehouse.location.service.impl;

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
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.domain.bo.WarehouseLocationBo;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationOptionVo;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationVo;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.location.service.IWarehouseLocationService;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.transaction.mapper.InventoryTransactionMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 库位服务实现。 */
@Service
@RequiredArgsConstructor
public class WarehouseLocationServiceImpl implements IWarehouseLocationService {
    private final WarehouseLocationMapper locationMapper;
    private final WarehouseZoneMapper zoneMapper;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseAccessService accessService;
    private final DictService dictService;
    private final InventoryMapper inventoryMapper;
    private final InventoryTransactionMapper transactionMapper;

    @Override
    public WarehouseLocationVo queryById(Long id) {
        WarehouseLocation location = requireLocation(id);
        WarehouseLocationVo vo = locationMapper.selectVoById(id);
        enrich(List.of(vo));
        return vo;
    }

    @Override
    public PageResult<WarehouseLocationVo> queryPageList(
            WarehouseLocationBo bo, PageQuery pageQuery) {
        List<Long> warehouseIds = accessService.accessibleWarehouseIds();
        if (warehouseIds.isEmpty()) return PageResult.build(List.of(), 0L);
        LambdaQueryWrapper<WarehouseLocation> wrapper =
                QueryBuilder.lambda(WarehouseLocation.class)
                        .in(WarehouseLocation::getWarehouseId, warehouseIds)
                        .eqIfPresent(WarehouseLocation::getWarehouseId, bo.getWarehouseId())
                        .eqIfPresent(WarehouseLocation::getZoneId, bo.getZoneId())
                        .likeIfText(WarehouseLocation::getLocationCode, bo.getLocationCode())
                        .likeIfText(WarehouseLocation::getLocationName, bo.getLocationName())
                        .eqIfText(WarehouseLocation::getStatus, bo.getStatus())
                        .orderByAsc(WarehouseLocation::getSort)
                        .orderByDesc(WarehouseLocation::getId)
                        .build();
        Page<WarehouseLocationVo> result = locationMapper.selectVoPage(pageQuery.build(), wrapper);
        enrich(result.getRecords());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public Boolean insertByBo(WarehouseLocationBo bo) {
        normalize(bo);
        validateBeforeSave(bo, null, true);
        WarehouseLocation entity = BeanUtil.copyProperties(bo, WarehouseLocation.class);
        try {
            boolean success = locationMapper.insert(entity) > 0;
            if (success) bo.setId(entity.getId());
            return success;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("同一仓库内库位编码已存在");
        }
    }

    @Override
    public Boolean updateByBo(WarehouseLocationBo bo) {
        requireLocation(bo.getId());
        normalize(bo);
        validateBeforeSave(bo, bo.getId(), false);
        try {
            return locationMapper.updateById(BeanUtil.copyProperties(bo, WarehouseLocation.class))
                    > 0;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("同一仓库内库位编码已存在");
        }
    }

    @Override
    public Boolean changeStatus(Long id, String status) {
        requireLocation(id);
        validateStatus(status);
        WarehouseLocation update = new WarehouseLocation();
        update.setId(id);
        update.setStatus(status);
        return locationMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        LinkedHashSet<Long> distinct = new LinkedHashSet<>(ids);
        distinct.forEach(this::requireLocation);
        if (transactionMapper.existsByLocationIds(distinct))
            throw new ServiceException("库位存在库存流水，不能删除，请停用库位");
        if (inventoryMapper.existsByLocationIds(distinct))
            throw new ServiceException("库位存在库存余额，不能删除，请停用库位");
        return locationMapper.deleteByIds(distinct) > 0;
    }

    @Override
    public List<WarehouseLocationOptionVo> selectOptions(Long warehouseId, Long zoneId) {
        accessService.requireWarehouse(warehouseId);
        if (zoneId != null) validateZoneRelation(warehouseId, zoneId, false);
        return locationMapper.selectOptions(warehouseId, zoneId);
    }

    private WarehouseLocation requireLocation(Long id) {
        WarehouseLocation location = locationMapper.selectById(id);
        if (location == null) throw new ServiceException("库位不存在");
        accessService.requireWarehouse(location.getWarehouseId());
        return location;
    }

    private void normalize(WarehouseLocationBo bo) {
        bo.setLocationCode(StringUtils.trim(bo.getLocationCode()).toUpperCase());
        bo.setLocationName(StringUtils.trim(bo.getLocationName()));
        if (bo.getStatus() == null) bo.setStatus(SystemConstants.NORMAL);
        if (bo.getSort() == null) bo.setSort(0);
    }

    private void validateBeforeSave(WarehouseLocationBo bo, Long excludeId, boolean insert) {
        if (insert) accessService.requireEnabledWarehouse(bo.getWarehouseId());
        else accessService.requireWarehouse(bo.getWarehouseId());
        validateZoneRelation(bo.getWarehouseId(), bo.getZoneId(), insert);
        validateStatus(bo.getStatus());
        if (locationMapper.countByCode(bo.getWarehouseId(), bo.getLocationCode(), excludeId) > 0)
            throw new ServiceException("同一仓库内库位编码已存在");
    }

    private void validateZoneRelation(Long warehouseId, Long zoneId, boolean requireEnabled) {
        WarehouseZone zone = zoneMapper.selectById(zoneId);
        if (zone == null) throw new ServiceException("库区不存在");
        if (!warehouseId.equals(zone.getWarehouseId())) throw new ServiceException("库区与仓库归属不一致");
        if (requireEnabled && !SystemConstants.NORMAL.equals(zone.getStatus()))
            throw new ServiceException("库区已停用，不允许新增库位");
    }

    private void validateStatus(String status) {
        if (!dictService.getAllDictByDictType("sys_normal_disable").containsKey(status))
            throw new ServiceException("状态不合法");
    }

    private void enrich(List<WarehouseLocationVo> rows) {
        List<Long> warehouseIds =
                rows.stream().map(WarehouseLocationVo::getWarehouseId).distinct().toList();
        List<Long> zoneIds = rows.stream().map(WarehouseLocationVo::getZoneId).distinct().toList();
        Map<Long, Warehouse> warehouses =
                warehouseMapper.selectByIds(warehouseIds).stream()
                        .collect(Collectors.toMap(Warehouse::getId, Function.identity()));
        Map<Long, WarehouseZone> zones =
                zoneMapper.selectByIds(zoneIds).stream()
                        .collect(Collectors.toMap(WarehouseZone::getId, Function.identity()));
        rows.forEach(
                row -> {
                    Warehouse warehouse = warehouses.get(row.getWarehouseId());
                    WarehouseZone zone = zones.get(row.getZoneId());
                    if (warehouse != null) row.setWarehouseName(warehouse.getWarehouseName());
                    if (zone != null) row.setZoneName(zone.getZoneName());
                });
    }
}
