package org.dromara.property.charge.payment.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 登记收款房屋选项。
 */
@Data
public class PaymentRoomOptionVo {

    private Long roomId;
    private String roomFullName;
    private BigDecimal outstandingAmount;
    private Long unpaidCount;
    private LocalDate earliestDueDate;
}
