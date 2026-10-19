package org.dromara.property.charge.receivable.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.*;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ExcelIgnoreUnannotated
public class ReceivableExportVo {
  @ExcelProperty("应收编号")
  private String receivableNo;

  @ExcelProperty("所属项目")
  private String projectName;

  @ExcelProperty("房屋")
  private String roomFullNameSnapshot;

  @ExcelProperty("责任主体")
  private String responsibleCustomerNameSnapshot;

  @ExcelProperty("收费项目")
  private String chargeItemNameSnapshot;

  @ExcelProperty("计费规则")
  private String chargeRuleNameSnapshot;

  @ExcelProperty("周期开始")
  private LocalDate periodStartDate;

  @ExcelProperty("周期结束")
  private LocalDate periodEndDate;

  @ExcelProperty("应缴日期")
  private LocalDate dueDate;

  @ExcelProperty("应收金额")
  private BigDecimal receivableAmount;

  @ExcelProperty("已收金额")
  private BigDecimal paidAmount;

  @ExcelProperty("未收金额")
  private BigDecimal outstandingAmount;

  @ExcelProperty(value = "支付状态", converter = ExcelDictConvert.class)
  @ExcelDictFormat(dictType = "pm_payment_status")
  private String paymentStatus;

  @ExcelProperty("是否逾期")
  private String overdueLabel;

  @ExcelProperty("生成月份")
  private LocalDate generateMonth;

  @ExcelProperty("批次号")
  private String batchNo;
}
