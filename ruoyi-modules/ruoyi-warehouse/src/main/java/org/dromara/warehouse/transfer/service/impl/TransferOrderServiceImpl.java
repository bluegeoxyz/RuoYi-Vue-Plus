package org.dromara.warehouse.transfer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.api.*;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.service.InventoryMutationService;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.support.QuantityPrecisionUtils;
import org.dromara.warehouse.transfer.domain.*;
import org.dromara.warehouse.transfer.domain.bo.TransferOrderBo;
import org.dromara.warehouse.transfer.domain.vo.TransferOrderVo;
import org.dromara.warehouse.transfer.mapper.*;
import org.dromara.warehouse.transfer.service.ITransferOrderService;
import org.dromara.warehouse.unit.domain.Unit;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 调拨单状态机及双边库存原子事务。 */
@Service
@RequiredArgsConstructor
public class TransferOrderServiceImpl implements ITransferOrderService {
  private final TransferOrderMapper orderMapper;
  private final TransferOrderItemMapper itemMapper;
  private final WarehouseAccessService accessService;
  private final WarehouseLocationMapper locationMapper;
  private final WarehouseZoneMapper zoneMapper;
  private final MaterialMapper materialMapper;
  private final UnitMapper unitMapper;
  private final InventoryMapper inventoryMapper;
  private final InventoryMutationService mutationService;
  private final ProjectReferenceService projectReferenceService;

  @Override
  public TransferOrderVo queryById(Long id) {
    TransferOrder o = require(id);
    requireBoth(o);
    TransferOrderVo v = orderMapper.selectTransferById(id);
    if (v == null) throw new ServiceException("调拨单不存在");
    v.setItems(itemMapper.selectItems(id));
    enrich(List.of(v));
    return v;
  }

  @Override
  public PageResult<TransferOrderVo> queryPageList(TransferOrderBo bo, PageQuery pq) {
    List<Long> ids = accessService.accessibleWarehouseIds();
    if (ids.isEmpty()) return PageResult.build(List.of(), 0L);
    IPage<TransferOrderVo> p = orderMapper.selectTransferPage(pq.build(), bo, ids);
    enrich(p.getRecords());
    return PageResult.build(p.getRecords(), p.getTotal());
  }

