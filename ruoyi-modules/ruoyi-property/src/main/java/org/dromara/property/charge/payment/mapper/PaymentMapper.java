package org.dromara.property.charge.payment.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.charge.payment.domain.Payment;
import org.dromara.property.charge.payment.domain.vo.PaymentRoomOptionVo;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface PaymentMapper extends BaseMapperPlus<Payment, Payment> {
    @Select("SELECT * FROM pm_payment WHERE id=#{id} AND del_flag='0'")
    Payment selectActiveById(@Param("id") Long id);

    @Select("SELECT * FROM pm_payment WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    Payment selectActiveByIdForUpdate(@Param("id") Long id);

    @Update(
            "UPDATE pm_payment SET allocated_amount=#{amount},update_time=NOW() WHERE id=#{id} AND status='VALID' AND del_flag='0' AND #{amount}<=payment_amount")
    int updateAllocatedAmount(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount);

    @Update(
            "UPDATE pm_payment SET status='REVERSED',update_time=NOW() WHERE id=#{id} AND status='VALID' AND del_flag='0'")
    int markReversed(@Param("id") Long id);

    @Select(
            """
     SELECT room_id,
            MAX(room_full_name_snapshot) AS room_full_name,
            SUM(receivable_amount - paid_amount) AS outstanding_amount,
            COUNT(*) AS unpaid_count,
            MIN(due_date) AS earliest_due_date
     FROM pm_receivable
     WHERE project_id = #{projectId}
       AND payment_status IN ('UNPAID', 'PARTIAL')
       AND receivable_amount > paid_amount
       AND del_flag = '0'
     GROUP BY room_id
     ORDER BY room_full_name ASC, room_id ASC
     """)
    List<PaymentRoomOptionVo> selectCollectableRoomOptions(@Param("projectId") Long projectId);
}
