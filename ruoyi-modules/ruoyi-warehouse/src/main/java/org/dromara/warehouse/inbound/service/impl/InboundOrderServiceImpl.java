package org.dromara.warehouse.inbound.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.api.ProjectReferenceDTO;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.warehouse.inbound.domain.InboundOrder;
import org.dromara.warehouse.inbound.domain.InboundOrderItem;
import org.dromara.warehouse.inbound.domain.bo.InboundOrderBo;
import org.dromara.warehouse.inbound.domain.vo.InboundOrderVo;
import org.dromara.warehouse.inbound.mapper.InboundOrderItemMapper;
import org.dromara.warehouse.inbound.mapper.InboundOrderMapper;
import org.dromara.warehouse.inbound.service.IInboundOrderService;
import org.dromara.warehouse.inventory.service.InventoryMutationService;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.support.QuantityPrecisionUtils;
import org.dromara.warehouse.unit.domain.Unit;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 入库单状态机和库存完成事务。 */
@Service @RequiredArgsConstructor
public class InboundOrderServiceImpl implements IInboundOrderService {
    private static final Set<String> MANUAL_TYPES = Set.of("PURCHASE", "OTHER");
    private final InboundOrderMapper orderMapper;
    private final InboundOrderItemMapper itemMapper;
    private final WarehouseAccessService accessService;
    private final WarehouseLocationMapper locationMapper;
    private final WarehouseZoneMapper zoneMapper;
    private final MaterialMapper materialMapper;
    private final UnitMapper unitMapper;
    private final InventoryMutationService mutationService;
    private final ProjectReferenceService projectReferenceService;

    @Override public InboundOrderVo queryById(Long id) {
        InboundOrder order = requireOrder(id);
        accessService.requireWarehouse(order.getWarehouseId());
        InboundOrderVo vo = orderMapper.selectInboundById(id);
        if (vo == null) throw new ServiceException("入库单不存在");
        vo.setItems(itemMapper.selectItems(id));
        enrichProjects(List.of(vo));
        return vo;
    }

