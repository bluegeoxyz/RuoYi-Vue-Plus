package org.dromara.warehouse.stocktake.service.impl;

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
import org.dromara.warehouse.stocktake.domain.*;
import org.dromara.warehouse.stocktake.domain.bo.StocktakeOrderBo;
import org.dromara.warehouse.stocktake.domain.vo.StocktakeOrderVo;
import org.dromara.warehouse.stocktake.mapper.*;
import org.dromara.warehouse.stocktake.service.IStocktakeOrderService;
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

@Service
@RequiredArgsConstructor
public class StocktakeOrderServiceImpl implements IStocktakeOrderService {
  private final StocktakeOrderMapper orderMapper;
  private final StocktakeOrderItemMapper itemMapper;
  private final WarehouseAccessService accessService;
  private final InventoryMapper inventoryMapper;
  private final InventoryMutationService mutationService;
  private final WarehouseLocationMapper locationMapper;
  private final WarehouseZoneMapper zoneMapper;
  private final MaterialMapper materialMapper;
  private final UnitMapper unitMapper;
  private final ProjectReferenceService projectReferenceService;

  @Override
  public StocktakeOrderVo queryById(Long id) {
    StocktakeOrder o = require(id);
    accessService.requireWarehouse(o.getWarehouseId());
    StocktakeOrderVo v = orderMapper.selectStocktakeById(id);
    if (v == null) throw new ServiceException("盘点单不存在");
    v.setItems(itemMapper.selectItems(id));
    enrich(List.of(v));
    return v;
  }

  @Override
  public PageResult<StocktakeOrderVo> queryPageList(StocktakeOrderBo bo, PageQuery pq) {
    List<Long> ids = accessService.accessibleWarehouseIds();
    if (ids.isEmpty()) return PageResult.build(List.of(), 0L);
    IPage<StocktakeOrderVo> p = orderMapper.selectStocktakePage(pq.build(), bo, ids);
    enrich(p.getRecords());
    return PageResult.build(p.getRecords(), p.getTotal());
  }

