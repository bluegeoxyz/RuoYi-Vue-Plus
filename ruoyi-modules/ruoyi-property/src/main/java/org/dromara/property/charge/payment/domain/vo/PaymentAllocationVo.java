package org.dromara.property.charge.payment.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.*;

@Data
public class PaymentAllocationVo {
    private Long id;
    private Long receivableId;
    private String receivableNo;
    private String chargeItemNameSnapshot;
    private String responsibleCustomerNameSnapshot;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal allocationAmount;
    private LocalDateTime allocationTime;
    private BigDecimal receivablePaidAmount;
    private BigDecimal receivableOutstandingAmount;
    private String paymentStatus;
}
