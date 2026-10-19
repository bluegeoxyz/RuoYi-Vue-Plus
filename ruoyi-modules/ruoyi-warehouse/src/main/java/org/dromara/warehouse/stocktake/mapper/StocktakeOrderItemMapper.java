package org.dromara.warehouse.stocktake.mapper;

import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.stocktake.domain.StocktakeOrderItem;
import org.dromara.warehouse.stocktake.domain.vo.StocktakeOrderItemVo;
import java.util.List;

public interface StocktakeOrderItemMapper
    extends BaseMapperPlus<StocktakeOrderItem, StocktakeOrderItemVo> {
  List<StocktakeOrderItemVo> selectItems(@Param("orderId") Long id);

  default List<StocktakeOrderItem> selectByOrderId(Long id) {
    return lambda().eq(StocktakeOrderItem::getStocktakeOrderId, id).list();
  }
}
