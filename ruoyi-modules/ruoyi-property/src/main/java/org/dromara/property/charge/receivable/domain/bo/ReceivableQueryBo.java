package org.dromara.property.charge.receivable.domain.bo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReceivableQueryBo {
    private Long projectId;
    private LocalDate generateMonth;
    private String receivableNo;
    private String roomKeyword;
    private Long roomId;
    private Long chargeItemId;
    private String responsibleCustomerKeyword;
    private String paymentStatus;
    private Boolean overdueOnly;
    private LocalDate periodStartFrom;
    private LocalDate periodStartTo;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
}
