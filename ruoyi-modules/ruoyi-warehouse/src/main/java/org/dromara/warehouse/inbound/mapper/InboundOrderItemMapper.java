package org.dromara.warehouse.inbound.mapper;

import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.inbound.domain.InboundOrderItem;
import org.dromara.warehouse.inbound.domain.vo.InboundOrderItemVo;

import java.util.List;

/** 入库明细数据层。 */
public interface InboundOrderItemMapper extends BaseMapperPlus<InboundOrderItem, InboundOrderItemVo> {
    List<InboundOrderItemVo> selectItems(@Param("orderId") Long orderId);
}
