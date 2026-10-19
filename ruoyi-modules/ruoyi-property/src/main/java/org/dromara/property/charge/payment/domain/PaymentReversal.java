package org.dromara.property.charge.payment.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 收款冲销审计记录。 */
@Data @EqualsAndHashCode(callSuper=true) @TableName("pm_payment_reversal")
public class PaymentReversal extends BaseEntity {
    @TableId private Long id;
    private Long projectId; private String reversalNo; private Long paymentId;
    private BigDecimal reversalAmount; private LocalDateTime reversalTime; private String reason;
    @TableLogic private String delFlag;
}
