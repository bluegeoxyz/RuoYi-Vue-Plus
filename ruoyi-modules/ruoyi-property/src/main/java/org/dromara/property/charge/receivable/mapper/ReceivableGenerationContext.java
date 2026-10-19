package org.dromara.property.charge.receivable.mapper;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReceivableGenerationContext {
  private Long bindingId;
  private Long projectId;
  private String objectType;
  private Long roomId;
  private Long chargeItemId;
  private Long chargeRuleId;
  private LocalDate bindingStart;
  private LocalDate bindingEnd;
  private Long roomProjectId;
  private BigDecimal usableArea;
  private String roomCode;
  private String roomNo;
  private String buildingCode;
  private String buildingName;
  private String unitCode;
  private String unitName;
  private Long itemProjectId;
  private String itemCode;
  private String itemName;
  private Long ruleProjectId;
  private Long ruleItemId;
  private String ruleCode;
  private String ruleName;
  private String billingMethod;
  private String billingCycle;
  private String priceCycle;
  private String cycleMode;
  private BigDecimal unitPrice;
  private BigDecimal fixedAmount;
  private LocalDate ruleStart;
  private LocalDate ruleEnd;
  private String dueDateMode;
  private Integer dueDateOffsetDays;
}
