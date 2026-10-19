package org.dromara.property.charge.payment.domain.vo;

import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaymentVo {
  private Long id;
  private Long projectId;
  private String projectName;
  private String paymentNo;
  private Long roomId;
  private String roomFullNameSnapshot;
  private Long payerCustomerId;
  private String payerNameSnapshot;
  private String payerTypeSnapshot;
  private BigDecimal paymentAmount;
  private BigDecimal allocatedAmount;
  private String paymentMethod;
  private String transactionNo;
  private LocalDateTime paymentTime;
  private String status;
  private String remark;
  private Long createBy;

  @Translation(type = TransConstant.USER_ID_TO_NAME, mapper = "createBy")
  private String createByName;

  private LocalDateTime createTime;
  private List<PaymentAllocationVo> allocations = List.of();
  private PaymentReversalVo reversal;
}
