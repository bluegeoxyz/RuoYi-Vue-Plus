package org.dromara.warehouse.stocktake.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StocktakeOrderVo {
  private Long id;

  @ExcelProperty("盘点单号")
  private String stocktakeNo;

  private Long companyId;

  @ExcelProperty("公司")
  private String companyName;

  private Long projectId;
  private String projectName;
  private Long warehouseId;

  @ExcelProperty("仓库")
  private String warehouseName;

  @ExcelProperty("盘点类型")
  private String stocktakeType;

  private String scopeType;
  private Long zoneId;
  private String zoneName;
  private Long locationId;
  private String locationName;

  @ExcelProperty("状态")
  private String status;

  @ExcelProperty("盘点时间")
  private LocalDateTime stocktakeAt;

  private LocalDateTime confirmedAt;

  @ExcelProperty("完成时间")
  private LocalDateTime completedAt;

  private LocalDateTime cancelledAt;
  private Long operatorId;
  private String operatorName;
  private LocalDateTime createTime;
  private String remark;
  private List<StocktakeOrderItemVo> items;
}
