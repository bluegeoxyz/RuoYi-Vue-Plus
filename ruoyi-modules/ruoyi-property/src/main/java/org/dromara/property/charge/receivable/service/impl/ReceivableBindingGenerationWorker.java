package org.dromara.property.charge.receivable.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import cn.hutool.core.util.IdUtil;
import org.dromara.property.charge.receivable.domain.Receivable;
import org.dromara.property.charge.receivable.engine.*;
import org.dromara.property.charge.receivable.mapper.*;
import org.dromara.property.charge.receivable.service.ReceivableResponsiblePartyResolver;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;

@Component
@RequiredArgsConstructor
public class ReceivableBindingGenerationWorker {
  private final ReceivableGenerationMapper contextMapper;
  private final ReceivableMapper receivableMapper;
  private final ChargePeriodCalculator periods;
  private final ReceivableAmountCalculator amounts;
  private final ReceivableResponsiblePartyResolver responsiblePartyResolver;

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public BindingGenerationOutcome generate(
      Long bindingId, Long projectId, Long batchId, YearMonth month) {
    try {
      ReceivableGenerationContext c = contextMapper.selectContext(bindingId);
      validate(c, projectId);
      var optional =
          periods.calculate(
              c.getBillingCycle(), c.getCycleMode(), c.getBindingStart(), c.getBindingEnd(), month);
      if (optional.isEmpty())
        return BindingGenerationOutcome.of(BindingGenerationOutcome.Type.SKIPPED_NOT_DUE);
      ChargePeriod p = optional.get();
      if (receivableMapper.existsActivePeriod(bindingId, p.actualStart(), p.actualEnd()))
        return BindingGenerationOutcome.of(BindingGenerationOutcome.Type.SKIPPED_EXISTING);
      ResponsibleCustomerSnapshot owner =
          responsiblePartyResolver.resolve(c.getRoomId(), p.actualStart());
      BigDecimal amount =
          amounts.calculate(
              c.getBillingMethod(),
              c.getUsableArea(),
              c.getUnitPrice(),
              c.getFixedAmount(),
              c.getBillingCycle(),
              c.getPriceCycle(),
              p.actualDays(),
              p.theoreticalDays());
      Receivable r = build(c, p, owner, projectId, batchId, month, amount);
      try {
        receivableMapper.insert(r);
      } catch (DuplicateKeyException e) {
        if (isActivePeriodConflict(e))
          return BindingGenerationOutcome.of(BindingGenerationOutcome.Type.SKIPPED_EXISTING);
        throw e;
      }
      return BindingGenerationOutcome.of(BindingGenerationOutcome.Type.GENERATED);
    } catch (ServiceException e) {
      return BindingGenerationOutcome.failed(e.getMessage());
    }
  }

  private void validate(ReceivableGenerationContext c, Long projectId) {
    if (c == null) throw new ServiceException("收费设置关联数据不完整");
    if (!"ROOM".equals(c.getObjectType())) throw new ServiceException("当前仅支持房屋收费设置");
    if (!projectId.equals(c.getProjectId())
        || !projectId.equals(c.getRoomProjectId())
        || !projectId.equals(c.getItemProjectId())
        || !projectId.equals(c.getRuleProjectId())) throw new ServiceException("收费设置存在跨项目脏数据");
    if (!c.getChargeItemId().equals(c.getRuleItemId())) throw new ServiceException("收费项目与计费规则不匹配");
    if (c.getRuleStart() == null
        || c.getBindingStart().isBefore(c.getRuleStart())
        || c.getRuleEnd() != null
            && (c.getBindingStart().isAfter(c.getRuleEnd())
                || c.getBindingEnd() != null && c.getBindingEnd().isAfter(c.getRuleEnd())))
      throw new ServiceException("收费设置有效期超出计费规则有效期");
    if (c.getDueDateOffsetDays() == null
        || c.getDueDateOffsetDays() < 0
        || c.getDueDateOffsetDays() > 7) throw new ServiceException("应缴日偏移天数不是有效值");
  }

  private Receivable build(
      ReceivableGenerationContext c,
      ChargePeriod p,
      ResponsibleCustomerSnapshot owner,
      Long projectId,
      Long batchId,
      YearMonth month,
      BigDecimal amount) {
    Receivable r = new Receivable();
    long id = IdUtil.getSnowflakeNextId();
    r.setId(id);
    r.setProjectId(projectId);
    r.setReceivableNo("RCV" + month.toString().replace("-", "") + id);
    r.setBatchId(batchId);
    r.setBindingId(c.getBindingId());
    r.setRoomId(c.getRoomId());
    r.setChargeItemId(c.getChargeItemId());
    r.setChargeRuleId(c.getChargeRuleId());
    r.setGenerateMonth(month.atDay(1));
    r.setPeriodStartDate(p.actualStart());
    r.setPeriodEndDate(p.actualEnd());
    r.setTheoreticalStartDate(p.theoreticalStart());
    r.setTheoreticalEndDate(p.theoreticalEnd());
    r.setDueDate(dueDate(p.actualStart(), c.getDueDateMode(), c.getDueDateOffsetDays()));
    r.setResponsibleCustomerId(owner.getCustomerId());
    r.setResponsibleCustomerNameSnapshot(owner.getCustomerName());
    r.setResponsibleCustomerTypeSnapshot(owner.getCustomerType());
    r.setRoomFullNameSnapshot(
        name(c.getBuildingName(), c.getBuildingCode())
            + name(c.getUnitName(), c.getUnitCode())
            + name(c.getRoomNo(), c.getRoomCode()));
    r.setChargeItemCodeSnapshot(c.getItemCode());
    r.setChargeItemNameSnapshot(c.getItemName());
    r.setChargeRuleCodeSnapshot(c.getRuleCode());
    r.setChargeRuleNameSnapshot(c.getRuleName());
    r.setBillingMethodSnapshot(c.getBillingMethod());
    r.setBillingCycleSnapshot(c.getBillingCycle());
    r.setPriceCycleSnapshot(c.getPriceCycle());
    r.setCycleModeSnapshot(c.getCycleMode());
    r.setDueDateModeSnapshot(c.getDueDateMode());
    r.setDueDateOffsetDaysSnapshot(c.getDueDateOffsetDays());
    if ("AREA".equals(c.getBillingMethod())) {
      r.setAreaSnapshot(c.getUsableArea());
      r.setUnitPriceSnapshot(c.getUnitPrice());
    } else r.setFixedAmountSnapshot(c.getFixedAmount());
    r.setTheoreticalDays(p.theoreticalDays());
    r.setActualDays(p.actualDays());
    r.setReceivableAmount(amount);
    r.setPaidAmount(BigDecimal.ZERO.setScale(2));
    r.setPaymentStatus("UNPAID");
    return r;
  }

  private LocalDate dueDate(LocalDate start, String mode, int offset) {
    return switch (mode) {
      case "BEFORE_PERIOD_START" -> start.minusDays(offset);
      case "AFTER_PERIOD_START" -> start.plusDays(offset);
      default -> throw new ServiceException("应缴日模式不是有效值");
    };
  }

  private String name(String value, String fallback) {
    return value != null && !value.isBlank() ? value : fallback == null ? "" : fallback;
  }

  private boolean isActivePeriodConflict(DuplicateKeyException e) {
    Throwable t = e;
    while (t != null) {
      String m = t.getMessage();
      if (m != null && m.contains("uk_pm_receivable_active_period")) return true;
      t = t.getCause();
    }
    return false;
  }
}
