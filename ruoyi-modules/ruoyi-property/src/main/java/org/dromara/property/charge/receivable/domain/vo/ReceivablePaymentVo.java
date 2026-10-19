package org.dromara.property.charge.receivable.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReceivablePaymentVo {
    private Long paymentId;
    private String paymentNo;
    private String payerNameSnapshot;
    private String payerTypeSnapshot;
    private LocalDateTime paymentTime;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal allocationAmount;
}
