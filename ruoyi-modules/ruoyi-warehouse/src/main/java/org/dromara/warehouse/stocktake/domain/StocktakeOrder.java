package org.dromara.warehouse.stocktake.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_stocktake_order")
public class StocktakeOrder extends BaseEntity {
  @Serial private static final long serialVersionUID = 1L;
  @TableId private Long id;
  private String stocktakeNo;
  private Long companyId;
  private Long projectId;
  private Long warehouseId;
  private String stocktakeType;
  private String scopeType;
  private Long zoneId;
  private Long locationId;
  private String status;
  private LocalDateTime stocktakeAt;
  private LocalDateTime confirmedAt;
  private LocalDateTime completedAt;
  private LocalDateTime cancelledAt;
  private Long operatorId;
  private String remark;
  @TableLogic private String delFlag;
}
