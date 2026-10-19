package org.dromara.property.api;

import java.util.Collection;
import java.util.List;

/**
 * 项目参考数据与业务范围的跨模块读取边界。
 */
public interface ProjectReferenceService {

    ProjectAccessScope resolveWarehouseScope();

    ProjectReferenceDTO queryById(Long projectId);

    List<ProjectReferenceDTO> queryByIds(Collection<Long> projectIds);

    List<ProjectReferenceDTO> selectWarehouseOptions();
}
