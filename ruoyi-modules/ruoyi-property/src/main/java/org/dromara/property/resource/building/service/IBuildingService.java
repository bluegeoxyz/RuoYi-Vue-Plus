package org.dromara.property.resource.building.service;

import java.util.Collection;

import org.dromara.property.resource.building.domain.bo.BuildingBo;
import org.dromara.property.resource.building.domain.vo.BuildingVo;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

public interface IBuildingService {
    BuildingVo queryById(Long id);

    PageResult<BuildingVo> queryPage(BuildingBo bo, PageQuery pageQuery);

    java.util.List<BuildingVo> queryExport(BuildingBo bo);

    boolean insert(BuildingBo bo);

    boolean update(BuildingBo bo);

    boolean changeStatus(Long id, String status);

    boolean delete(Collection<Long> ids);

    java.util.List<BuildingVo> options(BuildingBo bo);

    java.util.List<ProjectOptionVo> projectOptions();
}
