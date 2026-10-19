package org.dromara.property.charge.binding.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.charge.binding.domain.*;
import org.dromara.property.charge.binding.domain.bo.*;
import org.dromara.property.charge.binding.domain.vo.*;
import org.dromara.property.charge.binding.mapper.ChargeBindingMapper;
import org.dromara.property.charge.binding.service.IChargeBindingService;
import org.dromara.property.charge.item.domain.ChargeItem;
import org.dromara.property.charge.item.mapper.ChargeItemMapper;
import org.dromara.property.charge.rule.domain.ChargeRule;
import org.dromara.property.charge.rule.mapper.ChargeRuleMapper;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChargeBindingServiceImpl implements IChargeBindingService {
  private static final Set<String> STATUSES = Set.of("0", "1");
  private final ChargeBindingMapper mapper;
  private final RoomMapper roomMapper;
  private final ChargeItemMapper itemMapper;
  private final ChargeRuleMapper ruleMapper;
  private final ProjectMapper projectMapper;
  private final BuildingMapper buildingMapper;
  private final BuildingUnitMapper unitMapper;
  private final PropertyProjectScopeService scopeService;
  private final PlatformTransactionManager transactionManager;

  public List<ChargeBindingVo> queryRoomBindings(Long roomId) {
    Room room = room(roomId);
    access(room.getProjectId(), ProjectBusinessAction.READ);
    List<ChargeBindingVo> rows =
        mapper.selectVoList(
            QueryBuilder.lambda(ChargeBinding.class)
                .eq(ChargeBinding::getObjectType, "ROOM")
                .eq(ChargeBinding::getObjectId, roomId)
                .orderByAsc(ChargeBinding::getChargeItemId)
                .orderByDesc(ChargeBinding::getEffectiveStartDate)
                .build());
    enrich(rows, room);
    return rows;
  }

  public ChargeBindingVo queryById(Long id) {
    ChargeBinding b = require(id);
    Room room = room(b.getObjectId());
    assertBindingRoom(b, room);
    access(room.getProjectId(), ProjectBusinessAction.READ);
    ChargeBindingVo vo = mapper.selectVoById(id);
    enrich(List.of(vo), room);
    return vo;
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean insert(ChargeBindingBo bo) {
    return insertCore(bo);
  }

  public ChargeBindingBatchResultVo batchCreate(ChargeBindingBatchBo bo) {
    access(bo.getProjectId(), ProjectBusinessAction.WRITE);
    if (new HashSet<>(bo.getRoomIds()).size() != bo.getRoomIds().size())
      throw new ServiceException("房屋列表存在重复数据");
    List<Room> rooms = roomMapper.selectBatchIds(bo.getRoomIds());
    if (rooms.size() != bo.getRoomIds().size()) throw new ServiceException("部分房屋不存在");
    if (rooms.stream().anyMatch(r -> !bo.getProjectId().equals(r.getProjectId())))
      throw new ServiceException("存在不属于当前项目的房屋");
    ChargeItem item = validateItem(bo.getChargeItemId(), bo.getProjectId());
    ChargeRule rule = validateRule(bo.getChargeRuleId(), item, bo.getProjectId());
    validateDates(bo.getEffectiveStartDate(), bo.getEffectiveEndDate(), rule);
    Map<Long, Room> roomMap = new HashMap<>();
    rooms.forEach(r -> roomMap.put(r.getId(), r));
    TransactionTemplate tx = new TransactionTemplate(transactionManager);
    tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    List<ChargeBindingBatchFailureVo> failures = new ArrayList<>();
    int success = 0;
    for (Long roomId : bo.getRoomIds()) {
      ChargeBindingBo one = batchRow(bo, roomId);
      try {
        Boolean ok = tx.execute(s -> insertCore(one));
        if (Boolean.TRUE.equals(ok)) success++;
        else failures.add(failure(roomMap.get(roomId), "DATA_ERROR", "收费设置保存失败"));
      } catch (Exception e) {
        String message = e.getMessage() == null ? "收费设置数据异常" : e.getMessage();
        failures.add(
            failure(
                roomMap.get(roomId), message.contains("重叠") ? "OVERLAP" : "DATA_ERROR", message));
      }
    }
    return new ChargeBindingBatchResultVo(
        bo.getRoomIds().size(), success, failures.size(), failures);
  }

  private boolean insertCore(ChargeBindingBo bo) {
    if (!"ROOM".equals(bo.getObjectType())) throw new ServiceException("当前仅支持房屋收费设置");
    Room room = lockedRoom(bo.getObjectId());
    access(room.getProjectId(), ProjectBusinessAction.WRITE);
    if (bo.getProjectId() != null && !bo.getProjectId().equals(room.getProjectId()))
      throw new ServiceException("所属项目与房屋不一致");
    ChargeItem item = validateItem(bo.getChargeItemId(), room.getProjectId());
    ChargeRule rule = validateRule(bo.getChargeRuleId(), item, room.getProjectId());
    validateDates(bo.getEffectiveStartDate(), bo.getEffectiveEndDate(), rule);
    lockAndCheckOverlap(
        room, item.getId(), bo.getEffectiveStartDate(), bo.getEffectiveEndDate(), null);
    ChargeBinding row = BeanUtil.copyProperties(bo, ChargeBinding.class);
    row.setProjectId(room.getProjectId());
    row.setObjectType("ROOM");
    row.setObjectId(room.getId());
    if (row.getStatus() == null) row.setStatus("0");
    validateStatus(row.getStatus());
    if (row.getSort() == null) row.setSort(0);
    try {
      return mapper.insert(row) > 0;
    } catch (DuplicateKeyException e) {
      throw new ServiceException("同一房屋同一收费项目存在重叠的收费设置");
    }
  }

  private ChargeBindingBo batchRow(ChargeBindingBatchBo b, Long roomId) {
    ChargeBindingBo one = new ChargeBindingBo();
    one.setProjectId(b.getProjectId());
    one.setObjectType("ROOM");
    one.setObjectId(roomId);
    one.setChargeItemId(b.getChargeItemId());
    one.setChargeRuleId(b.getChargeRuleId());
    one.setEffectiveStartDate(b.getEffectiveStartDate());
    one.setEffectiveEndDate(b.getEffectiveEndDate());
    one.setStatus("0");
    one.setSort(b.getSort());
    one.setRemark(b.getRemark());
    return one;
  }

  private ChargeBindingBatchFailureVo failure(Room room, String code, String message) {
    return new ChargeBindingBatchFailureVo(room.getId(), roomName(room), code, message);
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean update(ChargeBindingBo bo) {
    ChargeBinding old = locked(bo.getId());
    Room room = lockedRoom(old.getObjectId());
    assertBindingRoom(old, room);
    access(room.getProjectId(), ProjectBusinessAction.WRITE);
    immutable(bo, old);
    ChargeItem item = validateItem(old.getChargeItemId(), room.getProjectId());
    ChargeRule rule = validateRule(old.getChargeRuleId(), item, room.getProjectId());
    LocalDate end = bo.getEffectiveEndDate();
    validateDates(old.getEffectiveStartDate(), end, rule);
    lockAndCheckOverlap(room, item.getId(), old.getEffectiveStartDate(), end, old.getId());
    validateStatus(bo.getStatus() == null ? old.getStatus() : bo.getStatus());
    ChargeBinding row = new ChargeBinding();
    row.setId(old.getId());
    row.setEffectiveEndDate(end);
    row.setStatus(bo.getStatus());
    row.setSort(bo.getSort());
    row.setRemark(bo.getRemark());
    return mapper.updateById(row) > 0;
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean changeStatus(Long id, String status, LocalDate effectiveEndDate) {
    ChargeBinding old = locked(id);
    Room room = lockedRoom(old.getObjectId());
    assertBindingRoom(old, room);
    access(room.getProjectId(), ProjectBusinessAction.WRITE);
    validateStatus(status);
    ChargeBinding row = new ChargeBinding();
    row.setId(id);
    row.setStatus(status);
    if ("1".equals(status)) {
      if (effectiveEndDate == null) throw new ServiceException("停用收费设置必须填写失效日期");
      if (effectiveEndDate.isBefore(old.getEffectiveStartDate()))
        throw new ServiceException("失效日期不能早于生效日期");
      mapper.lockItemBindings(old.getProjectId(), old.getObjectId(), old.getChargeItemId());
      if (mapper.overlaps(
          old.getProjectId(),
          old.getObjectId(),
          old.getChargeItemId(),
          old.getEffectiveStartDate(),
          effectiveEndDate,
          old.getId())) throw new ServiceException("失效日期与后续收费设置冲突");
      row.setEffectiveEndDate(effectiveEndDate);
    } else if (old.getEffectiveEndDate() != null)
      throw new ServiceException("已终止的收费设置不能直接重新启用，请新建收费设置");
    return mapper.updateById(row) > 0;
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean delete(Collection<Long> ids) {
    for (Long id : ids) {
      ChargeBinding b = locked(id);
      Room room = lockedRoom(b.getObjectId());
      assertBindingRoom(b, room);
      access(room.getProjectId(), ProjectBusinessAction.WRITE);
      checkChargeBindingCanDelete(b);
    }
    return mapper.deleteByIds(ids) > 0;
  }

  protected void checkChargeBindingCanDelete(ChargeBinding binding) {
    if (mapper.existsReceivable(binding.getId())) throw new ServiceException("收费设置已生成应收，不能删除");
  }

  private void immutable(ChargeBindingBo b, ChargeBinding o) {
    if (b.getProjectId() != null && !b.getProjectId().equals(o.getProjectId())) failImmutable();
    if (b.getObjectType() != null && !b.getObjectType().equals(o.getObjectType())) failImmutable();
    if (b.getObjectId() != null && !b.getObjectId().equals(o.getObjectId())) failImmutable();
    if (b.getChargeItemId() != null && !b.getChargeItemId().equals(o.getChargeItemId()))
      failImmutable();
    if (b.getChargeRuleId() != null && !b.getChargeRuleId().equals(o.getChargeRuleId()))
      failImmutable();
    if (b.getEffectiveStartDate() != null
        && !b.getEffectiveStartDate().equals(o.getEffectiveStartDate())) failImmutable();
  }

  private void failImmutable() {
    throw new ServiceException("所属项目、房屋、收费项目、计费规则和生效日期不允许修改");
  }

  private ChargeItem validateItem(Long id, Long projectId) {
    ChargeItem i = itemMapper.selectById(id);
    if (i == null || !"0".equals(i.getStatus())) throw new ServiceException("收费项目不存在或已停用");
    if (!projectId.equals(i.getProjectId())) throw new ServiceException("收费项目不属于房屋所在项目");
    if (!"ROOM".equals(i.getChargeObjectType())) throw new ServiceException("房屋只能绑定ROOM收费项目");
    return i;
  }

  private ChargeRule validateRule(Long id, ChargeItem item, Long projectId) {
    ChargeRule r = ruleMapper.selectById(id);
    if (r == null || !"0".equals(r.getStatus())) throw new ServiceException("计费规则不存在或已停用");
    if (!projectId.equals(r.getProjectId())) throw new ServiceException("计费规则不属于房屋所在项目");
    if (!item.getId().equals(r.getChargeItemId())) throw new ServiceException("计费规则与收费项目不匹配");
    return r;
  }

  private void validateDates(LocalDate start, LocalDate end, ChargeRule rule) {
    if (start == null) throw new ServiceException("收费生效日期不能为空");
    if (end != null && end.isBefore(start)) throw new ServiceException("收费失效日期不能早于生效日期");
    if (start.isBefore(rule.getEffectiveStartDate())
        || (rule.getEffectiveEndDate() != null
            && (start.isAfter(rule.getEffectiveEndDate())
                || end != null && end.isAfter(rule.getEffectiveEndDate()))))
      throw new ServiceException("收费设置有效期必须位于计费规则有效期内");
  }

  private void lockAndCheckOverlap(
      Room room, Long itemId, LocalDate start, LocalDate end, Long exclude) {
    mapper.lockItemBindings(room.getProjectId(), room.getId(), itemId);
    if (mapper.overlaps(room.getProjectId(), room.getId(), itemId, start, end, exclude))
      throw new ServiceException("同一房屋同一收费项目的有效期不能重叠");
  }

  private void validateStatus(String s) {
    if (!STATUSES.contains(s)) throw new ServiceException("状态不是有效值");
  }

  private ChargeBinding require(Long id) {
    ChargeBinding b = mapper.selectById(id);
    if (b == null) throw new ServiceException("房屋收费设置不存在");
    return b;
  }

  private ChargeBinding locked(Long id) {
    ChargeBinding b = mapper.selectActiveByIdForUpdate(id);
    if (b == null) throw new ServiceException("房屋收费设置不存在");
    return b;
  }

  private Room room(Long id) {
    Room r = roomMapper.selectById(id);
    if (r == null) throw new ServiceException("房屋不存在");
    return r;
  }

  private Room lockedRoom(Long id) {
    Room r = roomMapper.selectActiveByIdForUpdate(id);
    if (r == null) throw new ServiceException("房屋不存在");
    return r;
  }

  private void assertBindingRoom(ChargeBinding b, Room r) {
    if (!"ROOM".equals(b.getObjectType()) || !b.getProjectId().equals(r.getProjectId()))
      throw new ServiceException("收费设置与房屋项目不一致");
  }

  private void enrich(Collection<ChargeBindingVo> rows, Room room) {
    Project p = projectMapper.selectById(room.getProjectId());
    String roomName = roomName(room);
    Set<Long> itemIds = new HashSet<>(), ruleIds = new HashSet<>();
    rows.forEach(
        v -> {
          itemIds.add(v.getChargeItemId());
          ruleIds.add(v.getChargeRuleId());
        });
    Map<Long, ChargeItem> items = new HashMap<>();
    if (!itemIds.isEmpty())
      itemMapper.selectBatchIds(itemIds).forEach(x -> items.put(x.getId(), x));
    Map<Long, ChargeRule> rules = new HashMap<>();
    if (!ruleIds.isEmpty())
      ruleMapper.selectBatchIds(ruleIds).forEach(x -> rules.put(x.getId(), x));
    rows.forEach(
        v -> {
          v.setProjectName(p == null ? null : p.getProjectName());
          v.setRoomName(roomName);
          ChargeItem i = items.get(v.getChargeItemId());
          if (i != null) v.setChargeItemName(i.getItemName());
          ChargeRule r = rules.get(v.getChargeRuleId());
          if (r != null) {
            v.setChargeRuleName(r.getRuleName());
            v.setBillingMethod(r.getBillingMethod());
            v.setBillingCycle(r.getBillingCycle());
            v.setCycleMode(r.getCycleMode());
            v.setUnitPrice(r.getUnitPrice());
            v.setFixedAmount(r.getFixedAmount());
            v.setRuleEffectiveStartDate(r.getEffectiveStartDate());
            v.setRuleEffectiveEndDate(r.getEffectiveEndDate());
          }
        });
  }

  private String roomName(Room room) {
    Building building = buildingMapper.selectById(room.getBuildingId());
    BuildingUnit unit = room.getUnitId() == null ? null : unitMapper.selectById(room.getUnitId());
    return (building == null ? "" : name(building.getBuildingName(), building.getBuildingCode()))
        + (unit == null ? "" : name(unit.getUnitName(), unit.getUnitCode()))
        + name(room.getRoomNo(), room.getRoomCode());
  }

  private String name(String n, String code) {
    return n != null && !n.isBlank() ? n : (code == null ? "" : code);
  }

  private void access(Long p, ProjectBusinessAction a) {
    scopeService.assertBusinessAccess(p, PropertyBusinessDomain.FINANCE, a);
  }
}
