package org.dromara.warehouse.unit.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.unit.domain.bo.UnitBo;
import org.dromara.warehouse.unit.domain.Unit;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.dromara.warehouse.unit.domain.vo.UnitVo;

import java.util.Collection;
import java.util.List;

/** 计量单位服务。 */
public interface IUnitService {
    UnitVo queryById(Long id);
    PageResult<UnitVo> queryPageList(UnitBo bo, PageQuery pageQuery);
    Boolean insertByBo(UnitBo bo);
    Boolean updateByBo(UnitBo bo);
    Boolean changeStatus(Long id, String status);
    Boolean deleteWithValidByIds(Collection<Long> ids);
    List<UnitOptionVo> selectOptions();
    Unit requireEnabledUnit(Long id);
}
