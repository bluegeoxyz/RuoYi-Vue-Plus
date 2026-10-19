package org.dromara.warehouse.outbound.mapper;

import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.outbound.domain.OutboundOrderItem;
import org.dromara.warehouse.outbound.domain.vo.OutboundOrderItemVo;
import java.util.List;

/** 出库明细数据层。 */
public interface OutboundOrderItemMapper extends BaseMapperPlus<OutboundOrderItem,OutboundOrderItemVo> {
    List<OutboundOrderItemVo> selectItems(@Param("orderId")Long orderId);
}
