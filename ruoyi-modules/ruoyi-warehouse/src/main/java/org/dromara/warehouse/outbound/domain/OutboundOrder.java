package org.dromara.warehouse.outbound.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.io.Serial;
import java.time.LocalDateTime;

/** 出库单实体。 */
@Data @EqualsAndHashCode(callSuper = true) @TableName("wm_outbound_order")
public class OutboundOrder extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private String outboundNo;
    private Long companyId;
    private Long projectId;
    private Long warehouseId;
    private String outboundType;
    private String status;
    private LocalDateTime outboundAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private Long operatorId;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private String remark;
    @TableLogic private String delFlag;
}