  @Override
  public List<StocktakeOrderVo> queryList(StocktakeOrderBo bo) {
    List<Long> ids = accessService.accessibleWarehouseIds();
    if (ids.isEmpty()) return List.of();
    List<StocktakeOrderVo> rows = orderMapper.selectStocktakeList(bo, ids);
    enrich(rows);
    return rows;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long create(StocktakeOrderBo bo) {
    Warehouse w = accessService.requireEnabledWarehouse(bo.getWarehouseId());
    validateHeader(bo, w);
    StocktakeOrder o = new StocktakeOrder();
    o.setStocktakeNo(IdGeneratorUtil.nextIdWithPrefix("PD"));
    apply(o, bo, w);
    o.setStatus("DRAFT");
    orderMapper.insert(o);
    replace(o, bo.getItems(), Map.of(), true);
    return o.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(StocktakeOrderBo bo) {
    StocktakeOrder old = require(bo.getId());
    accessService.requireWarehouse(old.getWarehouseId());
    if (!"DRAFT".equals(old.getStatus())) throw new ServiceException("仅草稿盘点单允许修改");
    Warehouse w = accessService.requireEnabledWarehouse(bo.getWarehouseId());
    validateHeader(bo, w);
    Map<String, StocktakeOrderItem> existing =
        Objects.equals(old.getWarehouseId(), w.getId())
            ? itemMapper.selectByOrderId(old.getId()).stream()
                .collect(Collectors.toMap(this::key, Function.identity()))
            : Map.of();
    StocktakeOrder u = new StocktakeOrder();
    u.setId(old.getId());
    apply(u, bo, w);
    if (!orderMapper
        .lambda()
        .eq(StocktakeOrder::getId, old.getId())
        .eq(StocktakeOrder::getStatus, "DRAFT")
        .update(u)) throw new ServiceException("盘点单状态已变化");
    replace(u, bo.getItems(), existing, false);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void confirm(Long id) {
    StocktakeOrder o = locked(id);
    accessService.requireWarehouse(o.getWarehouseId());
    if (!"DRAFT".equals(o.getStatus())) throw new ServiceException("仅草稿盘点单允许确认");
    List<StocktakeOrderItem> rows = itemMapper.selectByOrderId(id);
    validateItems(o, rows, true, true);
    if (orderMapper.updateStatusCas(
            id, "DRAFT", "CONFIRMED", LocalDateTime.now(), null, LoginHelper.getUserId())
        != 1) throw new ServiceException("盘点单状态已变化");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void complete(Long id) {
    StocktakeOrder o = locked(id);
    accessService.requireWarehouse(o.getWarehouseId());
    if (!"CONFIRMED".equals(o.getStatus())) throw new ServiceException("仅已确认盘点单允许完成");
    List<StocktakeOrderItem> rows = new ArrayList<>(itemMapper.selectByOrderId(id));
    validateItems(o, rows, true, true);
    rows.stream()
        .map(
            i ->
                inventoryMapper.selectBalance(
                    o.getWarehouseId(), i.getLocationId(), i.getMaterialId()))
        .filter(Objects::nonNull)
        .map(Inventory::getId)
        .distinct()
        .sorted()
        .forEach(inventoryMapper::selectByIdForUpdate);
    rows.sort(
        Comparator.comparing(StocktakeOrderItem::getMaterialId)
            .thenComparing(StocktakeOrderItem::getLocationId));
    LocalDateTime now = LocalDateTime.now();
    Long operator = LoginHelper.getUserId();
    for (StocktakeOrderItem i : rows)
      mutationService.adjustForStocktake(
          o.getWarehouseId(),
          i.getLocationId(),
          i.getMaterialId(),
          i.getBookQuantity(),
          i.getActualQuantity(),
          o.getId(),
          o.getStocktakeNo(),
          now,
          operator);
    if (orderMapper.updateStatusCas(id, "CONFIRMED", "COMPLETED", null, now, operator) != 1)
      throw new ServiceException("盘点单状态已变化");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void cancel(Long id) {
    StocktakeOrder o = locked(id);
    accessService.requireWarehouse(o.getWarehouseId());
    if (!Set.of("DRAFT", "CONFIRMED").contains(o.getStatus()) || orderMapper.cancelCas(id) != 1)
      throw new ServiceException("当前状态不允许取消盘点单");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteDrafts(Collection<Long> ids) {
    for (Long id : ids) {
      StocktakeOrder o = require(id);
      accessService.requireWarehouse(o.getWarehouseId());
      if (!"DRAFT".equals(o.getStatus())) throw new ServiceException("仅草稿盘点单允许删除");
    }
    orderMapper.deleteByIds(ids);
  }

  private void replace(
      StocktakeOrder o,
      List<StocktakeOrderItem> submitted,
      Map<String, StocktakeOrderItem> existing,
      boolean generate) {
    LinkedHashMap<String, StocktakeOrderItem> all = new LinkedHashMap<>();
    if (generate) {
      for (Inventory v : scopeInventories(o)) {
        StocktakeOrderItem i = new StocktakeOrderItem();
        i.setMaterialId(v.getMaterialId());
        i.setLocationId(v.getLocationId());
        i.setBookQuantity(v.getQuantity());
        i.setActualQuantity(null);
        all.put(key(i), i);
      }
    }
    Set<String> submittedKeys = new HashSet<>();
    if (submitted != null)
      for (StocktakeOrderItem s : submitted) {
        if (s.getMaterialId() == null || s.getLocationId() == null)
          throw new ServiceException("物料和库位不能为空");
        StocktakeOrderItem i = new StocktakeOrderItem();
        i.setMaterialId(s.getMaterialId());
        i.setLocationId(s.getLocationId());
        if (!submittedKeys.add(key(i))) throw new ServiceException("同一物料和库位不能重复");
        i.setActualQuantity(s.getActualQuantity());
        i.setRemark(s.getRemark());
        StocktakeOrderItem old = existing.get(key(i));
        Inventory v =
            inventoryMapper.selectBalance(o.getWarehouseId(), i.getLocationId(), i.getMaterialId());
        i.setBookQuantity(
            old == null ? (v == null ? BigDecimal.ZERO : v.getQuantity()) : old.getBookQuantity());
        all.put(key(i), i);
      }
    itemMapper.lambda().eq(StocktakeOrderItem::getStocktakeOrderId, o.getId()).delete();
    for (StocktakeOrderItem i : all.values()) {
      snapshotRelations(o, i);
      i.setStocktakeOrderId(o.getId());
      i.setDifferenceQuantity(
          i.getActualQuantity() == null
              ? null
              : i.getActualQuantity().subtract(i.getBookQuantity()));
      itemMapper.insert(i);
    }
  }

  private List<Inventory> scopeInventories(StocktakeOrder o) {
    Set<Long> locations;
    if ("LOCATION".equals(o.getScopeType())) locations = Set.of(o.getLocationId());
    else if ("ZONE".equals(o.getScopeType())) {
      locations =
          locationMapper.lambda().eq(WarehouseLocation::getZoneId, o.getZoneId()).list().stream()
              .map(WarehouseLocation::getId)
              .collect(Collectors.toSet());
      if (locations.isEmpty()) return List.of();
    } else locations = Set.of();
    return inventoryMapper
        .lambda()
        .eq(Inventory::getWarehouseId, o.getWarehouseId())
        .in(!locations.isEmpty(), Inventory::getLocationId, locations)
        .list();
  }

  private void validateHeader(StocktakeOrderBo bo, Warehouse w) {
    if (!Set.of("FULL", "PARTIAL").contains(bo.getStocktakeType()))
      throw new ServiceException("盘点类型无效");
    if (!Set.of("WAREHOUSE", "ZONE", "LOCATION").contains(bo.getScopeType()))
      throw new ServiceException("盘点范围无效");
    if ("ZONE".equals(bo.getScopeType())) {
      WarehouseZone z = zoneMapper.selectById(bo.getZoneId());
      if (z == null || !w.getId().equals(z.getWarehouseId()))
        throw new ServiceException("盘点库区不属于所选仓库");
    }
    if ("LOCATION".equals(bo.getScopeType())) {
      WarehouseLocation l = locationMapper.selectById(bo.getLocationId());
      if (l == null || !w.getId().equals(l.getWarehouseId()))
        throw new ServiceException("盘点库位不属于所选仓库");
    }
  }

  private void validateItems(
      StocktakeOrder o, List<StocktakeOrderItem> rows, boolean required, boolean actualRequired) {
    if (required && rows.isEmpty()) throw new ServiceException("盘点单至少需要一条明细");
    Set<String> keys = new HashSet<>();
    for (StocktakeOrderItem i : rows) {
      if (!keys.add(key(i))) throw new ServiceException("同一物料和库位不能重复");
      snapshotRelations(o, i);
      if (actualRequired && i.getActualQuantity() == null)
        throw new ServiceException("所有盘点明细必须录入实际数量");
      if (i.getActualQuantity() != null
          && (i.getActualQuantity().signum() < 0 || QuantityPrecisionUtils.exceedsPrecision(i.getActualQuantity(), 6)))
        throw new ServiceException("实际数量必须为非负数且小数位不超过6位");
      Unit u = unitMapper.selectById(i.getUnitId());
      if (QuantityPrecisionUtils.exceedsPrecision(i.getActualQuantity(), u.getPrecisionScale()))
        throw new ServiceException("实际数量超过计量单位精度");
    }
  }

  private void snapshotRelations(StocktakeOrder o, StocktakeOrderItem i) {
    WarehouseLocation l = locationMapper.selectById(i.getLocationId());
    if (l == null || !o.getWarehouseId().equals(l.getWarehouseId()))
      throw new ServiceException("盘点库位不存在或不属于盘点仓库");
    if ("ZONE".equals(o.getScopeType()) && !Objects.equals(o.getZoneId(), l.getZoneId()))
      throw new ServiceException("盘点明细超出指定库区范围");
    if ("LOCATION".equals(o.getScopeType()) && !Objects.equals(o.getLocationId(), l.getId()))
      throw new ServiceException("盘点明细超出指定库位范围");
    Material m = materialMapper.selectById(i.getMaterialId());
    if (m == null || !o.getCompanyId().equals(m.getCompanyId()))
      throw new ServiceException("盘点物料不存在或公司不一致");
    i.setUnitId(m.getBaseUnitId());
    Unit u = unitMapper.selectById(i.getUnitId());
    if (u == null) throw new ServiceException("物料基础单位不存在");
  }

  private void apply(StocktakeOrder o, StocktakeOrderBo bo, Warehouse w) {
    o.setWarehouseId(w.getId());
    o.setCompanyId(w.getCompanyId());
    o.setProjectId(w.getProjectId());
    o.setStocktakeType(bo.getStocktakeType());
    o.setScopeType(bo.getScopeType());
    o.setZoneId(bo.getZoneId());
    o.setLocationId(bo.getLocationId());
    o.setStocktakeAt(bo.getStocktakeAt() == null ? LocalDateTime.now() : bo.getStocktakeAt());
    o.setRemark(bo.getRemark());
  }

  private String key(StocktakeOrderItem i) {
    return i.getMaterialId() + ":" + i.getLocationId();
  }

  private StocktakeOrder require(Long id) {
    StocktakeOrder o = orderMapper.selectById(id);
    if (o == null) throw new ServiceException("盘点单不存在");
    return o;
  }

  private StocktakeOrder locked(Long id) {
    StocktakeOrder o = orderMapper.selectForUpdate(id);
    if (o == null) throw new ServiceException("盘点单不存在");
    return o;
  }

  private void enrich(List<StocktakeOrderVo> rows) {
    List<Long> ids =
        rows.stream()
            .map(StocktakeOrderVo::getProjectId)
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
