package org.dromara.warehouse.transfer.mapper;

import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.transfer.domain.TransferOrderItem;
import org.dromara.warehouse.transfer.domain.vo.TransferOrderItemVo;
import java.util.List;

/** 调拨明细数据层。 */
public interface TransferOrderItemMapper
        extends BaseMapperPlus<TransferOrderItem, TransferOrderItemVo> {
    List<TransferOrderItemVo> selectItems(@Param("orderId") Long orderId);

    default List<TransferOrderItem> selectByOrderId(Long orderId) {
        return lambda().eq(TransferOrderItem::getTransferOrderId, orderId).list();
    }
}
