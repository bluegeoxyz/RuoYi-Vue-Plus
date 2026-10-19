package org.dromara.property.project.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.domain.bo.ProjectMemberBo;
import org.dromara.property.project.domain.vo.ProjectMemberExportVo;
import org.dromara.property.project.domain.vo.ProjectMemberOptionVo;
import org.dromara.property.project.domain.vo.ProjectMemberVo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 物业项目成员关系服务。
 */
public interface IProjectMemberService {
    ProjectMemberVo queryById(Long id);

    PageResult<ProjectMemberVo> queryPageList(ProjectMemberBo bo, PageQuery pageQuery);

    List<ProjectMemberVo> queryList(ProjectMemberBo bo);

    List<ProjectMemberExportVo> queryExportList(ProjectMemberBo bo);

    Boolean addMember(ProjectMemberBo bo);

    Boolean updateMember(ProjectMemberBo bo);

    Boolean endMember(Long id, LocalDate endDate);

    Boolean setManager(Long id);

    Boolean cancelManager(Long id);

    Boolean deleteWrongMembers(Collection<Long> ids);

    List<ProjectMemberOptionVo> selectOptions(Long projectId);
}
