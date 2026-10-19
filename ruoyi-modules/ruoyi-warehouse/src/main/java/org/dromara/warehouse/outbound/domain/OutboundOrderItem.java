package org.dromara.warehouse.outbound.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.io.Serial;
import java.math.BigDecimal;

/** 出库单明细实体。 */
@Data @EqualsAndHashCode(callSuper = true) @TableName("wm_outbound_order_item")
public class OutboundOrderItem extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long outboundOrderId;
    private Long materialId;
    private Long locationId;
    private Long unitId;
    private BigDecimal quantity;
    private String remark;
}
