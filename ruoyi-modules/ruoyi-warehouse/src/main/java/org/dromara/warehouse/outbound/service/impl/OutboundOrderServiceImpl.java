package org.dromara.warehouse.outbound.service.impl;

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
import org.dromara.warehouse.inventory.service.InventoryMutationService;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.outbound.domain.*;
import org.dromara.warehouse.outbound.domain.bo.OutboundOrderBo;
import org.dromara.warehouse.outbound.domain.vo.OutboundOrderVo;
import org.dromara.warehouse.outbound.mapper.*;
import org.dromara.warehouse.outbound.service.IOutboundOrderService;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 出库单状态机及原子扣减事务。 */
@Service
@RequiredArgsConstructor
public class OutboundOrderServiceImpl implements IOutboundOrderService {
  private static final Set<String> MANUAL_TYPES = Set.of("MATERIAL_USE", "OTHER");
  private final OutboundOrderMapper orderMapper;
  private final OutboundOrderItemMapper itemMapper;
  private final WarehouseAccessService accessService;
  private final WarehouseLocationMapper locationMapper;
  private final WarehouseZoneMapper zoneMapper;
  private final MaterialMapper materialMapper;
  private final UnitMapper unitMapper;
  private final InventoryMutationService mutationService;
  private final ProjectReferenceService projectReferenceService;

  @Override
  public OutboundOrderVo queryById(Long id) {
    OutboundOrder o = require(id);
    accessService.requireWarehouse(o.getWarehouseId());
    OutboundOrderVo v = orderMapper.selectOutboundById(id);
    if (v == null) throw new ServiceException("出库单不存在");
    v.setItems(itemMapper.selectItems(id));
    enrich(List.of(v));
    return v;
  }

  @Override
  public PageResult<OutboundOrderVo> queryPageList(OutboundOrderBo bo, PageQuery pq) {
    List<Long> ids = accessService.accessibleWarehouseIds();
    if (ids.isEmpty()) return PageResult.build(List.of(), 0L);
    IPage<OutboundOrderVo> p = orderMapper.selectOutboundPage(pq.build(), bo, ids);
    enrich(p.getRecords());
    return PageResult.build(p.getRecords(), p.getTotal());
  }

