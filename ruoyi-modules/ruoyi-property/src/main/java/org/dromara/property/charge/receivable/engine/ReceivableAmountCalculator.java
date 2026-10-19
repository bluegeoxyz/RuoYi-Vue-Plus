package org.dromara.property.charge.receivable.engine;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;
import java.math.*;

@Component
@RequiredArgsConstructor
public class ReceivableAmountCalculator {
  private static final int SCALE = 12;
  private final ChargePeriodCalculator periods;

  public BigDecimal calculate(
      String method,
      BigDecimal area,
      BigDecimal unitPrice,
      BigDecimal fixed,
      String billing,
      String price,
      int actual,
      int theoretical) {
    BigDecimal base;
    if ("AREA".equals(method)) {
      if (area == null) throw new ServiceException("房屋使用面积为空");
      if (unitPrice == null || area.signum() < 0 || unitPrice.signum() < 0)
        throw new ServiceException("面积或单价不是有效值");
      base = area.multiply(unitPrice);
    } else if ("FIXED".equals(method)) {
      if (fixed == null || fixed.signum() < 0) throw new ServiceException("固定金额不是有效值");
      base = fixed;
    } else throw new ServiceException("计费方式不是有效值");
    BigDecimal ratio =
        BigDecimal.valueOf(periods.cycleMonths(billing))
            .divide(BigDecimal.valueOf(periods.cycleMonths(price)), SCALE, RoundingMode.HALF_UP);
    BigDecimal amount = base.multiply(ratio);
    if (actual < theoretical)
      amount =
          amount
              .multiply(BigDecimal.valueOf(actual))
              .divide(BigDecimal.valueOf(theoretical), SCALE, RoundingMode.HALF_UP);
    return amount.setScale(2, RoundingMode.HALF_UP);
  }
}
