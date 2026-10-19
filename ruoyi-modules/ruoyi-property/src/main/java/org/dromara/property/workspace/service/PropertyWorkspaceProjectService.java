package org.dromara.property.workspace.service;

import org.dromara.property.workspace.domain.vo.WorkspaceProjectOptionVo;

import java.util.List;

/** 当前登录用户的工作项目查询边界。 */
public interface PropertyWorkspaceProjectService {

    List<WorkspaceProjectOptionVo> selectAvailableProjects();
}
