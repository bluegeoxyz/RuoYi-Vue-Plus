package org.dromara.warehouse.stocktake.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.stocktake.domain.StocktakeOrder;
import org.dromara.warehouse.stocktake.domain.bo.StocktakeOrderBo;
import org.dromara.warehouse.stocktake.domain.vo.StocktakeOrderVo;
import java.time.LocalDateTime;
import java.util.*;

public interface StocktakeOrderMapper extends BaseMapperPlus<StocktakeOrder, StocktakeOrderVo> {
    IPage<StocktakeOrderVo> selectStocktakePage(
            Page<StocktakeOrderVo> page,
            @Param("bo") StocktakeOrderBo bo,
            @Param("warehouseIds") Collection<Long> ids);

    List<StocktakeOrderVo> selectStocktakeList(
            @Param("bo") StocktakeOrderBo bo, @Param("warehouseIds") Collection<Long> ids);

    StocktakeOrderVo selectStocktakeById(@Param("id") Long id);

    @Select("SELECT * FROM wm_stocktake_order WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    StocktakeOrder selectForUpdate(@Param("id") Long id);

    @Update(
            "UPDATE wm_stocktake_order SET status=#{toStatus},confirmed_at=COALESCE(#{confirmedAt},confirmed_at),completed_at=COALESCE(#{completedAt},completed_at),operator_id=#{operatorId},update_time=NOW() WHERE id=#{id} AND status=#{fromStatus} AND del_flag='0'")
    int updateStatusCas(
            @Param("id") Long id,
            @Param("fromStatus") String from,
            @Param("toStatus") String to,
            @Param("confirmedAt") LocalDateTime confirmed,
            @Param("completedAt") LocalDateTime completed,
            @Param("operatorId") Long operator);

    @Update(
            "UPDATE wm_stocktake_order SET status='CANCELLED',cancelled_at=NOW(),update_time=NOW() WHERE id=#{id} AND status IN ('DRAFT','CONFIRMED') AND del_flag='0'")
    int cancelCas(@Param("id") Long id);
}
