package org.dromara.property.project.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.domain.bo.ProjectBo;
import org.dromara.property.project.domain.vo.ProjectExportVo;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.project.domain.vo.ProjectVo;

import java.util.Collection;
import java.util.List;

/**
 * 物业项目服务。
 */
public interface IProjectService {
    ProjectVo queryById(Long id);

    PageResult<ProjectVo> queryPageList(ProjectBo bo, PageQuery pageQuery);

    List<ProjectVo> queryList(ProjectBo bo);

    List<ProjectExportVo> queryExportList(ProjectBo bo);

    Boolean insertByBo(ProjectBo bo);

    Boolean updateByBo(ProjectBo bo);

    Boolean changeStatus(Long id, String status);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    List<ProjectOptionVo> selectOptions();

    List<ProjectOptionVo> selectBusinessOptions();
}
