package org.dromara.warehouse.inbound.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/** 入库单主表实体。 */
@Data @EqualsAndHashCode(callSuper = true) @TableName("wm_inbound_order")
public class InboundOrder extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private String inboundNo;
    private Long companyId;
    private Long projectId;
    private Long warehouseId;
    private String inboundType;
    private String status;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private LocalDateTime inboundAt;
    private LocalDateTime completedAt;
    private Long operatorId;
    private String remark;
    @TableLogic private String delFlag;
}
