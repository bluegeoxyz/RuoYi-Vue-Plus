package org.dromara.property.charge.receivable.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.*;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_receivable")
public class Receivable extends BaseEntity {
    @TableId private Long id;
    private Long projectId;
    private String receivableNo;
    private Long batchId;
    private Long bindingId;
    private Long roomId;
    private Long chargeItemId;
    private Long chargeRuleId;
    private LocalDate generateMonth;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private LocalDate theoreticalStartDate;
    private LocalDate theoreticalEndDate;
    private LocalDate dueDate;
    private Long responsibleCustomerId;
    private String responsibleCustomerNameSnapshot;
    private String responsibleCustomerTypeSnapshot;
    private String roomFullNameSnapshot;
    private String chargeItemCodeSnapshot;
    private String chargeItemNameSnapshot;
    private String chargeRuleCodeSnapshot;
    private String chargeRuleNameSnapshot;
    private String billingMethodSnapshot;
    private String billingCycleSnapshot;
    private String priceCycleSnapshot;
    private String cycleModeSnapshot;
    private String dueDateModeSnapshot;
    private Integer dueDateOffsetDaysSnapshot;
    private BigDecimal areaSnapshot;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal fixedAmountSnapshot;
    private Integer theoreticalDays;
    private Integer actualDays;
    private BigDecimal receivableAmount;
    private BigDecimal paidAmount;
    private String paymentStatus;
    private String cancelReason;
    private Long cancelledBy;
    private LocalDateTime cancelledAt;
    @TableLogic private String delFlag;
}
