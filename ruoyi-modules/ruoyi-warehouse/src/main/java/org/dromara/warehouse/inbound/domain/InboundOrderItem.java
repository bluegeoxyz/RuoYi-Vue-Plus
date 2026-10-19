package org.dromara.warehouse.inbound.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/** 入库单明细实体。 */
@Data @EqualsAndHashCode(callSuper = true) @TableName("wm_inbound_order_item")
public class InboundOrderItem extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long inboundOrderId;
    private Long materialId;
    private Long locationId;
    private BigDecimal quantity;
    private Long unitId;
    private String remark;
}
