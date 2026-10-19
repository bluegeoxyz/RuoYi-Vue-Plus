package org.dromara.property.charge.payment.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ExcelIgnoreUnannotated
public class PaymentExportVo {
    @ExcelProperty("收款编号") private String paymentNo;
    @ExcelProperty("所属项目") private String projectName;
    @ExcelProperty("房屋") private String roomFullNameSnapshot;
    @ExcelProperty("付款人") private String payerNameSnapshot;
    @ExcelProperty("收款金额") private BigDecimal paymentAmount;
    @ExcelProperty("已核销金额") private BigDecimal allocatedAmount;
    @ExcelProperty(value = "收款方式", converter = ExcelDictConvert.class) @ExcelDictFormat(dictType = "pm_payment_method") private String paymentMethod;
    @ExcelProperty("交易流水号") private String transactionNo;
    @ExcelProperty("收款时间") private LocalDateTime paymentTime;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class) @ExcelDictFormat(dictType = "pm_payment_record_status") private String status;
    @ExcelProperty("操作人") private String createByName;
    @ExcelProperty("备注") private String remark;
}