    @Override public PageResult<InboundOrderVo> queryPageList(InboundOrderBo bo, PageQuery pageQuery) {
        List<Long> ids = accessService.accessibleWarehouseIds();
        if (ids.isEmpty()) return PageResult.build(List.of(), 0L);
        IPage<InboundOrderVo> page = orderMapper.selectInboundPage(pageQuery.build(), bo, ids);
        enrichProjects(page.getRecords());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override public List<InboundOrderVo> queryList(InboundOrderBo bo) {
        List<Long> ids = accessService.accessibleWarehouseIds();
        if (ids.isEmpty()) return List.of();
        List<InboundOrderVo> rows = orderMapper.selectInboundList(bo, ids);
        enrichProjects(rows);
        return rows;
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public Long create(InboundOrderBo bo) {
        Warehouse warehouse = accessService.requireEnabledWarehouse(bo.getWarehouseId());
        validateType(bo.getInboundType());
        validateItems(warehouse, bo.getItems(), false);
        InboundOrder order = new InboundOrder();
        order.setInboundNo(IdGeneratorUtil.nextIdWithPrefix("RK"));
        applyDraft(order, bo, warehouse);
        order.setStatus("DRAFT");
        orderMapper.insert(order);
        replaceItems(order.getId(), bo.getItems());
        return order.getId();
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void update(InboundOrderBo bo) {
        InboundOrder current = requireOrder(bo.getId());
        accessService.requireWarehouse(current.getWarehouseId());
        if (!"DRAFT".equals(current.getStatus())) throw new ServiceException("仅草稿入库单允许修改");
        Warehouse warehouse = accessService.requireEnabledWarehouse(bo.getWarehouseId());
        validateType(bo.getInboundType());
        validateItems(warehouse, bo.getItems(), false);
        InboundOrder update = new InboundOrder();
        update.setId(current.getId());
        applyDraft(update, bo, warehouse);
        if (!orderMapper.lambda().eq(InboundOrder::getId, current.getId()).eq(InboundOrder::getStatus, "DRAFT").update(update)) {
            throw new ServiceException("入库单状态已变化，无法修改");
        }
        replaceItems(current.getId(), bo.getItems());
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        InboundOrder order = orderMapper.selectForUpdate(id);
        if (order == null) throw new ServiceException("入库单不存在");
        Warehouse warehouse = accessService.requireEnabledWarehouse(order.getWarehouseId());
        if (!"DRAFT".equals(order.getStatus())) throw new ServiceException("仅草稿入库单允许确认");
        List<InboundOrderItem> items = itemMapper.lambda().eq(InboundOrderItem::getInboundOrderId, id).list();
        validateItems(warehouse, items, true);
        if (orderMapper.updateStatusCas(id, "DRAFT", "CONFIRMED", null, LoginHelper.getUserId()) != 1) {
            throw new ServiceException("入库单状态已变化，确认失败");
        }
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        InboundOrder order = orderMapper.selectForUpdate(id);
        if (order == null) throw new ServiceException("入库单不存在");
        Warehouse warehouse = accessService.requireEnabledWarehouse(order.getWarehouseId());
        if (!"CONFIRMED".equals(order.getStatus())) throw new ServiceException("仅已确认入库单允许完成");
        List<InboundOrderItem> items = itemMapper.lambda().eq(InboundOrderItem::getInboundOrderId, id).list();
        validateItems(warehouse, items, true);
        LocalDateTime completedAt = LocalDateTime.now();
        Long operatorId = LoginHelper.getUserId();
        for (InboundOrderItem item : items) {
            mutationService.inbound(order.getWarehouseId(), item.getLocationId(), item.getMaterialId(), item.getQuantity(),
                order.getId(), order.getInboundNo(), completedAt, operatorId);
        }
        if (orderMapper.updateStatusCas(id, "CONFIRMED", "COMPLETED", completedAt, operatorId) != 1) {
            throw new ServiceException("入库单状态已变化，完成失败");
        }
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        InboundOrder order = orderMapper.selectForUpdate(id);
        if (order == null) throw new ServiceException("入库单不存在");
        accessService.requireWarehouse(order.getWarehouseId());
        if (!Set.of("DRAFT", "CONFIRMED").contains(order.getStatus()) || orderMapper.cancelCas(id) != 1) {
            throw new ServiceException("当前状态不允许取消入库单");
        }
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void deleteDrafts(Collection<Long> ids) {
        for (Long id : ids) {
            InboundOrder order = requireOrder(id);
            accessService.requireWarehouse(order.getWarehouseId());
            if (!"DRAFT".equals(order.getStatus())) throw new ServiceException("仅草稿入库单允许删除");
        }
        orderMapper.deleteByIds(ids);
    }

    private void applyDraft(InboundOrder order, InboundOrderBo bo, Warehouse warehouse) {
        order.setWarehouseId(warehouse.getId());
        order.setCompanyId(warehouse.getCompanyId());
        order.setProjectId(warehouse.getProjectId());
        order.setInboundType(bo.getInboundType());
        order.setInboundAt(bo.getInboundAt() == null ? LocalDateTime.now() : bo.getInboundAt());
        order.setRemark(bo.getRemark());
    }

    private void replaceItems(Long orderId, List<InboundOrderItem> items) {
        itemMapper.lambda().eq(InboundOrderItem::getInboundOrderId, orderId).delete();
        if (items == null) return;
        for (InboundOrderItem source : items) {
            InboundOrderItem item = new InboundOrderItem();
            item.setInboundOrderId(orderId); item.setMaterialId(source.getMaterialId()); item.setLocationId(source.getLocationId());
            item.setQuantity(source.getQuantity()); item.setUnitId(source.getUnitId()); item.setRemark(source.getRemark());
            itemMapper.insert(item);
        }
    }

    private void validateItems(Warehouse warehouse, List<InboundOrderItem> items, boolean required) {
        if (required && (items == null || items.isEmpty())) throw new ServiceException("入库单至少需要一条明细");
        if (items == null) return;
        Set<String> keys = new HashSet<>();
        for (InboundOrderItem item : items) {
            if (item.getMaterialId() == null || item.getLocationId() == null || item.getUnitId() == null) throw new ServiceException("物料、库位和单位不能为空");
            BigDecimal quantity = item.getQuantity();
            if (quantity == null || quantity.signum() <= 0 || QuantityPrecisionUtils.exceedsPrecision(quantity, 6)) throw new ServiceException("入库数量必须大于0且小数位不超过6位");
            if (!keys.add(item.getMaterialId() + ":" + item.getLocationId())) throw new ServiceException("同一物料和库位不能重复");
            WarehouseLocation location = locationMapper.selectById(item.getLocationId());
            if (location == null || !warehouse.getId().equals(location.getWarehouseId())) throw new ServiceException("库位与入库仓库不一致");
            if (!SystemConstants.NORMAL.equals(location.getStatus())) throw new ServiceException("停用库位不能入库");
            WarehouseZone zone = zoneMapper.selectById(location.getZoneId());
            if (zone == null || !warehouse.getId().equals(zone.getWarehouseId()) || !SystemConstants.NORMAL.equals(zone.getStatus())) throw new ServiceException("库位所属库区无效或已停用");
            Material material = materialMapper.selectById(item.getMaterialId());
            if (material == null || !warehouse.getCompanyId().equals(material.getCompanyId())) throw new ServiceException("物料与仓库所属公司不一致");
            if (!SystemConstants.NORMAL.equals(material.getStatus())) throw new ServiceException("停用物料不能入库");
            if (!item.getUnitId().equals(material.getBaseUnitId())) throw new ServiceException("入库单位必须为物料基础单位");
            Unit unit = unitMapper.selectById(item.getUnitId());
            if (unit == null || !SystemConstants.NORMAL.equals(unit.getStatus())) throw new ServiceException("物料基础单位无效或已停用");
            if (QuantityPrecisionUtils.exceedsPrecision(quantity, unit.getPrecisionScale())) throw new ServiceException("入库数量超过计量单位精度");
        }
    }

    private void validateType(String type) {
        if (!MANUAL_TYPES.contains(type)) throw new ServiceException("当前仅支持采购入库和其他入库，调拨入库必须由调拨单产生");
    }

    private InboundOrder requireOrder(Long id) {
        InboundOrder order = orderMapper.selectById(id);
        if (order == null) throw new ServiceException("入库单不存在");
        return order;
    }

    private void enrichProjects(List<InboundOrderVo> rows) {
        List<Long> ids = rows.stream().map(InboundOrderVo::getProjectId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return;
        Map<Long, ProjectReferenceDTO> projects = projectReferenceService.queryByIds(ids).stream().collect(Collectors.toMap(ProjectReferenceDTO::id, Function.identity()));
        rows.forEach(row -> { ProjectReferenceDTO p = projects.get(row.getProjectId()); if (p != null) row.setProjectName(p.projectName()); });
    }
}
