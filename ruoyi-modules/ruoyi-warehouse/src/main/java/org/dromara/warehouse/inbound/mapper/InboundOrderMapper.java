package org.dromara.warehouse.inbound.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.inbound.domain.InboundOrder;
import org.dromara.warehouse.inbound.domain.bo.InboundOrderBo;
import org.dromara.warehouse.inbound.domain.vo.InboundOrderVo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** 入库单数据层。 */
public interface InboundOrderMapper extends BaseMapperPlus<InboundOrder, InboundOrderVo> {
    IPage<InboundOrderVo> selectInboundPage(
            Page<InboundOrderVo> page,
            @Param("bo") InboundOrderBo bo,
            @Param("warehouseIds") Collection<Long> warehouseIds);

    List<InboundOrderVo> selectInboundList(
            @Param("bo") InboundOrderBo bo, @Param("warehouseIds") Collection<Long> warehouseIds);

    InboundOrderVo selectInboundById(@Param("id") Long id);

    @Select("SELECT * FROM wm_inbound_order WHERE id = #{id} AND del_flag = '0' FOR UPDATE")
    InboundOrder selectForUpdate(@Param("id") Long id);

    @Update(
            "UPDATE wm_inbound_order SET status = #{toStatus}, completed_at = #{completedAt}, operator_id = #{operatorId}, update_time = NOW() WHERE id = #{id} AND status = #{fromStatus} AND del_flag = '0'")
    int updateStatusCas(
            @Param("id") Long id,
            @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus,
            @Param("completedAt") LocalDateTime completedAt,
            @Param("operatorId") Long operatorId);

    @Update(
            "UPDATE wm_inbound_order SET status = 'CANCELLED', update_time = NOW() WHERE id = #{id} AND status IN ('DRAFT','CONFIRMED') AND del_flag = '0'")
    int cancelCas(@Param("id") Long id);
}
