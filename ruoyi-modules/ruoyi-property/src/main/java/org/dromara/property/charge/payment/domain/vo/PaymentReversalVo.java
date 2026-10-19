package org.dromara.property.charge.payment.domain.vo;

import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 收款冲销审计信息。 */
@Data
public class PaymentReversalVo {
    private Long id; private String reversalNo; private BigDecimal reversalAmount;
    private LocalDateTime reversalTime; private String reason; private Long createBy;
    @Translation(type=TransConstant.USER_ID_TO_NAME,mapper="createBy") private String createByName;
}
