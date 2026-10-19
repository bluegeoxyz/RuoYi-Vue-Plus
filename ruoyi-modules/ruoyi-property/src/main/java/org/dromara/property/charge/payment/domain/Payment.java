package org.dromara.property.charge.payment.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper=true) @TableName("pm_payment")
public class Payment extends BaseEntity {
    @TableId private Long id;
    private Long projectId; private String paymentNo; private Long roomId; private String roomFullNameSnapshot;
    private Long payerCustomerId; private String payerNameSnapshot; private String payerTypeSnapshot;
    private BigDecimal paymentAmount; private BigDecimal allocatedAmount; private String paymentMethod;
    private String transactionNo; private LocalDateTime paymentTime; private String status; private String remark;
    @TableLogic private String delFlag;
}
