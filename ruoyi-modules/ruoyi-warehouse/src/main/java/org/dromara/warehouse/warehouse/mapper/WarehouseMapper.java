package org.dromara.warehouse.warehouse.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseOptionVo;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseVo;

import java.util.List;

/** 仓库数据层。 */
public interface WarehouseMapper extends BaseMapperPlus<Warehouse, WarehouseVo> {
    default long countByCode(Long companyId, String code, Long excludeId) {
        return lambda().eq(Warehouse::getCompanyId, companyId).eq(Warehouse::getWarehouseCode, code)
            .ne(excludeId != null, Warehouse::getId, excludeId).count();
    }

    default List<WarehouseOptionVo> selectOptions(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return selectVoList(lambda().eq(Warehouse::getStatus, "0").in(Warehouse::getId, ids)
            .orderByAsc(Warehouse::getSort).orderByAsc(Warehouse::getId).build(), WarehouseOptionVo.class);
    }
}
