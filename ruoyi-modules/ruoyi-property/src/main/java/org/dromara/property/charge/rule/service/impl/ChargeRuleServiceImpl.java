package org.dromara.property.charge.rule.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.charge.item.domain.ChargeItem;
import org.dromara.property.charge.item.mapper.ChargeItemMapper;
import org.dromara.property.charge.rule.domain.ChargeRule;
import org.dromara.property.charge.rule.domain.bo.ChargeRuleBo;
import org.dromara.property.charge.rule.domain.vo.*;
import org.dromara.property.charge.rule.mapper.ChargeRuleMapper;
import org.dromara.property.charge.rule.service.IChargeRuleService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.scope.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeRuleServiceImpl implements IChargeRuleService {
  private static final Set<String> METHODS = Set.of("AREA", "FIXED"),
      CYCLES = Set.of("MONTH", "BIMONTHLY", "QUARTER", "HALF_YEAR", "YEAR"),
      MODES = Set.of("NATURAL", "ROLLING"),
      DUE_MODES = Set.of("BEFORE_PERIOD_START", "AFTER_PERIOD_START"),
      STATUSES = Set.of("0", "1");
  private final ChargeRuleMapper mapper;
  private final ChargeItemMapper itemMapper;
  private final ProjectMapper projectMapper;
  private final PropertyProjectScopeService scopeService;

  public ChargeRuleVo queryById(Long id) {
    ChargeRule row = require(id);
    access(row.getProjectId(), ProjectBusinessAction.READ);
    ChargeRuleVo vo = mapper.selectVoById(id);
    enrich(List.of(vo));
    return vo;
  }

  public PageResult<ChargeRuleVo> queryPage(ChargeRuleBo bo, PageQuery pq) {
    PropertyProjectScope s = scope(ProjectBusinessAction.READ);
    if (s.type() == PropertyProjectScope.Type.NONE) return PageResult.build(List.of(), 0L);
    Page<ChargeRuleVo> p = mapper.selectVoPage(pq.build(), query(bo, s));
    enrich(p.getRecords());
    return PageResult.build(p.getRecords(), p.getTotal());
  }

  public List<ChargeRuleExportVo> queryExport(ChargeRuleBo bo) {
    PropertyProjectScope s = scope(ProjectBusinessAction.EXPORT);
    if (s.type() == PropertyProjectScope.Type.NONE) return List.of();
    List<ChargeRuleVo> rows = mapper.selectVoList(query(bo, s));
    enrich(rows);
    return rows.stream()
        .map(
            v -> {
              ChargeRuleExportVo e = BeanUtil.copyProperties(v, ChargeRuleExportVo.class);
              e.setBillingStandard(
                  "AREA".equals(v.getBillingMethod())
                      ? v.getUnitPrice() + " 元/㎡"
                      : v.getFixedAmount() + " 元/周期");
              return e;
            })
        .toList();
  }

  public List<ChargeRuleOptionVo> options(ChargeRuleBo bo) {
    PropertyProjectScope s = scope(ProjectBusinessAction.OPTION);
    if (s.type() == PropertyProjectScope.Type.NONE) return List.of();
    LocalDate today = LocalDate.now();
    LambdaQueryWrapper<ChargeRule> q = query(bo, s);
    q.eq(ChargeRule::getStatus, "0")
        .le(ChargeRule::getEffectiveStartDate, today)
        .and(
            w ->
                w.isNull(ChargeRule::getEffectiveEndDate)
                    .or()
                    .ge(ChargeRule::getEffectiveEndDate, today));
    List<ChargeRuleVo> rows = mapper.selectVoList(q);
    enrich(rows);
    return BeanUtil.copyToList(rows, ChargeRuleOptionVo.class);
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean insert(ChargeRuleBo bo) {
    access(bo.getProjectId(), ProjectBusinessAction.WRITE);
    validateProject(bo.getProjectId());
    validateItem(bo.getChargeItemId(), bo.getProjectId());
    defaults(bo, null);
    validate(bo);
    if (mapper.countActiveCode(bo.getProjectId(), bo.getRuleCode(), null) > 0)
      throw new ServiceException("同一项目下计费规则编码已存在");
    ChargeRule row = BeanUtil.copyProperties(bo, ChargeRule.class);
    if (row.getStatus() == null) row.setStatus("0");
    if (row.getSort() == null) row.setSort(0);
    try {
      return mapper.insert(row) > 0;
    } catch (DuplicateKeyException e) {
      throw new ServiceException("同一项目下计费规则编码已存在");
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean update(ChargeRuleBo bo) {
    ChargeRule old = locked(bo.getId());
    access(old.getProjectId(), ProjectBusinessAction.WRITE);
    if (bo.getProjectId() != null && !bo.getProjectId().equals(old.getProjectId()))
      throw new ServiceException("所属项目不允许修改");
    if (bo.getRuleCode() != null && !bo.getRuleCode().equals(old.getRuleCode()))
      throw new ServiceException("规则编码不允许修改");
    validateItem(bo.getChargeItemId(), old.getProjectId());
    defaults(bo, old);
    validate(bo);
    if (mapper.existsActiveChargeBinding(old.getId())) {
      protectBillingFields(old, bo);
      if (bo.getEffectiveEndDate() != null
          && mapper.existsBindingBeyond(old.getId(), bo.getEffectiveEndDate()))
        throw new ServiceException("失效日期不能早于已有关联收费设置的结束日期");
    }
    ChargeRule row = BeanUtil.copyProperties(bo, ChargeRule.class);
    row.setProjectId(old.getProjectId());
    row.setRuleCode(old.getRuleCode());
    return mapper.updateById(row) > 0;
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean changeStatus(Long id, String status) {
    ChargeRule old = locked(id);
    access(old.getProjectId(), ProjectBusinessAction.WRITE);
    if (!STATUSES.contains(status)) throw new ServiceException("状态不是有效值");
    ChargeRule row = new ChargeRule();
    row.setId(id);
    row.setStatus(status);
    return mapper.updateById(row) > 0;
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean delete(Collection<Long> ids) {
    for (Long id : ids) {
      ChargeRule old = locked(id);
      access(old.getProjectId(), ProjectBusinessAction.WRITE);
      checkChargeRuleCanDelete(old);
    }
    return mapper.deleteByIds(ids) > 0;
  }

  protected void checkChargeRuleCanDelete(ChargeRule rule) {
    if (mapper.existsActiveChargeBinding(rule.getId()))
      throw new ServiceException("计费规则已被房屋收费设置引用，不能删除");
  }

  private LambdaQueryWrapper<ChargeRule> query(ChargeRuleBo bo, PropertyProjectScope s) {
    return QueryBuilder.lambda(ChargeRule.class)
        .eqIfPresent(ChargeRule::getProjectId, bo.getProjectId())
        .eqIfPresent(ChargeRule::getChargeItemId, bo.getChargeItemId())
        .likeIfText(ChargeRule::getRuleCode, bo.getRuleCode())
        .likeIfText(ChargeRule::getRuleName, bo.getRuleName())
        .eqIfText(ChargeRule::getBillingMethod, bo.getBillingMethod())
        .eqIfText(ChargeRule::getBillingCycle, bo.getBillingCycle())
        .eqIfText(ChargeRule::getCycleMode, bo.getCycleMode())
        .eqIfText(ChargeRule::getStatus, bo.getStatus())
        .in(
            s.type() == PropertyProjectScope.Type.EXPLICIT_IDS,
            ChargeRule::getProjectId,
            s.projectIds())
        .orderByAsc(ChargeRule::getSort)
        .orderByDesc(ChargeRule::getCreateTime)
        .build();
  }

  private void validate(ChargeRuleBo b) {
    if (!METHODS.contains(b.getBillingMethod())) throw new ServiceException("计费方式不是有效值");
    if (!CYCLES.contains(b.getBillingCycle())) throw new ServiceException("计费周期不是有效值");
    if (!CYCLES.contains(b.getPriceCycle())) throw new ServiceException("计价周期不是有效值");
    if (!MODES.contains(b.getCycleMode())) throw new ServiceException("周期模式不是有效值");
    if (!DUE_MODES.contains(b.getDueDateMode())) throw new ServiceException("应缴日模式不是有效值");
    if (b.getDueDateOffsetDays() == null
        || b.getDueDateOffsetDays() < 0
        || b.getDueDateOffsetDays() > 7) throw new ServiceException("应缴日偏移天数必须在0到7之间");
    String status = b.getStatus() == null ? "0" : b.getStatus();
    if (!STATUSES.contains(status)) throw new ServiceException("状态不是有效值");
    if (b.getEffectiveEndDate() != null
        && b.getEffectiveEndDate().isBefore(b.getEffectiveStartDate()))
      throw new ServiceException("失效日期不能早于生效日期");
    if ("AREA".equals(b.getBillingMethod())) {
      nonnegative(b.getUnitPrice(), "按面积计费时单价不能为空且不能小于0");
      if (b.getFixedAmount() != null) throw new ServiceException("按面积计费时固定金额必须为空");
    } else {
      nonnegative(b.getFixedAmount(), "固定金额计费时金额不能为空且不能小于0");
      if (b.getUnitPrice() != null) throw new ServiceException("固定金额计费时单价必须为空");
    }
  }

  private void protectBillingFields(ChargeRule old, ChargeRuleBo b) {
    if (!Objects.equals(old.getChargeItemId(), b.getChargeItemId())
        || !Objects.equals(old.getBillingMethod(), b.getBillingMethod())
        || !Objects.equals(old.getBillingCycle(), b.getBillingCycle())
        || !Objects.equals(old.getPriceCycle(), b.getPriceCycle())
        || !Objects.equals(old.getCycleMode(), b.getCycleMode())
        || !Objects.equals(old.getUnitPrice(), b.getUnitPrice())
        || !Objects.equals(old.getFixedAmount(), b.getFixedAmount())
        || !Objects.equals(old.getEffectiveStartDate(), b.getEffectiveStartDate())
        || !Objects.equals(old.getDueDateMode(), b.getDueDateMode())
        || !Objects.equals(old.getDueDateOffsetDays(), b.getDueDateOffsetDays()))
      throw new ServiceException("计费规则已被收费设置引用，计费方式、周期、价格、生效日期和应缴日规则不允许修改，请新建规则");
  }

  private void defaults(ChargeRuleBo b, ChargeRule old) {
    if (b.getPriceCycle() == null)
      b.setPriceCycle(old == null || old.getPriceCycle() == null ? "MONTH" : old.getPriceCycle());
    if (b.getDueDateMode() == null)
      b.setDueDateMode(
          old == null || old.getDueDateMode() == null
              ? "AFTER_PERIOD_START"
              : old.getDueDateMode());
    if (b.getDueDateOffsetDays() == null)
      b.setDueDateOffsetDays(
          old == null || old.getDueDateOffsetDays() == null ? 0 : old.getDueDateOffsetDays());
  }

  private void nonnegative(BigDecimal n, String msg) {
    if (n == null || n.signum() < 0) throw new ServiceException(msg);
  }

  private void validateProject(Long id) {
    Project p = projectMapper.selectById(id);
    if (p == null || !"0".equals(p.getStatus())) throw new ServiceException("项目不存在或已停用");
  }

  private void validateItem(Long id, Long projectId) {
    ChargeItem i = itemMapper.selectById(id);
    if (i == null || !"0".equals(i.getStatus())) throw new ServiceException("收费项目不存在或已停用");
    if (!projectId.equals(i.getProjectId())) throw new ServiceException("收费项目不属于当前项目");
    if (!"PERIODIC".equals(i.getChargeType())) throw new ServiceException("计费规则只允许关联周期性收费项目");
  }

  private ChargeRule require(Long id) {
    ChargeRule r = mapper.selectById(id);
    if (r == null) throw new ServiceException("计费规则不存在");
    return r;
  }

  private ChargeRule locked(Long id) {
    ChargeRule r = mapper.selectActiveByIdForUpdate(id);
    if (r == null) throw new ServiceException("计费规则不存在");
    return r;
  }

  private void enrich(Collection<ChargeRuleVo> rows) {
    Set<Long> pids =
        rows.stream()
            .map(ChargeRuleVo::getProjectId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    Set<Long> iids =
        rows.stream()
            .map(ChargeRuleVo::getChargeItemId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    Map<Long, Project> ps =
        pids.isEmpty()
            ? Map.of()
            : projectMapper.selectBatchIds(pids).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));
    Map<Long, ChargeItem> is =
        iids.isEmpty()
            ? Map.of()
            : itemMapper.selectBatchIds(iids).stream()
                .collect(Collectors.toMap(ChargeItem::getId, Function.identity()));
    rows.forEach(
        v -> {
          Project p = ps.get(v.getProjectId());
          if (p != null) v.setProjectName(p.getProjectName());
          ChargeItem i = is.get(v.getChargeItemId());
          if (i != null) v.setChargeItemName(i.getItemName());
        });
  }

  private PropertyProjectScope scope(ProjectBusinessAction a) {
    return scopeService.resolveBusinessScope(PropertyBusinessDomain.FINANCE, a);
  }

  private void access(Long id, ProjectBusinessAction a) {
    scopeService.assertBusinessAccess(id, PropertyBusinessDomain.FINANCE, a);
  }
}
