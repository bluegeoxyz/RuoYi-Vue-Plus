package org.dromara.property.charge.receivable.domain.bo;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class ReceivableCancelBo { @NotBlank(message="作废原因不能为空") @Size(min=2,max=500,message="作废原因长度必须在2到500字符之间") private String cancelReason; }
