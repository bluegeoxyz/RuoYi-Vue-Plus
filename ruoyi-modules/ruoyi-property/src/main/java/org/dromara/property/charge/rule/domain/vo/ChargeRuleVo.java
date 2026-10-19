package org.dromara.property.charge.rule.domain.vo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.property.charge.rule.domain.ChargeRule;
import java.math.BigDecimal; import java.time.*;
@Data @AutoMapper(target=ChargeRule.class)
public class ChargeRuleVo {
 private Long id; private Long projectId; private String projectName; private Long chargeItemId; private String chargeItemName;
 private String ruleCode; private String ruleName; private String billingMethod; private String billingCycle; private String priceCycle; private String cycleMode;
 private String dueDateMode; private Integer dueDateOffsetDays;
 private BigDecimal unitPrice; private BigDecimal fixedAmount; private LocalDate effectiveStartDate; private LocalDate effectiveEndDate;
 private String status; private Integer sort; private String remark; private LocalDateTime createTime;
}
