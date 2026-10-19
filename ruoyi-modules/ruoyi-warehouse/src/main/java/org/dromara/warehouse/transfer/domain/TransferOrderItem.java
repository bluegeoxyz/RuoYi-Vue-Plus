package org.dromara.warehouse.transfer.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.io.Serial;
import java.math.BigDecimal;

/** 调拨单明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_transfer_order_item")
public class TransferOrderItem extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long transferOrderId;
    private Long materialId;
    private Long sourceLocationId;
    private Long targetLocationId;
    private Long unitId;
    private BigDecimal quantity;
    private String remark;
}
