package org.dromara.property.project.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.project.domain.vo.ProjectVo;

import java.util.List;
import java.util.Set;

/**
 * 物业项目数据层。
 */
public interface ProjectMapper extends BaseMapperPlus<Project, ProjectVo> {
    @org.apache.ibatis.annotations.Select("SELECT * FROM pm_project WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    Project selectActiveByIdForUpdate(@org.apache.ibatis.annotations.Param("id") Long id);

    default Project selectByIdForUpdate(Long id) {
        return lambda().eq(Project::getId, id).last("FOR UPDATE").one();
    }

    default int updateManagerEmployeeId(Long projectId, Long employeeId) {
        return lambda()
            .set(Project::getManagerEmployeeId, employeeId)
            .eq(Project::getId, projectId)
            .updateCount();
    }

    default long countByProjectCode(String projectCode, Long excludeId) {
        return lambda()
            .eq(Project::getProjectCode, projectCode)
            .ne(excludeId != null, Project::getId, excludeId)
            .count();
    }

    default List<ProjectOptionVo> selectOptions() {
        return selectVoList(QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getProjectShortName)
            .eq(Project::getStatus, "0")
            .orderByAsc(Project::getSort)
            .orderByAsc(Project::getId)
            .build(), ProjectOptionVo.class);
    }

    /** 工作项目最小字段查询；projectIds 为 null 表示全量有效项目。 */
    default List<Project> selectWorkspaceProjects(Set<Long> projectIds) {
        return selectList(QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getProjectShortName)
            .in(projectIds != null, Project::getId, projectIds)
            .eq(Project::getStatus, "0")
            .orderByAsc(Project::getSort)
            .orderByAsc(Project::getId)
            .build());
    }
}
