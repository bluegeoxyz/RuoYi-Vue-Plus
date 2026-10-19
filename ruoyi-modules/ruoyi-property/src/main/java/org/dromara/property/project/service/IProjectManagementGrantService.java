package org.dromara.property.project.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.domain.bo.ProjectManagementGrantBo;
import org.dromara.property.project.domain.vo.ProjectManagementGrantVo;

import java.util.List;

public interface IProjectManagementGrantService {
    PageResult<ProjectManagementGrantVo> queryPage(ProjectManagementGrantBo bo, PageQuery pageQuery);

    List<ProjectManagementGrantVo> queryHistory(ProjectManagementGrantBo bo);

    boolean grant(ProjectManagementGrantBo bo);

    boolean changeStatus(Long id, Integer version, String status);

    boolean revoke(Long id, Integer version);
}
