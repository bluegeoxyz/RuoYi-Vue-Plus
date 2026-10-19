package org.dromara.warehouse.zone.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneOptionVo;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneVo;

import java.util.Collection;
import java.util.List;

/** 库区数据层。 */
public interface WarehouseZoneMapper extends BaseMapperPlus<WarehouseZone, WarehouseZoneVo> {
    default long countByCode(Long warehouseId, String code, Long excludeId) {
        return lambda().eq(WarehouseZone::getWarehouseId, warehouseId).eq(WarehouseZone::getZoneCode, code)
            .ne(excludeId != null, WarehouseZone::getId, excludeId).count();
    }

    default boolean existsByWarehouseIds(Collection<Long> warehouseIds) {
        return !warehouseIds.isEmpty() && lambda().in(WarehouseZone::getWarehouseId, warehouseIds).exists();
    }

    default List<WarehouseZoneOptionVo> selectOptions(Long warehouseId) {
        return selectVoList(lambda().eq(WarehouseZone::getWarehouseId, warehouseId).eq(WarehouseZone::getStatus, "0")
            .orderByAsc(WarehouseZone::getSort).orderByAsc(WarehouseZone::getId).build(), WarehouseZoneOptionVo.class);
    }
}
