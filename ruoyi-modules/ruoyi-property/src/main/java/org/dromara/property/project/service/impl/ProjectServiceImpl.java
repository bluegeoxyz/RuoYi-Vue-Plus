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
import org.dromara.property.project.domain.bo.ProjectBo;
import org.dromara.property.project.domain.vo.ProjectExportVo;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.project.domain.vo.ProjectVo;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.project.mapper.ProjectMemberMapper;
import org.dromara.property.project.service.IProjectService;
import org.dromara.property.scope.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物业项目服务实现。
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements IProjectService {

    private static final String DICT_PROJECT_TYPE = "pm_project_type";
    private static final String DICT_NORMAL_DISABLE = "sys_normal_disable";
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");
    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final IEmployeeService employeeService;
    private final DictService dictService;
    private final PropertyProjectScopeService scopeService;

    @Override
    public ProjectVo queryById(Long id) {
        scopeService.assertManagementAccess(id, ProjectManagementAction.READ_PROJECT);
        ProjectVo project = projectMapper.selectVoById(id);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        enrichManagers(List.of(project));
        return project;
    }

    @Override
    public PageResult<ProjectVo> queryPageList(ProjectBo bo, PageQuery pageQuery) {
        assertManagementList(ProjectManagementAction.READ_PROJECT);
        Page<ProjectVo> result = projectMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        enrichManagers(result.getRecords());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<ProjectVo> queryList(ProjectBo bo) {
        assertManagementList(ProjectManagementAction.READ_PROJECT);
        List<ProjectVo> projects = projectMapper.selectVoList(buildQueryWrapper(bo));
        enrichManagers(projects);
        return projects;
    }

    @Override
    public List<ProjectExportVo> queryExportList(ProjectBo bo) {
        return queryList(bo).stream().map(this::toExportVo).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ProjectBo bo) {
        scopeService.assertCanCreateProject();
        if (bo.getManagerEmployeeId() != null) throw new ServiceException("项目负责人只能通过项目成员管理设置");
        normalize(bo);
        validateBeforeSave(bo, null);
        Project project = BeanUtil.copyProperties(bo, Project.class);
        try {
            boolean success = projectMapper.insert(project) > 0;
            if (success) {
                bo.setId(project.getId());
            }
            return success;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("项目编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ProjectBo bo) {
        scopeService.assertManagementAccess(bo.getId(), ProjectManagementAction.WRITE_PROJECT);
        Project current = projectMapper.selectById(bo.getId());
        if (current == null) {
            throw new ServiceException("项目不存在");
        }
        if (bo.getManagerEmployeeId() != null && !java.util.Objects.equals(bo.getManagerEmployeeId(), current.getManagerEmployeeId())) {
            throw new ServiceException("项目负责人只能通过项目成员管理设置");
        }
        bo.setManagerEmployeeId(current.getManagerEmployeeId());
        normalize(bo);
        validateBeforeSave(bo, bo.getId());
        Project project = BeanUtil.copyProperties(bo, Project.class);
        try {
            if (projectMapper.updateById(project) == 0) {
                throw new ServiceException("项目不存在或数据已变更");
            }
            return true;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("项目编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeStatus(Long id, String status) {
        scopeService.assertManagementAccess(id, ProjectManagementAction.WRITE_PROJECT);
        validateDict(DICT_NORMAL_DISABLE, status, true, "状态");
        if (projectMapper.selectById(id) == null) {
            throw new ServiceException("项目不存在");
        }
        Project project = new Project();
        project.setId(id);
        project.setStatus(status);
        return projectMapper.updateById(project) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(ids);
        distinctIds.forEach(id -> scopeService.assertManagementAccess(id, ProjectManagementAction.WRITE_PROJECT));
        if (projectMapper.selectByIds(distinctIds).size() != distinctIds.size()) {
            throw new ServiceException("项目不存在或已删除");
        }
        if (projectMemberMapper.existsByProjectIds(distinctIds)) {
            throw new ServiceException("项目存在成员历史关系，不能删除，建议停用项目");
        }
        return projectMapper.deleteByIds(distinctIds) > 0;
    }

    @Override
    public List<ProjectOptionVo> selectOptions() {
        PropertyProjectScope scope = scopeService.resolveManagementScope(ProjectManagementAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) throw new ServiceException("无项目管理权限");
        if (scope.type() == PropertyProjectScope.Type.ALL) return projectMapper.selectOptions();
        return projectMapper.selectVoList(QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getProjectShortName)
            .in(Project::getId, scope.projectIds()).eq(Project::getStatus, "0")
            .orderByAsc(Project::getSort).orderByAsc(Project::getId).build(), ProjectOptionVo.class);
    }

    @Override
    public List<ProjectOptionVo> selectBusinessOptions() {
        PropertyProjectScope scope = scopeService.resolveBusinessScope(PropertyBusinessDomain.PROJECT_BASE, ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        if (scope.type() == PropertyProjectScope.Type.ALL) return projectMapper.selectOptions();
        return projectMapper.selectVoList(QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getProjectShortName)
            .in(Project::getId, scope.projectIds()).eq(Project::getStatus, "0")
            .orderByAsc(Project::getSort).orderByAsc(Project::getId).build(), ProjectOptionVo.class);
    }

    private void assertManagementList(ProjectManagementAction action) {
        if (scopeService.resolveManagementScope(action).type() == PropertyProjectScope.Type.NONE) {
            throw new ServiceException("无项目管理权限");
        }
    }

    private LambdaQueryWrapper<Project> buildQueryWrapper(ProjectBo bo) {
        PropertyProjectScope scope = scopeService.resolveManagementScope(ProjectManagementAction.READ_PROJECT);
        if (scope.type() == PropertyProjectScope.Type.NONE) throw new ServiceException("无项目管理权限");
        return QueryBuilder.lambda(Project.class)
            .likeIfText(Project::getProjectCode, bo.getProjectCode())
            .likeIfText(Project::getProjectName, bo.getProjectName())
            .eqIfText(Project::getProjectType, bo.getProjectType())
            .eqIfPresent(Project::getManagerEmployeeId, bo.getManagerEmployeeId())
            .eqIfText(Project::getStatus, bo.getStatus())
            .eqIfText(Project::getProvinceCode, bo.getProvinceCode())
            .eqIfText(Project::getCityCode, bo.getCityCode())
            .eqIfText(Project::getDistrictCode, bo.getDistrictCode())
            .in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS, Project::getId, scope.projectIds())
            .orderByAsc(Project::getSort)
            .orderByDesc(Project::getId)
            .build();
    }

    private void validateBeforeSave(ProjectBo bo, Long excludeId) {
        if (projectMapper.countByProjectCode(bo.getProjectCode(), excludeId) > 0) {
            throw new ServiceException("项目编码已存在");
        }
        validateDict(DICT_PROJECT_TYPE, bo.getProjectType(), false, "项目类型");
        validateDict(DICT_NORMAL_DISABLE, bo.getStatus(), true, "状态");
        validateManager(bo.getManagerEmployeeId());
        validateCoordinate(bo.getLongitude(), MIN_LONGITUDE, MAX_LONGITUDE, "经度");
        validateCoordinate(bo.getLatitude(), MIN_LATITUDE, MAX_LATITUDE, "纬度");
    }

    private void validateManager(Long employeeId) {
        if (employeeId == null) {
            return;
        }
        EmployeeVo employee;
        try {
            employee = employeeService.queryById(employeeId);
        } catch (ServiceException ex) {
            throw new ServiceException("项目负责人不存在或无权访问");
        }
        if (!"ACTIVE".equals(employee.getEmploymentStatus())) {
            throw new ServiceException("项目负责人必须是在职员工");
        }
        if (!SystemConstants.NORMAL.equals(employee.getStatus())) {
            throw new ServiceException("项目负责人档案已停用");
        }
    }

    private void validateCoordinate(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName) {
        if (value != null && (value.compareTo(min) < 0 || value.compareTo(max) > 0)) {
            throw new ServiceException(fieldName + "超出有效范围");
        }
    }

    private void validateDict(String dictType, String value, boolean required, String fieldName) {
        if (StringUtils.isBlank(value)) {
            if (required) {
                throw new ServiceException(fieldName + "不能为空");
            }
            return;
        }
        Map<String, String> values = dictService.getAllDictByDictType(dictType);
        if (values == null || !values.containsKey(value)) {
            throw new ServiceException(fieldName + "字典值无效");
        }
    }

    private void normalize(ProjectBo bo) {
        bo.setProjectCode(StringUtils.trim(bo.getProjectCode()));
        bo.setProjectName(StringUtils.trim(bo.getProjectName()));
        bo.setProjectShortName(StringUtils.trim(bo.getProjectShortName()));
        bo.setProvinceCode(StringUtils.trim(bo.getProvinceCode()));
        bo.setCityCode(StringUtils.trim(bo.getCityCode()));
        bo.setDistrictCode(StringUtils.trim(bo.getDistrictCode()));
        bo.setAddress(StringUtils.trim(bo.getAddress()));
        bo.setServicePhone(StringUtils.trim(bo.getServicePhone()));
        if (StringUtils.isBlank(bo.getProjectCode())) {
            throw new ServiceException("项目编码不能为空");
        }
        if (StringUtils.isBlank(bo.getProjectName())) {
            throw new ServiceException("项目名称不能为空");
        }
        if (bo.getSort() == null) {
            bo.setSort(0);
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(SystemConstants.NORMAL);
        }
    }

    private void enrichManagers(List<ProjectVo> projects) {
        List<Long> employeeIds = projects.stream()
            .map(ProjectVo::getManagerEmployeeId)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();
        if (employeeIds.isEmpty()) {
            return;
        }
        Map<Long, EmployeeOptionVo> employees = employeeService.selectEmployeeOptionsByIds(employeeIds).stream()
            .collect(Collectors.toMap(EmployeeOptionVo::getId, Function.identity()));
        for (ProjectVo project : projects) {
            EmployeeOptionVo manager = employees.get(project.getManagerEmployeeId());
            if (manager != null) {
                project.setManagerEmployeeName(manager.getEmployeeName());
                project.setManagerEmployeeNo(manager.getEmployeeNo());
            }
        }
    }

    private ProjectExportVo toExportVo(ProjectVo source) {
        ProjectExportVo target = new ProjectExportVo();
        target.setProjectCode(source.getProjectCode());
        target.setProjectName(source.getProjectName());
        target.setProjectShortName(source.getProjectShortName());
        target.setProjectType(source.getProjectType());
        target.setManagerEmployeeName(source.getManagerEmployeeName());
        target.setServicePhone(source.getServicePhone());
        target.setRegionCode(java.util.stream.Stream.of(source.getProvinceCode(), source.getCityCode(), source.getDistrictCode())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining("/")));
        target.setAddress(source.getAddress());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setStatus(source.getStatus());
        return target;
    }
}
