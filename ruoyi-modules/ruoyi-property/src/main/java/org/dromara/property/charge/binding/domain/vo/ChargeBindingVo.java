package org.dromara.property.charge.binding.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.property.charge.binding.domain.ChargeBinding;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AutoMapper(target = ChargeBinding.class)
public class ChargeBindingVo {
    private Long id;
    private Long projectId;
    private String projectName;
    private String objectType;
    private Long objectId;
    private String roomName;
    private Long chargeItemId;
    private String chargeItemName;
    private Long chargeRuleId;
    private String chargeRuleName;
    private String billingMethod;
    private String billingCycle;
    private String priceCycle;
    private String cycleMode;
    private String dueDateMode;
    private Integer dueDateOffsetDays;
    private BigDecimal unitPrice;
    private BigDecimal fixedAmount;
    private LocalDate ruleEffectiveStartDate;
    private LocalDate ruleEffectiveEndDate;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private String status;
    private Integer sort;
    private String remark;
}
