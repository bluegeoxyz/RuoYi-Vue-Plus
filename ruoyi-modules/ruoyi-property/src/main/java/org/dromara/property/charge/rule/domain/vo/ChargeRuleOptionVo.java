package org.dromara.property.charge.rule.domain.vo;
import lombok.Data; import java.math.BigDecimal; import java.time.LocalDate;
@Data public class ChargeRuleOptionVo {
 private Long id; private Long projectId; private Long chargeItemId; private String chargeItemName; private String ruleCode; private String ruleName;
 private String billingMethod; private String billingCycle; private String priceCycle; private String cycleMode; private BigDecimal unitPrice; private BigDecimal fixedAmount;
 private String dueDateMode; private Integer dueDateOffsetDays;
 private LocalDate effectiveStartDate; private LocalDate effectiveEndDate;
}
