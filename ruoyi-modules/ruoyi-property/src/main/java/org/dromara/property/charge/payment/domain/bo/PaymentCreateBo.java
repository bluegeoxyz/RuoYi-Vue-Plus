package org.dromara.property.charge.payment.domain.bo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentCreateBo {
    @NotNull(message="所属项目不能为空") private Long projectId;
    @NotNull(message="房屋不能为空") private Long roomId;
    private Long payerCustomerId;
    @Size(max=128,message="付款人名称不能超过128个字符") private String payerName;
    @Size(max=32,message="付款人类型不能超过32个字符") private String payerType;
    @NotNull(message="收款金额不能为空") @DecimalMin(value="0.01",message="收款金额必须大于0") private BigDecimal paymentAmount;
    @NotBlank(message="收款方式不能为空") private String paymentMethod;
    @Size(max=128,message="交易流水号不能超过128个字符") private String transactionNo;
    @NotNull(message="收款时间不能为空") private LocalDateTime paymentTime;
    @Size(max=500,message="备注不能超过500个字符") private String remark;
}
