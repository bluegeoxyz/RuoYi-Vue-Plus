package org.dromara.property.charge.receivable.domain.vo;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ReceivableSummaryVo {
    private BigDecimal receivableTotal;
    private BigDecimal paidTotal;
    private BigDecimal outstandingTotal;
    private BigDecimal overdueOutstandingTotal;
}