  @Override
  public List<OutboundOrderVo> queryList(OutboundOrderBo bo) {
    List<Long> ids = accessService.accessibleWarehouseIds();
    if (ids.isEmpty()) return List.of();
    List<OutboundOrderVo> rows = orderMapper.selectOutboundList(bo, ids);
    enrich(rows);
    return rows;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long create(OutboundOrderBo bo) {
    Warehouse w = accessService.requireEnabledWarehouse(bo.getWarehouseId());
    validateType(bo.getOutboundType());
    validateItems(w, bo.getItems(), false);
    OutboundOrder o = new OutboundOrder();
    o.setOutboundNo(IdGeneratorUtil.nextIdWithPrefix("CK"));
    apply(o, bo, w);
    o.setStatus("DRAFT");
    orderMapper.insert(o);
    replace(o.getId(), bo.getItems());
    return o.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(OutboundOrderBo bo) {
    OutboundOrder old = require(bo.getId());
    accessService.requireWarehouse(old.getWarehouseId());
    if (!"DRAFT".equals(old.getStatus())) throw new ServiceException("仅草稿出库单允许修改");
    Warehouse w = accessService.requireEnabledWarehouse(bo.getWarehouseId());
    validateType(bo.getOutboundType());
    validateItems(w, bo.getItems(), false);
    OutboundOrder u = new OutboundOrder();
    u.setId(old.getId());
    apply(u, bo, w);
    if (!orderMapper
        .lambda()
        .eq(OutboundOrder::getId, old.getId())
        .eq(OutboundOrder::getStatus, "DRAFT")
        .update(u)) throw new ServiceException("出库单状态已变化");
    replace(old.getId(), bo.getItems());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void confirm(Long id) {
    OutboundOrder o = orderMapper.selectForUpdate(id);
    if (o == null) throw new ServiceException("出库单不存在");
    Warehouse w = accessService.requireEnabledWarehouse(o.getWarehouseId());
    if (!"DRAFT".equals(o.getStatus())) throw new ServiceException("仅草稿出库单允许确认");
    validateItems(w, items(id), true);
    LocalDateTime now = LocalDateTime.now();
    if (orderMapper.updateStatusCas(id, "DRAFT", "CONFIRMED", now, null, LoginHelper.getUserId())
        != 1) throw new ServiceException("出库单状态已变化");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void complete(Long id) {
    OutboundOrder o = orderMapper.selectForUpdate(id);
    if (o == null) throw new ServiceException("出库单不存在");
    Warehouse w = accessService.requireEnabledWarehouse(o.getWarehouseId());
    if (!"CONFIRMED".equals(o.getStatus())) throw new ServiceException("仅已确认出库单允许完成");
    List<OutboundOrderItem> items = items(id);
    validateItems(w, items, true);
    LocalDateTime now = LocalDateTime.now();
    Long operator = LoginHelper.getUserId();
    for (OutboundOrderItem i : items)
      mutationService.outbound(
          o.getWarehouseId(),
          i.getLocationId(),
          i.getMaterialId(),
          i.getQuantity(),
          o.getId(),
          o.getOutboundNo(),
          now,
          operator);
    if (orderMapper.updateStatusCas(id, "CONFIRMED", "COMPLETED", null, now, operator) != 1)
      throw new ServiceException("出库单状态已变化");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void cancel(Long id) {
    OutboundOrder o = orderMapper.selectForUpdate(id);
    if (o == null) throw new ServiceException("出库单不存在");
    accessService.requireWarehouse(o.getWarehouseId());
    if (!Set.of("DRAFT", "CONFIRMED").contains(o.getStatus()) || orderMapper.cancelCas(id) != 1)
      throw new ServiceException("当前状态不允许取消出库单");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteDrafts(Collection<Long> ids) {
    for (Long id : ids) {
      OutboundOrder o = require(id);
      accessService.requireWarehouse(o.getWarehouseId());
      if (!"DRAFT".equals(o.getStatus())) throw new ServiceException("仅草稿出库单允许删除");
    }
    orderMapper.deleteByIds(ids);
  }

  private List<OutboundOrderItem> items(Long id) {
    return itemMapper.lambda().eq(OutboundOrderItem::getOutboundOrderId, id).list();
  }

  private void apply(OutboundOrder o, OutboundOrderBo bo, Warehouse w) {
    o.setWarehouseId(w.getId());
    o.setCompanyId(w.getCompanyId());
    o.setProjectId(w.getProjectId());
    o.setOutboundType(bo.getOutboundType());
    o.setOutboundAt(bo.getOutboundAt() == null ? LocalDateTime.now() : bo.getOutboundAt());
    o.setRemark(bo.getRemark());
  }

  private void replace(Long id, List<OutboundOrderItem> items) {
    itemMapper.lambda().eq(OutboundOrderItem::getOutboundOrderId, id).delete();
    if (items == null) return;
    for (OutboundOrderItem s : items) {
      OutboundOrderItem i = new OutboundOrderItem();
      i.setOutboundOrderId(id);
      i.setMaterialId(s.getMaterialId());
      i.setLocationId(s.getLocationId());
      i.setUnitId(s.getUnitId());
      i.setQuantity(s.getQuantity());
      i.setRemark(s.getRemark());
      itemMapper.insert(i);
    }
  }

  private void validateItems(Warehouse w, List<OutboundOrderItem> items, boolean required) {
    if (required && (items == null || items.isEmpty())) throw new ServiceException("出库单至少需要一条明细");
    if (items == null) return;
    Set<String> keys = new HashSet<>();
    for (OutboundOrderItem i : items) {
      if (i.getMaterialId() == null || i.getLocationId() == null || i.getUnitId() == null)
        throw new ServiceException("物料、库位和单位不能为空");
      BigDecimal q = i.getQuantity();
      if (q == null || q.signum() <= 0 || QuantityPrecisionUtils.exceedsPrecision(q, 6))
        throw new ServiceException("出库数量必须大于0且小数位不超过6位");
      if (!keys.add(i.getMaterialId() + ":" + i.getLocationId()))
        throw new ServiceException("同一物料和库位不能重复");
      WarehouseLocation l = locationMapper.selectById(i.getLocationId());
      if (l == null
          || !w.getId().equals(l.getWarehouseId())
          || !SystemConstants.NORMAL.equals(l.getStatus()))
        throw new ServiceException("目标库位无效、已停用或不属于出库仓库");
      WarehouseZone z = zoneMapper.selectById(l.getZoneId());
      if (z == null
          || !w.getId().equals(z.getWarehouseId())
          || !SystemConstants.NORMAL.equals(z.getStatus()))
        throw new ServiceException("库位所属库区无效或已停用");
      Material m = materialMapper.selectById(i.getMaterialId());
      if (m == null
          || !w.getCompanyId().equals(m.getCompanyId())
          || !SystemConstants.NORMAL.equals(m.getStatus()))
        throw new ServiceException("物料无效、已停用或与仓库公司不一致");
      if (!i.getUnitId().equals(m.getBaseUnitId())) throw new ServiceException("出库单位必须为物料基础单位");
      Unit u = unitMapper.selectById(i.getUnitId());
      if (u == null || !SystemConstants.NORMAL.equals(u.getStatus()))
        throw new ServiceException("物料基础单位无效或已停用");
      if (QuantityPrecisionUtils.exceedsPrecision(q, u.getPrecisionScale())) throw new ServiceException("出库数量超过计量单位精度");
    }
  }

  private void validateType(String t) {
    if (!MANUAL_TYPES.contains(t)) throw new ServiceException("当前仅支持领料出库和其他出库，调拨出库必须由调拨单产生");
  }

  private OutboundOrder require(Long id) {
    OutboundOrder o = orderMapper.selectById(id);
    if (o == null) throw new ServiceException("出库单不存在");
    return o;
  }

  private void enrich(List<OutboundOrderVo> rows) {
    List<Long> ids =
        rows.stream()
            .map(OutboundOrderVo::getProjectId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    if (ids.isEmpty()) return;
    Map<Long, ProjectReferenceDTO> map =
        projectReferenceService.queryByIds(ids).stream()
            .collect(Collectors.toMap(ProjectReferenceDTO::id, Function.identity()));
    rows.forEach(
        r -> {
          ProjectReferenceDTO p = map.get(r.getProjectId());
          if (p != null) r.setProjectName(p.projectName());
        });
  }
}
