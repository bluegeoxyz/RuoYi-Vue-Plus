package org.dromara.property.resource.unit.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.resource.unit.domain.bo.BuildingUnitBo;
import org.dromara.property.resource.unit.domain.vo.BuildingUnitVo;

import java.util.Collection;
import java.util.List;

/**
 * 楼栋单元服务。
 */
public interface IBuildingUnitService {
    BuildingUnitVo queryById(Long id);

    PageResult<BuildingUnitVo> queryPage(BuildingUnitBo bo, PageQuery pageQuery);

    List<BuildingUnitVo> queryExport(BuildingUnitBo bo);

    boolean insert(BuildingUnitBo bo);

    boolean update(BuildingUnitBo bo);

    boolean changeStatus(Long id, String status);

    boolean delete(Collection<Long> ids);

    List<BuildingUnitVo> options(BuildingUnitBo bo);
}
