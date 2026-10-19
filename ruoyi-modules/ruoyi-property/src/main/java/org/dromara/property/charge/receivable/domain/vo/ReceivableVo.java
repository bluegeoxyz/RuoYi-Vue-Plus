package org.dromara.property.charge.receivable.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.*;

@Data
public class ReceivableVo {
  private Long id;
  private Long projectId;
  private String projectName;
  private String receivableNo;
  private Long batchId;
  private String batchNo;
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
  private BigDecimal outstandingAmount;
  private String paymentStatus;
  private Boolean overdue;
  private String cancelReason;
  private Long cancelledBy;
  private LocalDateTime cancelledAt;
  private LocalDateTime createTime;
}
