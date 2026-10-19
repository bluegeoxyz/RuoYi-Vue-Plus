package org.dromara.warehouse.transfer.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.io.Serial;
import java.time.LocalDateTime;

/** 调拨单实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_transfer_order")
public class TransferOrder extends BaseEntity {
  @Serial private static final long serialVersionUID = 1L;
  @TableId private Long id;
  private String transferNo;
  private Long sourceWarehouseId;
  private Long targetWarehouseId;
  private Long sourceCompanyId;
  private Long sourceProjectId;
  private Long targetCompanyId;
  private Long targetProjectId;
  private String transferType;
  private String status;
  private LocalDateTime transferAt;
  private LocalDateTime confirmedAt;
  private LocalDateTime completedAt;
  private LocalDateTime cancelledAt;
  private Long operatorId;
  private String remark;
  @TableLogic private String delFlag;
}
