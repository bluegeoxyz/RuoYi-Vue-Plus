package org.dromara.property.charge.payment.mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.charge.payment.domain.PaymentAllocation;
import org.apache.ibatis.annotations.*;
import java.util.List;
public interface PaymentAllocationMapper extends BaseMapperPlus<PaymentAllocation,PaymentAllocation> {
 @Select("SELECT * FROM pm_payment_allocation WHERE payment_id=#{paymentId} AND del_flag='0' ORDER BY receivable_id ASC,id ASC FOR UPDATE")
 List<PaymentAllocation> selectByPaymentIdForUpdate(@Param("paymentId")Long paymentId);
}
