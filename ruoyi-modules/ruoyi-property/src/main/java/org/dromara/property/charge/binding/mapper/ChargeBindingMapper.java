package org.dromara.property.charge.binding.mapper;

import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.charge.binding.domain.ChargeBinding;
import org.dromara.property.charge.binding.domain.vo.ChargeBindingVo;
import java.time.LocalDate;
import java.util.List;

public interface ChargeBindingMapper extends BaseMapperPlus<ChargeBinding, ChargeBindingVo> {
  @Select("SELECT * FROM pm_charge_binding WHERE id=#{id} AND del_flag='0' FOR UPDATE")
  ChargeBinding selectActiveByIdForUpdate(@Param("id") Long id);

  @Select(
      "SELECT * FROM pm_charge_binding WHERE project_id=#{projectId} AND object_type='ROOM' AND object_id=#{objectId} AND charge_item_id=#{itemId} AND del_flag='0' FOR UPDATE")
  List<ChargeBinding> lockItemBindings(
      @Param("projectId") Long projectId,
      @Param("objectId") Long objectId,
      @Param("itemId") Long itemId);

  @Select("SELECT EXISTS(SELECT 1 FROM pm_receivable WHERE binding_id=#{bindingId})")
  boolean existsReceivable(@Param("bindingId") Long bindingId);

  default boolean overlaps(
      Long projectId, Long objectId, Long itemId, LocalDate start, LocalDate end, Long excludeId) {
    return lambda()
        .eq(ChargeBinding::getProjectId, projectId)
        .eq(ChargeBinding::getObjectType, "ROOM")
        .eq(ChargeBinding::getObjectId, objectId)
        .eq(ChargeBinding::getChargeItemId, itemId)
        .ne(excludeId != null, ChargeBinding::getId, excludeId)
        .le(end != null, ChargeBinding::getEffectiveStartDate, end)
        .and(
            w ->
                w.isNull(ChargeBinding::getEffectiveEndDate)
                    .or()
                    .ge(ChargeBinding::getEffectiveEndDate, start))
        .exists();
  }
}
