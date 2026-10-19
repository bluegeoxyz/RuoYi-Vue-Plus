package org.dromara.property.charge.payment.domain.vo;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 整笔收款冲销结果。 */
@Data @Builder
public class PaymentReverseResultVo {
    private Long paymentId; private String paymentNo; private Long reversalId; private String reversalNo;
    private BigDecimal reversalAmount; private LocalDateTime reversalTime; private String reason;
    private String paymentStatus; private int affectedReceivableCount;
    private List<PaymentReverseReceivableVo> receivables;
}
