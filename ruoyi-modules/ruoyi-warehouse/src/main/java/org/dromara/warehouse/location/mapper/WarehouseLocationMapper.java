package org.dromara.warehouse.location.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationOptionVo;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationVo;

import java.util.Collection;
import java.util.List;

/** 库位数据层。 */
public interface WarehouseLocationMapper extends BaseMapperPlus<WarehouseLocation, WarehouseLocationVo> {
    default long countByCode(Long warehouseId, String code, Long excludeId) {
        return lambda().eq(WarehouseLocation::getWarehouseId, warehouseId).eq(WarehouseLocation::getLocationCode, code)
            .ne(excludeId != null, WarehouseLocation::getId, excludeId).count();
    }

    default boolean existsByZoneIds(Collection<Long> zoneIds) {
        return !zoneIds.isEmpty() && lambda().in(WarehouseLocation::getZoneId, zoneIds).exists();
    }

    default List<WarehouseLocationOptionVo> selectOptions(Long warehouseId, Long zoneId) {
        return selectVoList(lambda().eq(WarehouseLocation::getWarehouseId, warehouseId)
            .eq(zoneId != null, WarehouseLocation::getZoneId, zoneId).eq(WarehouseLocation::getStatus, "0")
            .orderByAsc(WarehouseLocation::getSort).orderByAsc(WarehouseLocation::getId).build(), WarehouseLocationOptionVo.class);
    }
}
