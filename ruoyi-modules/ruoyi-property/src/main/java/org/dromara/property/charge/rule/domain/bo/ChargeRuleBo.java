package org.dromara.property.charge.rule.domain.bo;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.*;
import org.dromara.property.charge.rule.domain.ChargeRule;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data @AutoMapper(target=ChargeRule.class, reverseConvertGenerate=false)
public class ChargeRuleBo {
 @NotNull(groups=EditGroup.class) private Long id;
 @NotNull(groups=AddGroup.class) private Long projectId;
 @NotNull(groups={AddGroup.class,EditGroup.class}) private Long chargeItemId;
 @NotBlank(groups=AddGroup.class) @Size(max=64) private String ruleCode;
 @NotBlank(groups={AddGroup.class,EditGroup.class}) @Size(max=128) private String ruleName;
 @NotBlank(groups={AddGroup.class,EditGroup.class}) @Size(max=16) private String billingMethod;
 @NotBlank(groups={AddGroup.class,EditGroup.class}) @Size(max=16) private String billingCycle;
 @NotBlank(groups={AddGroup.class,EditGroup.class}) @Size(max=16) private String priceCycle;
 @NotBlank(groups={AddGroup.class,EditGroup.class}) @Size(max=16) private String cycleMode;
 @NotBlank(groups={AddGroup.class,EditGroup.class}) @Size(max=32) private String dueDateMode;
 @NotNull(groups={AddGroup.class,EditGroup.class}) @Min(0) @Max(7) private Integer dueDateOffsetDays;
 private BigDecimal unitPrice; private BigDecimal fixedAmount;
 @NotNull(groups={AddGroup.class,EditGroup.class}) private LocalDate effectiveStartDate;
 private LocalDate effectiveEndDate; @Size(max=1) private String status; private Integer sort; @Size(max=500) private String remark;
}
