package org.dromara.property.charge.payment.domain.bo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentQueryBo {
    private Long projectId;
    private Long roomId;
    private String paymentNo;
    private String roomKeyword;
    private String payerKeyword;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentTimeFrom;
    private LocalDateTime paymentTimeTo;
}
