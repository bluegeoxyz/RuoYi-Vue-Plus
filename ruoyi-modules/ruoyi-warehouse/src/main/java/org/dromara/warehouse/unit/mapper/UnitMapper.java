package org.dromara.warehouse.unit.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.unit.domain.Unit;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.dromara.warehouse.unit.domain.vo.UnitVo;

import java.util.List;

/** 计量单位数据层。 */
public interface UnitMapper extends BaseMapperPlus<Unit, UnitVo> {
    default long countByCode(String code, Long excludeId) {
        return lambda().eq(Unit::getUnitCode, code).ne(excludeId != null, Unit::getId, excludeId).count();
    }

    default List<UnitOptionVo> selectOptions() {
        return selectVoList(lambda().eq(Unit::getStatus, "0").orderByAsc(Unit::getSort)
            .orderByAsc(Unit::getId).build(), UnitOptionVo.class);
    }
}
