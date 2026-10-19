package org.dromara.property.charge.payment.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @EqualsAndHashCode(callSuper=true) @TableName("pm_payment_allocation")
public class PaymentAllocation extends BaseEntity {
    @TableId private Long id;
    private Long projectId; private Long paymentId; private Long receivableId;
    private BigDecimal allocationAmount; private LocalDateTime allocationTime; private String remark;
    @TableLogic private String delFlag;
}
