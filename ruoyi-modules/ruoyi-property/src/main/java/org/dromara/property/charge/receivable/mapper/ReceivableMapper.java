package org.dromara.property.charge.receivable.mapper;

import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.charge.receivable.domain.Receivable;

public interface ReceivableMapper extends BaseMapperPlus<Receivable, Receivable> {
    @Select(
            "SELECT EXISTS(SELECT 1 FROM pm_receivable WHERE binding_id=#{bindingId} AND period_start_date=#{start} AND period_end_date=#{end} AND payment_status<>'CANCELLED' AND del_flag='0')")
    boolean existsActivePeriod(
            @Param("bindingId") Long bindingId,
            @Param("start") java.time.LocalDate start,
            @Param("end") java.time.LocalDate end);

    @Select("SELECT * FROM pm_receivable WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    Receivable selectActiveByIdForUpdate(@Param("id") Long id);

    @Select(
            "SELECT * FROM pm_receivable WHERE project_id=#{projectId} AND room_id=#{roomId} AND payment_status IN ('UNPAID','PARTIAL','PAID') AND del_flag='0' ORDER BY id ASC FOR UPDATE")
    java.util.List<Receivable> selectPayableByRoomForUpdate(
            @Param("projectId") Long projectId, @Param("roomId") Long roomId);

    @Select(
            "<script>SELECT * FROM pm_receivable WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> AND del_flag='0' ORDER BY id ASC FOR UPDATE</script>")
    java.util.List<Receivable> selectByIdsForUpdate(@Param("ids") java.util.Collection<Long> ids);

    @Update(
            "UPDATE pm_receivable SET paid_amount=#{paidAmount},payment_status=#{paymentStatus},update_time=NOW() WHERE id=#{id} AND del_flag='0'")
    int updatePaymentState(
            @Param("id") Long id,
            @Param("paidAmount") java.math.BigDecimal paidAmount,
            @Param("paymentStatus") String paymentStatus);
}
