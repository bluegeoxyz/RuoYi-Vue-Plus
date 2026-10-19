package org.dromara.warehouse.transfer.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import java.time.LocalDateTime;
import java.util.List;

/** 调拨单展示对象。 */
@Data
public class TransferOrderVo {
  private Long id;

  @ExcelProperty("调拨单号")
  private String transferNo;

  private Long sourceWarehouseId;

  @ExcelProperty("源仓库")
  private String sourceWarehouseName;

  private Long targetWarehouseId;

  @ExcelProperty("目标仓库")
  private String targetWarehouseName;

  private Long sourceCompanyId;

  @ExcelProperty("源公司")
  private String sourceCompanyName;

  private Long sourceProjectId;
  private String sourceProjectName;
  private Long targetCompanyId;

  @ExcelProperty("目标公司")
  private String targetCompanyName;

  private Long targetProjectId;
  private String targetProjectName;

  @ExcelProperty("调拨类型")
  private String transferType;

  @ExcelProperty("状态")
  private String status;

  @ExcelProperty("调拨时间")
  private LocalDateTime transferAt;

  private LocalDateTime confirmedAt;

  @ExcelProperty("完成时间")
  private LocalDateTime completedAt;

  private LocalDateTime cancelledAt;
  private Long operatorId;

  @ExcelProperty("操作人")
  private String operatorName;

  @ExcelProperty("创建时间")
  private LocalDateTime createTime;

  private String remark;
  private List<TransferOrderItemVo> items;
}
