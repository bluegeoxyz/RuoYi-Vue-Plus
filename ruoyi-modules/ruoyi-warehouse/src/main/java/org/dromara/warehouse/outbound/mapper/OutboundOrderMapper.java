package org.dromara.warehouse.outbound.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.outbound.domain.OutboundOrder;
import org.dromara.warehouse.outbound.domain.bo.OutboundOrderBo;
import org.dromara.warehouse.outbound.domain.vo.OutboundOrderVo;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** 出库单数据层。 */
public interface OutboundOrderMapper extends BaseMapperPlus<OutboundOrder, OutboundOrderVo> {
    IPage<OutboundOrderVo> selectOutboundPage(
            Page<OutboundOrderVo> page,
            @Param("bo") OutboundOrderBo bo,
            @Param("warehouseIds") Collection<Long> warehouseIds);

    List<OutboundOrderVo> selectOutboundList(
            @Param("bo") OutboundOrderBo bo, @Param("warehouseIds") Collection<Long> warehouseIds);

    OutboundOrderVo selectOutboundById(@Param("id") Long id);

    @Select("SELECT * FROM wm_outbound_order WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    OutboundOrder selectForUpdate(@Param("id") Long id);

    @Update(
            "UPDATE wm_outbound_order SET status=#{toStatus}, confirmed_at=COALESCE(#{confirmedAt},confirmed_at), completed_at=COALESCE(#{completedAt},completed_at), operator_id=#{operatorId}, update_time=NOW() WHERE id=#{id} AND status=#{fromStatus} AND del_flag='0'")
    int updateStatusCas(
            @Param("id") Long id,
            @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus,
            @Param("confirmedAt") LocalDateTime confirmedAt,
            @Param("completedAt") LocalDateTime completedAt,
            @Param("operatorId") Long operatorId);

    @Update(
            "UPDATE wm_outbound_order SET status='CANCELLED',cancelled_at=NOW(),update_time=NOW() WHERE id=#{id} AND status IN ('DRAFT','CONFIRMED') AND del_flag='0'")
    int cancelCas(@Param("id") Long id);
}
