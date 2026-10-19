package org.dromara.property.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.hr.employee.domain.vo.EmployeeOptionVo;
import org.dromara.hr.employee.domain.vo.EmployeeVo;
import org.dromara.hr.employee.service.IEmployeeService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.domain.ProjectMember;
import org.dromara.property.project.domain.bo.ProjectMemberBo;
import org.dromara.property.project.domain.vo.ProjectMemberExportVo;
import org.dromara.property.project.domain.vo.ProjectMemberOptionVo;
import org.dromara.property.project.domain.vo.ProjectMemberVo;
import org.dromara.property.project.domain.vo.ProjectVo;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.project.mapper.ProjectMemberMapper;
import org.dromara.property.project.service.IProjectMemberService;
import org.dromara.property.scope.ProjectManagementAction;
import org.dromara.property.scope.PropertyProjectScope;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物业项目成员关系服务实现。
 */
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements IProjectMemberService {

    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";
    private static final String YES = "1";
    private static final String NO = "0";
    private static final String DICT_MEMBER_ROLE = "pm_member_role";
    private static final String DICT_MEMBER_STATUS = "pm_member_status";
    private static final String DICT_NORMAL_DISABLE = "sys_normal_disable";

    private final ProjectMemberMapper memberMapper;
    private final ProjectMapper projectMapper;
    private final IEmployeeService employeeService;
    private final DictService dictService;
    private final PropertyProjectScopeService scopeService;

    @Override
    public ProjectMemberVo queryById(Long id) {
        ProjectMemberVo member = memberMapper.selectVoById(id);
        if (member == null) {
            throw new ServiceException("项目成员关系不存在");
        }
        scopeService.assertManagementAccess(member.getProjectId(), ProjectManagementAction.READ_MEMBER);
        enrich(List.of(member));
        return member;
    }

    @Override
    public PageResult<ProjectMemberVo> queryPageList(ProjectMemberBo bo, PageQuery pageQuery) {
        assertManagementList(ProjectManagementAction.READ_MEMBER);
        Page<ProjectMemberVo> page = memberMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        enrich(page.getRecords());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<ProjectMemberVo> queryList(ProjectMemberBo bo) {
        assertManagementList(ProjectManagementAction.READ_MEMBER);
        List<ProjectMemberVo> members = memberMapper.selectVoList(buildQueryWrapper(bo));
        enrich(members);
        return members;
    }

    @Override
    public List<ProjectMemberExportVo> queryExportList(ProjectMemberBo bo) {
        return queryList(bo).stream().map(this::toExportVo).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addMember(ProjectMemberBo bo) {
        scopeService.assertManagementAccess(bo.getProjectId(), ProjectManagementAction.WRITE_MEMBER);
        normalizeForAdd(bo);
        Project project = lockActiveProject(bo.getProjectId());
        validateEmployee(bo.getEmployeeId());
        validateValues(bo);
        if (memberMapper.existsActive(bo.getProjectId(), bo.getEmployeeId(), null)) {
            throw new ServiceException("该员工已是当前项目的有效成员");
        }
        ProjectMember member = BeanUtil.copyProperties(bo, ProjectMember.class);
        if (memberMapper.insert(member) == 0) {
            return false;
        }
        bo.setId(member.getId());
        if (YES.equals(member.getIsManager())) {
            synchronizeManager(project, member);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMember(ProjectMemberBo bo) {
        ProjectMember current = requireMember(bo.getId());
        scopeService.assertManagementAccess(current.getProjectId(), ProjectManagementAction.WRITE_MEMBER);
        if (INACTIVE.equals(current.getMemberStatus())) {
            throw new ServiceException("历史成员关系不允许修改，请重新加入项目");
        }
        Project project = lockProject(current.getProjectId());
        assertSubjectUnchanged(bo, current);
        fillImmutableAndDefaults(bo, current);
        validateEmployee(current.getEmployeeId());
        validateValues(bo);
        if (memberMapper.existsActive(current.getProjectId(), current.getEmployeeId(), current.getId())) {
            throw new ServiceException("该员工已存在其他有效成员关系");
        }

        ProjectMember update = BeanUtil.copyProperties(bo, ProjectMember.class);
        if (YES.equals(update.getIsManager()) && ACTIVE.equals(update.getMemberStatus())) {
            memberMapper.clearActiveManagers(current.getProjectId(), current.getId());
        }
        if (memberMapper.updateById(update) == 0) {
            throw new ServiceException("项目成员关系不存在或数据已变更");
        }
        if (YES.equals(update.getIsManager()) && ACTIVE.equals(update.getMemberStatus())) {
            projectMapper.updateManagerEmployeeId(current.getProjectId(), current.getEmployeeId());
        } else if (YES.equals(current.getIsManager())
            || Objects.equals(project.getManagerEmployeeId(), current.getEmployeeId())) {
            projectMapper.updateManagerEmployeeId(current.getProjectId(), null);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean endMember(Long id, LocalDate endDate) {
        ProjectMember current = requireMember(id);
        scopeService.assertManagementAccess(current.getProjectId(), ProjectManagementAction.WRITE_MEMBER);
        if (INACTIVE.equals(current.getMemberStatus())) {
            return true;
        }
        Project project = lockProject(current.getProjectId());
        LocalDate actualEndDate = endDate == null ? LocalDate.now() : endDate;
        if (actualEndDate.isBefore(current.getStartDate())) {
            throw new ServiceException("结束日期不能早于开始日期");
        }
        ProjectMember update = new ProjectMember();
        update.setId(id);
        update.setMemberStatus(INACTIVE);
        update.setEndDate(actualEndDate);
        update.setIsManager(NO);
        if (memberMapper.updateById(update) == 0) {
            throw new ServiceException("项目成员关系不存在或数据已变更");
        }
        if (YES.equals(current.getIsManager())
            || Objects.equals(project.getManagerEmployeeId(), current.getEmployeeId())) {
            projectMapper.updateManagerEmployeeId(current.getProjectId(), null);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setManager(Long id) {
        ProjectMember member = requireMember(id);
        scopeService.assertManagementAccess(member.getProjectId(), ProjectManagementAction.WRITE_MEMBER);
        Project project = lockProject(member.getProjectId());
        if (!ACTIVE.equals(member.getMemberStatus()) || !SystemConstants.NORMAL.equals(member.getStatus())) {
            throw new ServiceException("只有有效且启用的项目成员才能设为负责人");
        }
        validateEmployee(member.getEmployeeId());
        memberMapper.clearActiveManagers(member.getProjectId(), member.getId());
        ProjectMember update = new ProjectMember();
        update.setId(member.getId());
        update.setIsManager(YES);
        if (memberMapper.updateById(update) == 0) {
            throw new ServiceException("项目成员关系不存在或数据已变更");
        }
        if (projectMapper.updateManagerEmployeeId(project.getId(), member.getEmployeeId()) == 0) {
            throw new ServiceException("项目负责人同步失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelManager(Long id) {
        ProjectMember member = requireMember(id);
        scopeService.assertManagementAccess(member.getProjectId(), ProjectManagementAction.WRITE_MEMBER);
        Project project = lockProject(member.getProjectId());
        ProjectMember update = new ProjectMember();
        update.setId(id);
        update.setIsManager(NO);
        if (memberMapper.updateById(update) == 0) {
            throw new ServiceException("项目成员关系不存在或数据已变更");
        }
        if (YES.equals(member.getIsManager())
            || Objects.equals(project.getManagerEmployeeId(), member.getEmployeeId())) {
            projectMapper.updateManagerEmployeeId(member.getProjectId(), null);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWrongMembers(Collection<Long> ids) {
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(ids);
        List<ProjectMember> members = memberMapper.selectByIds(distinctIds);
        if (members.size() != distinctIds.size()) {
            throw new ServiceException("项目成员关系不存在或已删除");
        }
        members.forEach(member -> scopeService.assertManagementAccess(member.getProjectId(), ProjectManagementAction.WRITE_MEMBER));
        if (members.stream().anyMatch(member -> YES.equals(member.getIsManager()))) {
            throw new ServiceException("主要负责人关系不能直接删除，请先取消负责人或移出项目");
        }
        members.stream().map(ProjectMember::getProjectId).distinct().sorted(Comparator.naturalOrder())
            .forEach(this::lockProject);
        return memberMapper.deleteByIds(distinctIds) > 0;
    }

    @Override
    public List<ProjectMemberOptionVo> selectOptions(Long projectId) {
        if (projectId == null) throw new ServiceException("项目ID不能为空");
        scopeService.assertManagementAccess(projectId, ProjectManagementAction.OPTION);
        ProjectMemberBo bo = new ProjectMemberBo();
        bo.setProjectId(projectId);
        bo.setMemberStatus(ACTIVE);
        bo.setStatus(SystemConstants.NORMAL);
        return queryList(bo).stream().map(this::toOptionVo).toList();
    }

    private LambdaQueryWrapper<ProjectMember> buildQueryWrapper(ProjectMemberBo bo) {
        PropertyProjectScope scope = scopeService.resolveManagementScope(ProjectManagementAction.READ_MEMBER);
        if (scope.type() == PropertyProjectScope.Type.NONE) throw new ServiceException("无项目管理权限");
        return QueryBuilder.lambda(ProjectMember.class)
            .eqIfPresent(ProjectMember::getProjectId, bo.getProjectId())
            .in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS, ProjectMember::getProjectId, scope.projectIds())
            .eqIfPresent(ProjectMember::getEmployeeId, bo.getEmployeeId())
            .eqIfText(ProjectMember::getMemberRole, bo.getMemberRole())
            .eqIfText(ProjectMember::getMemberStatus, bo.getMemberStatus())
            .eqIfText(ProjectMember::getIsManager, bo.getIsManager())
            .eqIfText(ProjectMember::getStatus, bo.getStatus())
            .orderByAsc(ProjectMember::getSort)
            .orderByDesc(ProjectMember::getId)
            .build();
    }

    private void assertManagementList(ProjectManagementAction action) {
        if (scopeService.resolveManagementScope(action).type() == PropertyProjectScope.Type.NONE) {
            throw new ServiceException("无项目管理权限");
        }
    }

    private Project lockActiveProject(Long projectId) {
        Project project = lockProject(projectId);
        if (!SystemConstants.NORMAL.equals(project.getStatus())) {
            throw new ServiceException("停用项目不允许新增成员");
        }
        return project;
    }

    private Project lockProject(Long projectId) {
        Project project = projectMapper.selectByIdForUpdate(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在或已删除");
        }
        return project;
    }

    private ProjectMember requireMember(Long id) {
        ProjectMember member = memberMapper.selectById(id);
        if (member == null) {
            throw new ServiceException("项目成员关系不存在");
        }
        return member;
    }

    private void validateEmployee(Long employeeId) {
        EmployeeVo employee;
        try {
            employee = employeeService.queryById(employeeId);
        } catch (ServiceException ex) {
            throw new ServiceException("员工不存在或无权访问");
        }
        if (!ACTIVE.equals(employee.getEmploymentStatus())) {
            throw new ServiceException("只有在职员工可以加入项目");
        }
        if (!SystemConstants.NORMAL.equals(employee.getStatus())) {
            throw new ServiceException("停用员工不能加入项目");
        }
    }

    private void validateValues(ProjectMemberBo bo) {
        validateDict(DICT_MEMBER_ROLE, bo.getMemberRole(), "项目角色");
        validateDict(DICT_MEMBER_STATUS, bo.getMemberStatus(), "成员状态");
        validateDict(DICT_NORMAL_DISABLE, bo.getStatus(), "状态");
        if (!YES.equals(bo.getIsManager()) && !NO.equals(bo.getIsManager())) {
            throw new ServiceException("负责人标识无效");
        }
        if (bo.getStartDate() == null) {
            throw new ServiceException("开始日期不能为空");
        }
        if (ACTIVE.equals(bo.getMemberStatus()) && bo.getEndDate() != null) {
            throw new ServiceException("有效成员不能设置结束日期");
        }
        if (INACTIVE.equals(bo.getMemberStatus()) && bo.getEndDate() == null) {
            throw new ServiceException("已结束成员必须设置结束日期");
        }
        if (bo.getEndDate() != null && bo.getEndDate().isBefore(bo.getStartDate())) {
            throw new ServiceException("结束日期不能早于开始日期");
        }
        if (INACTIVE.equals(bo.getMemberStatus()) && YES.equals(bo.getIsManager())) {
            throw new ServiceException("已结束成员不能设为负责人");
        }
    }

    private void validateDict(String dictType, String value, String fieldName) {
        Map<String, String> values = dictService.getAllDictByDictType(dictType);
        if (StringUtils.isBlank(value) || values == null || !values.containsKey(value)) {
            throw new ServiceException(fieldName + "字典值无效");
        }
    }

    private void normalizeForAdd(ProjectMemberBo bo) {
        bo.setMemberRole(StringUtils.trim(bo.getMemberRole()));
        bo.setMemberStatus(StringUtils.isBlank(bo.getMemberStatus()) ? ACTIVE : StringUtils.trim(bo.getMemberStatus()));
        bo.setIsManager(StringUtils.isBlank(bo.getIsManager()) ? NO : StringUtils.trim(bo.getIsManager()));
        bo.setStatus(StringUtils.isBlank(bo.getStatus()) ? SystemConstants.NORMAL : StringUtils.trim(bo.getStatus()));
        bo.setSort(bo.getSort() == null ? 0 : bo.getSort());
        bo.setEndDate(ACTIVE.equals(bo.getMemberStatus()) ? null : bo.getEndDate());
    }

    private void assertSubjectUnchanged(ProjectMemberBo bo, ProjectMember current) {
        if (bo.getProjectId() != null && !bo.getProjectId().equals(current.getProjectId())) {
            throw new ServiceException("项目成员关系的项目不允许修改");
        }
        if (bo.getEmployeeId() != null && !bo.getEmployeeId().equals(current.getEmployeeId())) {
            throw new ServiceException("项目成员关系的员工不允许修改");
        }
    }

    private void fillImmutableAndDefaults(ProjectMemberBo bo, ProjectMember current) {
        bo.setProjectId(current.getProjectId());
        bo.setEmployeeId(current.getEmployeeId());
        bo.setMemberRole(StringUtils.trim(bo.getMemberRole()));
        bo.setMemberStatus(StringUtils.isBlank(bo.getMemberStatus()) ? current.getMemberStatus() : StringUtils.trim(bo.getMemberStatus()));
        bo.setIsManager(StringUtils.isBlank(bo.getIsManager()) ? current.getIsManager() : StringUtils.trim(bo.getIsManager()));
        bo.setStatus(StringUtils.isBlank(bo.getStatus()) ? current.getStatus() : StringUtils.trim(bo.getStatus()));
        bo.setSort(bo.getSort() == null ? current.getSort() : bo.getSort());
    }

    private void synchronizeManager(Project project, ProjectMember member) {
        if (!ACTIVE.equals(member.getMemberStatus())) {
            throw new ServiceException("只有有效成员才能设为负责人");
        }
        memberMapper.clearActiveManagers(project.getId(), member.getId());
        if (projectMapper.updateManagerEmployeeId(project.getId(), member.getEmployeeId()) == 0) {
            throw new ServiceException("项目负责人同步失败");
        }
    }

    private void enrich(List<ProjectMemberVo> members) {
        if (members.isEmpty()) {
            return;
        }
        List<Long> projectIds = members.stream().map(ProjectMemberVo::getProjectId).filter(Objects::nonNull).distinct().toList();
        List<Long> employeeIds = members.stream().map(ProjectMemberVo::getEmployeeId).filter(Objects::nonNull).distinct().toList();
        Map<Long, ProjectVo> projects = projectMapper.selectVoByIds(projectIds).stream()
            .collect(Collectors.toMap(ProjectVo::getId, Function.identity()));
        Map<Long, EmployeeOptionVo> employees = employeeService.selectEmployeeOptionsByIds(employeeIds).stream()
            .collect(Collectors.toMap(EmployeeOptionVo::getId, Function.identity()));
        for (ProjectMemberVo member : members) {
            ProjectVo project = projects.get(member.getProjectId());
            if (project != null) {
                member.setProjectCode(project.getProjectCode());
                member.setProjectName(project.getProjectName());
            }
            EmployeeOptionVo employee = employees.get(member.getEmployeeId());
            if (employee != null) {
                member.setEmployeeNo(employee.getEmployeeNo());
                member.setEmployeeName(employee.getEmployeeName());
                member.setDeptName(employee.getDeptName());
                member.setPostName(employee.getPostName());
            }
        }
    }

    private ProjectMemberExportVo toExportVo(ProjectMemberVo source) {
        ProjectMemberExportVo target = BeanUtil.copyProperties(source, ProjectMemberExportVo.class);
        return target;
    }

    private ProjectMemberOptionVo toOptionVo(ProjectMemberVo source) {
        ProjectMemberOptionVo target = new ProjectMemberOptionVo();
        target.setMemberId(source.getId());
        target.setEmployeeId(source.getEmployeeId());
        target.setEmployeeNo(source.getEmployeeNo());
        target.setEmployeeName(source.getEmployeeName());
        target.setProjectId(source.getProjectId());
        target.setProjectName(source.getProjectName());
        target.setMemberRole(source.getMemberRole());
        target.setIsManager(source.getIsManager());
        return target;
    }
}
