package org.dromara.warehouse.stocktake.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.io.Serial;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_stocktake_order_item")
public class StocktakeOrderItem extends BaseEntity {
  @Serial private static final long serialVersionUID = 1L;
  @TableId private Long id;
  private Long stocktakeOrderId;
  private Long materialId;
  private Long locationId;
  private Long unitId;
  private BigDecimal bookQuantity;
  private BigDecimal actualQuantity;
  private BigDecimal differenceQuantity;
  private String remark;
}
