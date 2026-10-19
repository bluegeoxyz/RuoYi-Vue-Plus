package org.dromara.property.charge.receivable.engine;

import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class ChargePeriodCalculator {
  public Optional<ChargePeriod> calculate(
      String cycle, String mode, LocalDate start, LocalDate end, YearMonth month) {
    if (start == null) throw new ServiceException("收费设置生效日期不能为空");
    int months = cycleMonths(cycle);
    ChargePeriod p =
        "NATURAL".equals(mode)
            ? natural(months, start, end, month)
            : "ROLLING".equals(mode) ? rolling(months, start, end, month) : null;
    if (p == null && !("NATURAL".equals(mode) || "ROLLING".equals(mode)))
      throw new ServiceException("周期模式不是有效值");
    return Optional.ofNullable(p);
  }

  private ChargePeriod natural(int months, LocalDate start, LocalDate end, YearMonth month) {
    LocalDate targetStart = month.atDay(1), targetEnd = month.atEndOfMonth();
    LocalDate firstStart = naturalBlockStart(start, months),
        firstEnd = firstStart.plusMonths(months).minusDays(1);
    if (!start.isBefore(targetStart) && !start.isAfter(targetEnd))
      return crop(firstStart, firstEnd, start, end);
    LocalDate next = firstEnd.plusDays(1);
    if (next.isBefore(targetStart)) {
      long delta = ChronoUnit.MONTHS.between(YearMonth.from(next), month);
      next = next.plusMonths(Math.max(0, delta / months) * months);
      while (next.isBefore(targetStart)) next = next.plusMonths(months);
    }
    if (next.isAfter(targetEnd)) return null;
    return crop(next, next.plusMonths(months).minusDays(1), start, end);
  }

  private ChargePeriod rolling(int months, LocalDate anchor, LocalDate end, YearMonth month) {
    LocalDate targetStart = month.atDay(1), targetEnd = month.atEndOfMonth();
    long delta = ChronoUnit.MONTHS.between(YearMonth.from(anchor), month),
        index = Math.max(0, Math.floorDiv(delta, months));
    for (long i = Math.max(0, index - 1); i <= index + 2; i++) {
      LocalDate ts = anchor.plusMonths(i * months);
      if (!ts.isBefore(targetStart) && !ts.isAfter(targetEnd))
        return crop(ts, anchor.plusMonths((i + 1) * months).minusDays(1), anchor, end);
    }
    return null;
  }

  private ChargePeriod crop(LocalDate ts, LocalDate te, LocalDate bs, LocalDate be) {
    LocalDate as = ts.isBefore(bs) ? bs : ts, ae = be != null && te.isAfter(be) ? be : te;
    if (as.isAfter(ae)) return null;
    return new ChargePeriod(ts, te, as, ae, days(ts, te), days(as, ae));
  }

  private LocalDate naturalBlockStart(LocalDate d, int months) {
    int m = ((d.getMonthValue() - 1) / months) * months + 1;
    return LocalDate.of(d.getYear(), m, 1);
  }

  public int cycleMonths(String c) {
    return switch (c) {
      case "MONTH" -> 1;
      case "BIMONTHLY" -> 2;
      case "QUARTER" -> 3;
      case "HALF_YEAR" -> 6;
      case "YEAR" -> 12;
      default -> throw new ServiceException("计费周期不是有效值");
    };
  }

  private int days(LocalDate s, LocalDate e) {
    return Math.toIntExact(ChronoUnit.DAYS.between(s, e) + 1);
  }
}
