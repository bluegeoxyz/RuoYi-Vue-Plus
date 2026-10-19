package org.dromara.warehouse.stocktake.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.warehouse.stocktake.domain.StocktakeOrderItem;
import java.time.LocalDateTime;
import java.util.*;

@Data
public class StocktakeOrderBo {
  private Long id;
  private String stocktakeNo;

  @NotNull(message = "仓库不能为空")
  private Long warehouseId;

  @NotBlank(message = "盘点类型不能为空")
  private String stocktakeType;

  @NotBlank(message = "盘点范围不能为空")
  private String scopeType;

  private Long zoneId;
  private Long locationId;
  private String status;
  private LocalDateTime stocktakeAt;
  private LocalDateTime beginStocktakeAt;
  private LocalDateTime endStocktakeAt;
  private String remark;
  @Valid private List<StocktakeOrderItem> items = new ArrayList<>();
}
