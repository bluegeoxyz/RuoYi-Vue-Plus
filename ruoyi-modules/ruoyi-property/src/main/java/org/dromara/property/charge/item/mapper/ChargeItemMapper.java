package org.dromara.property.charge.item.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.charge.item.domain.ChargeItem;
import org.dromara.property.charge.item.domain.vo.ChargeItemVo;

/** 收费项目数据层。 */
public interface ChargeItemMapper extends BaseMapperPlus<ChargeItem, ChargeItemVo> {
    @Select("SELECT * FROM pm_charge_item WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    ChargeItem selectActiveByIdForUpdate(@Param("id") Long id);

    default long countActiveCode(Long projectId, String itemCode, Long excludeId) {
        return lambda().eq(ChargeItem::getProjectId, projectId).eq(ChargeItem::getItemCode, itemCode)
            .ne(excludeId != null, ChargeItem::getId, excludeId).count();
    }

    @Select("SELECT EXISTS(SELECT 1 FROM pm_charge_rule WHERE charge_item_id=#{itemId} AND del_flag='0')")
    boolean existsActiveChargeRule(@Param("itemId") Long itemId);

    @Select("SELECT EXISTS(SELECT 1 FROM pm_charge_binding WHERE charge_item_id=#{itemId} AND del_flag='0')")
    boolean existsActiveChargeBinding(@Param("itemId") Long itemId);
}
