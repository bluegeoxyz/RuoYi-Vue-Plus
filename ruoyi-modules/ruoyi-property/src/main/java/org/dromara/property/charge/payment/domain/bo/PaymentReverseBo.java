package org.dromara.property.charge.payment.domain.bo;

import jakarta.validation.constraints.*;
import lombok.Data;

/** 整笔收款冲销请求。 */
@Data
public class PaymentReverseBo {
    @NotBlank(message="冲销原因不能为空")
    @Size(min=2,max=500,message="冲销原因长度必须在2到500字符之间")
    private String reason;
}
