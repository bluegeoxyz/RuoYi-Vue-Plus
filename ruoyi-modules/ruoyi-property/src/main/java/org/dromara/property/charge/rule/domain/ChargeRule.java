package org.dromara.property.charge.rule.domain;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data @EqualsAndHashCode(callSuper=true) @TableName("pm_charge_rule")
public class ChargeRule extends BaseEntity {
 @TableId private Long id; private Long projectId; private Long chargeItemId; private String ruleCode; private String ruleName;
 private String billingMethod; private String billingCycle; private String priceCycle; private String cycleMode; private BigDecimal unitPrice; private BigDecimal fixedAmount;
 private String dueDateMode; private Integer dueDateOffsetDays;
 private LocalDate effectiveStartDate; private LocalDate effectiveEndDate; private String status; private Integer sort;
}
