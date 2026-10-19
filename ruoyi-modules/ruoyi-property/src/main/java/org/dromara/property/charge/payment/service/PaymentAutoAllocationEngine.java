package org.dromara.property.charge.payment.service;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.charge.receivable.domain.Receivable;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;

@Component
public class PaymentAutoAllocationEngine {
  public List<AllocationPlan> calculate(List<Receivable> lockedRows, BigDecimal paymentAmount) {
    List<Receivable> rows = new ArrayList<>(lockedRows);
    rows.sort(
        Comparator.comparing(Receivable::getDueDate)
            .thenComparing(Receivable::getPeriodStartDate)
            .thenComparing(Receivable::getId));
    for (Receivable row : rows) validateConsistency(row);
    rows.removeIf(row -> "PAID".equals(row.getPaymentStatus()));
    BigDecimal available =
        rows.stream().map(this::outstanding).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (available.signum() == 0) throw new ServiceException("当前房屋没有可核销应收");
    if (paymentAmount.compareTo(available) > 0) throw new ServiceException("收款金额超过当前房屋可核销未收总额");
    BigDecimal remaining = paymentAmount;
    List<AllocationPlan> plans = new ArrayList<>();
    for (Receivable row : rows) {
      if (remaining.signum() == 0) break;
      BigDecimal amount = remaining.min(outstanding(row));
      plans.add(new AllocationPlan(row, amount));
      remaining = remaining.subtract(amount);
    }
    if (remaining.signum() != 0) throw new ServiceException("收款未能全部核销");
    return plans;
  }

  public void validateConsistency(Receivable row) {
    BigDecimal amount = row.getReceivableAmount(), paid = row.getPaidAmount();
    if (amount == null
        || paid == null
        || amount.signum() <= 0
        || paid.signum() < 0
        || paid.compareTo(amount) > 0)
      throw new ServiceException("应收数据异常：" + row.getReceivableNo());
    boolean ok =
        switch (row.getPaymentStatus()) {
          case "UNPAID" -> paid.signum() == 0;
          case "PARTIAL" -> paid.signum() > 0 && paid.compareTo(amount) < 0;
          case "PAID" -> paid.compareTo(amount) == 0;
          default -> false;
        };
    if (!ok) throw new ServiceException("应收金额与支付状态不一致：" + row.getReceivableNo());
  }

  private BigDecimal outstanding(Receivable row) {
    return row.getReceivableAmount().subtract(row.getPaidAmount());
  }

  public record AllocationPlan(Receivable receivable, BigDecimal amount) {}
}