  @Override
  public List<TransferOrderVo> queryList(TransferOrderBo bo) {
    List<Long> ids = accessService.accessibleWarehouseIds();
    if (ids.isEmpty()) return List.of();
    List<TransferOrderVo> rows = orderMapper.selectTransferList(bo, ids);
    enrich(rows);
    return rows;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long create(TransferOrderBo bo) {
    Warehouse source = accessService.requireEnabledWarehouse(bo.getSourceWarehouseId());
    Warehouse target = accessService.requireEnabledWarehouse(bo.getTargetWarehouseId());
    validateWarehouses(source, target);
    validateType(bo.getTransferType());
    validateItems(source, target, bo.getItems(), false);
    TransferOrder o = new TransferOrder();
    o.setTransferNo(IdGeneratorUtil.nextIdWithPrefix("DB"));
    apply(o, bo, source, target);
    o.setStatus("DRAFT");
    orderMapper.insert(o);
    replace(o.getId(), bo.getItems());
    return o.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(TransferOrderBo bo) {
    TransferOrder old = require(bo.getId());
    requireBoth(old);
    if (!"DRAFT".equals(old.getStatus())) throw new ServiceException("仅草稿调拨单允许修改");
    Warehouse source = accessService.requireEnabledWarehouse(bo.getSourceWarehouseId());
    Warehouse target = accessService.requireEnabledWarehouse(bo.getTargetWarehouseId());
    validateWarehouses(source, target);
    validateType(bo.getTransferType());
    validateItems(source, target, bo.getItems(), false);
    TransferOrder u = new TransferOrder();
    u.setId(old.getId());
    apply(u, bo, source, target);
    if (!orderMapper
        .lambda()
        .eq(TransferOrder::getId, old.getId())
        .eq(TransferOrder::getStatus, "DRAFT")
        .update(u)) throw new ServiceException("调拨单状态已变化");
    replace(old.getId(), bo.getItems());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void confirm(Long id) {
    TransferOrder o = locked(id);
    Warehouse source = accessService.requireEnabledWarehouse(o.getSourceWarehouseId());
    Warehouse target = accessService.requireEnabledWarehouse(o.getTargetWarehouseId());
    if (!"DRAFT".equals(o.getStatus())) throw new ServiceException("仅草稿调拨单允许确认");
    validateWarehouses(source, target);
    validateItems(source, target, items(id), true);
    if (orderMapper.updateStatusCas(
            id, "DRAFT", "CONFIRMED", LocalDateTime.now(), null, LoginHelper.getUserId())
        != 1) throw new ServiceException("调拨单状态已变化");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void complete(Long id) {
    TransferOrder o = locked(id);
    Warehouse source = accessService.requireEnabledWarehouse(o.getSourceWarehouseId());
    Warehouse target = accessService.requireEnabledWarehouse(o.getTargetWarehouseId());
    if (!"CONFIRMED".equals(o.getStatus())) throw new ServiceException("仅已确认调拨单允许完成");
    validateWarehouses(source, target);
    List<TransferOrderItem> rows = new ArrayList<>(items(id));
    validateItems(source, target, rows, true);
    lockBalances(source.getId(), target.getId(), rows);
    rows.sort(
        Comparator.comparing(TransferOrderItem::getMaterialId)
            .thenComparing(TransferOrderItem::getSourceLocationId)
            .thenComparing(TransferOrderItem::getTargetLocationId));
    LocalDateTime now = LocalDateTime.now();
    Long operator = LoginHelper.getUserId();
    for (TransferOrderItem i : rows) {
      mutationService.transferOut(
          source.getId(),
          i.getSourceLocationId(),
          i.getMaterialId(),
          i.getQuantity(),
          o.getId(),
          o.getTransferNo(),
          now,
          operator);
      mutationService.transferIn(
          target.getId(),
          i.getTargetLocationId(),
          i.getMaterialId(),
          i.getQuantity(),
          o.getId(),
          o.getTransferNo(),
          now,
          operator);
    }
    if (orderMapper.updateStatusCas(id, "CONFIRMED", "COMPLETED", null, now, operator) != 1)
      throw new ServiceException("调拨单状态已变化");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void cancel(Long id) {
    TransferOrder o = locked(id);
    requireBoth(o);
    if (!Set.of("DRAFT", "CONFIRMED").contains(o.getStatus()) || orderMapper.cancelCas(id) != 1)
      throw new ServiceException("当前状态不允许取消调拨单");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteDrafts(Collection<Long> ids) {
    for (Long id : ids) {
      TransferOrder o = require(id);
      requireBoth(o);
      if (!"DRAFT".equals(o.getStatus())) throw new ServiceException("仅草稿调拨单允许删除");
    }
    orderMapper.deleteByIds(ids);
  }

  private void lockBalances(
      Long sourceWarehouseId, Long targetWarehouseId, List<TransferOrderItem> rows) {
    rows.stream()
        .flatMap(
            i ->
                java.util.stream.Stream.of(
                    inventoryMapper.selectBalance(
                        sourceWarehouseId, i.getSourceLocationId(), i.getMaterialId()),
                    inventoryMapper.selectBalance(
                        targetWarehouseId, i.getTargetLocationId(), i.getMaterialId())))
        .filter(Objects::nonNull)
        .map(Inventory::getId)
        .distinct()
        .sorted()
        .forEach(inventoryMapper::selectByIdForUpdate);
  }

  private void validateWarehouses(Warehouse source, Warehouse target) {
    if (!source.getCompanyId().equals(target.getCompanyId()))
      throw new ServiceException("Phase 1 不允许跨公司调拨");
  }

  private void validateType(String type) {
    if (!"NORMAL".equals(type)) throw new ServiceException("当前仅支持普通调拨");
  }

  private void validateItems(
      Warehouse source, Warehouse target, List<TransferOrderItem> rows, boolean required) {
    if (required && (rows == null || rows.isEmpty())) throw new ServiceException("调拨单至少需要一条明细");
    if (rows == null) return;
    Set<String> keys = new HashSet<>();
    for (TransferOrderItem i : rows) {
      if (i.getMaterialId() == null
          || i.getSourceLocationId() == null
          || i.getTargetLocationId() == null
          || i.getUnitId() == null) throw new ServiceException("物料、源库位、目标库位和单位不能为空");
      BigDecimal q = i.getQuantity();
      if (q == null || q.signum() <= 0 || QuantityPrecisionUtils.exceedsPrecision(q, 6))
        throw new ServiceException("调拨数量必须大于0且小数位不超过6位");
      if (!keys.add(
          i.getMaterialId() + ":" + i.getSourceLocationId() + ":" + i.getTargetLocationId()))
        throw new ServiceException("同一物料及源目标库位不能重复");
      if (source.getId().equals(target.getId())
          && i.getSourceLocationId().equals(i.getTargetLocationId()))
        throw new ServiceException("同仓库调拨的源库位和目标库位不能相同");
      validateLocation(source, i.getSourceLocationId(), "源");
      validateLocation(target, i.getTargetLocationId(), "目标");
      Material m = materialMapper.selectById(i.getMaterialId());
      if (m == null
          || !source.getCompanyId().equals(m.getCompanyId())
          || !SystemConstants.NORMAL.equals(m.getStatus()))
        throw new ServiceException("物料无效、已停用或与仓库公司不一致");
      if (!i.getUnitId().equals(m.getBaseUnitId())) throw new ServiceException("调拨单位必须为物料基础单位");
      Unit u = unitMapper.selectById(i.getUnitId());
      if (u == null || !SystemConstants.NORMAL.equals(u.getStatus()))
        throw new ServiceException("物料基础单位无效或已停用");
      if (QuantityPrecisionUtils.exceedsPrecision(q, u.getPrecisionScale())) throw new ServiceException("调拨数量超过计量单位精度");
    }
  }

  private void validateLocation(Warehouse w, Long id, String side) {
    WarehouseLocation l = locationMapper.selectById(id);
    if (l == null
        || !w.getId().equals(l.getWarehouseId())
        || !SystemConstants.NORMAL.equals(l.getStatus()))
      throw new ServiceException(side + "库位无效、已停用或不属于对应仓库");
    WarehouseZone z = zoneMapper.selectById(l.getZoneId());
    if (z == null
        || !w.getId().equals(z.getWarehouseId())
        || !SystemConstants.NORMAL.equals(z.getStatus()))
      throw new ServiceException(side + "库位所属库区无效或已停用");
  }

  private void apply(TransferOrder o, TransferOrderBo bo, Warehouse source, Warehouse target) {
    o.setSourceWarehouseId(source.getId());
    o.setTargetWarehouseId(target.getId());
    o.setSourceCompanyId(source.getCompanyId());
    o.setSourceProjectId(source.getProjectId());
    o.setTargetCompanyId(target.getCompanyId());
    o.setTargetProjectId(target.getProjectId());
    o.setTransferType(bo.getTransferType());
    o.setTransferAt(bo.getTransferAt() == null ? LocalDateTime.now() : bo.getTransferAt());
    o.setRemark(bo.getRemark());
  }

  private void replace(Long id, List<TransferOrderItem> rows) {
    itemMapper.lambda().eq(TransferOrderItem::getTransferOrderId, id).delete();
    if (rows == null) return;
    for (TransferOrderItem s : rows) {
      TransferOrderItem i = new TransferOrderItem();
      i.setTransferOrderId(id);
      i.setMaterialId(s.getMaterialId());
      i.setSourceLocationId(s.getSourceLocationId());
      i.setTargetLocationId(s.getTargetLocationId());
      i.setUnitId(s.getUnitId());
      i.setQuantity(s.getQuantity());
      i.setRemark(s.getRemark());
      itemMapper.insert(i);
    }
  }

  private List<TransferOrderItem> items(Long id) {
    return itemMapper.selectByOrderId(id);
  }

  private TransferOrder require(Long id) {
    TransferOrder o = orderMapper.selectById(id);
    if (o == null) throw new ServiceException("调拨单不存在");
    return o;
  }

  private TransferOrder locked(Long id) {
    TransferOrder o = orderMapper.selectForUpdate(id);
    if (o == null) throw new ServiceException("调拨单不存在");
    return o;
  }

  private void requireBoth(TransferOrder o) {
    accessService.requireWarehouse(o.getSourceWarehouseId());
    accessService.requireWarehouse(o.getTargetWarehouseId());
  }

  private void enrich(List<TransferOrderVo> rows) {
    List<Long> ids =
        rows.stream()
            .flatMap(
                r -> java.util.stream.Stream.of(r.getSourceProjectId(), r.getTargetProjectId()))
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    if (ids.isEmpty()) return;
    Map<Long, ProjectReferenceDTO> map =
        projectReferenceService.queryByIds(ids).stream()
            .collect(Collectors.toMap(ProjectReferenceDTO::id, Function.identity()));
    rows.forEach(
        r -> {
          ProjectReferenceDTO s = map.get(r.getSourceProjectId());
          if (s != null) r.setSourceProjectName(s.projectName());
          ProjectReferenceDTO t = map.get(r.getTargetProjectId());
          if (t != null) r.setTargetProjectName(t.projectName());
        });
  }
}
