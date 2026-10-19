package org.dromara.property.charge.context.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 收款页面可见的最小待收应收选项。 */
public record PaymentReceivableOptionVo(Long id, String receivableNo, Long projectId, Long roomId,
                                        String chargeItemNameSnapshot, String responsibleCustomerNameSnapshot,
                                        LocalDate periodStartDate, LocalDate periodEndDate, LocalDate dueDate,
                                        BigDecimal receivableAmount, BigDecimal paidAmount,
                                        BigDecimal outstandingAmount, String paymentStatus) {
}
