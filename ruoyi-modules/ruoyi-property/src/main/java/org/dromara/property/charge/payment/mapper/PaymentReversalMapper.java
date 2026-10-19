package org.dromara.property.charge.payment.mapper;

import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.charge.payment.domain.PaymentReversal;

/** 收款冲销审计记录 Mapper。 */
public interface PaymentReversalMapper extends BaseMapperPlus<PaymentReversal,PaymentReversal> {
    @Select("SELECT * FROM pm_payment_reversal WHERE payment_id=#{paymentId} AND del_flag='0'")
    PaymentReversal selectByPaymentId(@Param("paymentId") Long paymentId);
}
