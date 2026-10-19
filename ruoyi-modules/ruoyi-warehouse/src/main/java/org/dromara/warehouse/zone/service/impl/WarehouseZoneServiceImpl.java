package org.dromara.warehouse.zone.service.impl;

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
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.domain.bo.WarehouseZoneBo;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneOptionVo;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneVo;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.dromara.warehouse.zone.service.IWarehouseZoneService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 库区服务实现。 */
@Service
@RequiredArgsConstructor
public class WarehouseZoneServiceImpl implements IWarehouseZoneService {
    private final WarehouseZoneMapper zoneMapper;
    private final WarehouseLocationMapper locationMapper;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseAccessService accessService;
    private final DictService dictService;

    @Override
    public WarehouseZoneVo queryById(Long id) {
        WarehouseZone zone = requireZone(id);
        WarehouseZoneVo vo = zoneMapper.selectVoById(id);
        vo.setWarehouseName(accessService.requireWarehouse(zone.getWarehouseId()).getWarehouseName());
        return vo;
    }

    @Override
    public PageResult<WarehouseZoneVo> queryPageList(WarehouseZoneBo bo, PageQuery pageQuery) {
        List<Long> warehouseIds = accessService.accessibleWarehouseIds();
        if (warehouseIds.isEmpty()) return PageResult.build(List.of(), 0L);
        LambdaQueryWrapper<WarehouseZone> wrapper = QueryBuilder.lambda(WarehouseZone.class)
            .in(WarehouseZone::getWarehouseId, warehouseIds)
            .eqIfPresent(WarehouseZone::getWarehouseId, bo.getWarehouseId())
            .likeIfText(WarehouseZone::getZoneCode, bo.getZoneCode())
            .likeIfText(WarehouseZone::getZoneName, bo.getZoneName())
            .eqIfText(WarehouseZone::getStatus, bo.getStatus())
            .orderByAsc(WarehouseZone::getSort).orderByDesc(WarehouseZone::getId).build();
        Page<WarehouseZoneVo> result = zoneMapper.selectVoPage(pageQuery.build(), wrapper);
        enrichWarehouses(result.getRecords());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public Boolean insertByBo(WarehouseZoneBo bo) {
        normalize(bo); validateBeforeSave(bo, null, true);
        WarehouseZone entity = BeanUtil.copyProperties(bo, WarehouseZone.class);
        try { boolean success = zoneMapper.insert(entity) > 0; if (success) bo.setId(entity.getId()); return success; }
        catch (DuplicateKeyException ex) { throw new ServiceException("同一仓库内库区编码已存在"); }
    }

    @Override
    public Boolean updateByBo(WarehouseZoneBo bo) {
        requireZone(bo.getId()); normalize(bo); validateBeforeSave(bo, bo.getId(), false);
        try { return zoneMapper.updateById(BeanUtil.copyProperties(bo, WarehouseZone.class)) > 0; }
        catch (DuplicateKeyException ex) { throw new ServiceException("同一仓库内库区编码已存在"); }
    }

    @Override
    public Boolean changeStatus(Long id, String status) {
        requireZone(id); validateStatus(status);
        WarehouseZone update = new WarehouseZone(); update.setId(id); update.setStatus(status);
        return zoneMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        LinkedHashSet<Long> distinct = new LinkedHashSet<>(ids); distinct.forEach(this::requireZone);
        if (locationMapper.existsByZoneIds(distinct)) throw new ServiceException("库区下存在未删除库位，不能删除");
        return zoneMapper.deleteByIds(distinct) > 0;
    }

    @Override
    public List<WarehouseZoneOptionVo> selectOptions(Long warehouseId) {
        accessService.requireWarehouse(warehouseId);
        return zoneMapper.selectOptions(warehouseId);
    }

    private WarehouseZone requireZone(Long id) {
        WarehouseZone zone = zoneMapper.selectById(id);
        if (zone == null) throw new ServiceException("库区不存在");
        accessService.requireWarehouse(zone.getWarehouseId());
        return zone;
    }

    private void normalize(WarehouseZoneBo bo) {
        bo.setZoneCode(StringUtils.trim(bo.getZoneCode()).toUpperCase()); bo.setZoneName(StringUtils.trim(bo.getZoneName()));
        if (bo.getStatus() == null) bo.setStatus(SystemConstants.NORMAL); if (bo.getSort() == null) bo.setSort(0);
    }

    private void validateBeforeSave(WarehouseZoneBo bo, Long excludeId, boolean insert) {
        if (insert) accessService.requireEnabledWarehouse(bo.getWarehouseId()); else accessService.requireWarehouse(bo.getWarehouseId());
        validateStatus(bo.getStatus());
        if (zoneMapper.countByCode(bo.getWarehouseId(), bo.getZoneCode(), excludeId) > 0) throw new ServiceException("同一仓库内库区编码已存在");
    }

    private void validateStatus(String status) {
        if (!dictService.getAllDictByDictType("sys_normal_disable").containsKey(status)) throw new ServiceException("状态不合法");
    }

    private void enrichWarehouses(List<WarehouseZoneVo> rows) {
        List<Long> ids = rows.stream().map(WarehouseZoneVo::getWarehouseId).distinct().toList();
        Map<Long, Warehouse> warehouses = warehouseMapper.selectByIds(ids).stream().collect(Collectors.toMap(Warehouse::getId, Function.identity()));
        rows.forEach(row -> { Warehouse warehouse = warehouses.get(row.getWarehouseId()); if (warehouse != null) row.setWarehouseName(warehouse.getWarehouseName()); });
    }
}
