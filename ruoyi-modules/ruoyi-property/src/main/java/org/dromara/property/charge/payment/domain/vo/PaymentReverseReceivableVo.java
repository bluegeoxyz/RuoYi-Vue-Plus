package org.dromara.property.charge.payment.domain.vo;

import lombok.*;
import java.math.BigDecimal;

/** 单条应收的冲销回退结果。 */
@Data @Builder
public class PaymentReverseReceivableVo {
    private Long receivableId; private String receivableNo;
    private BigDecimal reversedAllocationAmount; private BigDecimal paidAmountAfter;
    private BigDecimal outstandingAmountAfter; private String paymentStatusAfter;
}
