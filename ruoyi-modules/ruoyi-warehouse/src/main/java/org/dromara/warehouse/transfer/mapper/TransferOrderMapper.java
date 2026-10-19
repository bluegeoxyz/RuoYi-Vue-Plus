package org.dromara.warehouse.transfer.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.transfer.domain.TransferOrder;
import org.dromara.warehouse.transfer.domain.bo.TransferOrderBo;
import org.dromara.warehouse.transfer.domain.vo.TransferOrderVo;
import java.time.LocalDateTime;
import java.util.*;

/** 调拨单数据层。 */
public interface TransferOrderMapper extends BaseMapperPlus<TransferOrder, TransferOrderVo> {
    IPage<TransferOrderVo> selectTransferPage(
            Page<TransferOrderVo> page,
            @Param("bo") TransferOrderBo bo,
            @Param("warehouseIds") Collection<Long> ids);

    List<TransferOrderVo> selectTransferList(
            @Param("bo") TransferOrderBo bo, @Param("warehouseIds") Collection<Long> ids);

    TransferOrderVo selectTransferById(@Param("id") Long id);

    @Select("SELECT * FROM wm_transfer_order WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    TransferOrder selectForUpdate(@Param("id") Long id);

    @Update(
            "UPDATE wm_transfer_order SET status=#{toStatus},confirmed_at=COALESCE(#{confirmedAt},confirmed_at),completed_at=COALESCE(#{completedAt},completed_at),operator_id=#{operatorId},update_time=NOW() WHERE id=#{id} AND status=#{fromStatus} AND del_flag='0'")
    int updateStatusCas(
            @Param("id") Long id,
            @Param("fromStatus") String from,
            @Param("toStatus") String to,
            @Param("confirmedAt") LocalDateTime confirmed,
            @Param("completedAt") LocalDateTime completed,
            @Param("operatorId") Long operator);

    @Update(
            "UPDATE wm_transfer_order SET status='CANCELLED',cancelled_at=NOW(),update_time=NOW() WHERE id=#{id} AND status IN ('DRAFT','CONFIRMED') AND del_flag='0'")
    int cancelCas(@Param("id") Long id);
}
