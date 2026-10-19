package org.dromara.hr.employee.service.impl;

import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.sensitive.core.SensitiveStrategy;
import org.dromara.hr.employee.domain.Employee;
import org.dromara.hr.employee.domain.bo.EmployeeBo;
import org.dromara.hr.employee.domain.bo.EmployeeUserOptionQuery;
import org.dromara.hr.employee.domain.vo.EmployeeExportVo;
import org.dromara.hr.employee.domain.vo.EmployeeOptionVo;
import org.dromara.hr.employee.domain.vo.EmployeeVo;
import org.dromara.hr.employee.domain.vo.EmployeeUserOptionVo;
import org.dromara.hr.employee.mapper.EmployeeMapper;
import org.dromara.hr.employee.service.IEmployeeService;
import org.dromara.hr.employee.service.EmployeeReferenceChecker;
import org.dromara.system.domain.bo.SysDeptBo;
import org.dromara.system.domain.bo.SysPostBo;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysPostVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysPostService;
import org.dromara.system.service.ISysUserService;
import org.dromara.system.event.OnlineUserCleanEvent;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 员工档案服务实现。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements IEmployeeService {

    private static final String DICT_GENDER = "sys_user_gender";
    private static final String DICT_ID_TYPE = "zbm_id_type";
    private static final String DICT_EMPLOYMENT_TYPE = "hr_employment_type";
    private static final String DICT_EMPLOYMENT_STATUS = "hr_employment_status";
    private static final String DICT_NORMAL_DISABLE = "sys_normal_disable";

    private final EmployeeMapper employeeMapper;
    private final ISysDeptService deptService;
    private final ISysPostService postService;
    private final ISysUserService userService;
    private final DictService dictService;
    private final List<EmployeeReferenceChecker> referenceCheckers;

    @Override
    public EmployeeVo queryById(Long id) {
        EmployeeVo employee = employeeMapper.selectEmployeeById(id);
        if (employee == null) {
            throw new ServiceException("员工不存在或无权访问");
        }
        return employee;
    }

    @Override
    public PageResult<EmployeeVo> queryPageList(EmployeeBo bo, PageQuery pageQuery) {
        var result = employeeMapper.selectEmployeePage(pageQuery.build(), bo, resolveDeptIds(bo.getDeptId()));
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<EmployeeExportVo> queryExportList(EmployeeBo bo) {
        return employeeMapper.selectEmployeeList(bo, resolveDeptIds(bo.getDeptId())).stream()
            .map(this::toExportVo)
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(EmployeeBo bo) {
        normalize(bo);
        validateBeforeSave(bo, null);
        Employee employee = toEntity(bo);
        employee.setUserId(null);
        try {
            boolean success = employeeMapper.insert(employee) > 0;
            if (success) {
                bo.setId(employee.getId());
            }
            return success;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("员工编号已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(EmployeeBo bo) {
        EmployeeVo current = queryById(bo.getId());
        normalize(bo);
        validateBeforeSave(bo, bo.getId());
        Employee employee = toEntity(bo);
        employee.setUserId(current.getUserId());
        try {
            if (employeeMapper.updateById(employee) == 0) {
                throw new ServiceException("员工不存在、无权访问或数据已变更");
            }
            return true;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("员工编号已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeStatus(Long id, String status) {
        validateDict(DICT_NORMAL_DISABLE, status, true, "状态");
        queryById(id);
        Employee update = new Employee();
        update.setId(id);
        update.setStatus(status);
        if (employeeMapper.updateById(update) == 0) {
            throw new ServiceException("员工不存在、无权访问或数据已变更");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindUser(Long employeeId, Long userId) {
        EmployeeVo employee = queryById(employeeId);
        if (userId.equals(employee.getUserId())) {
            return true;
        }
        if (employee.getUserId() != null) {
            throw new ServiceException("该员工已绑定系统账号，请先解绑后再重新绑定");
        }
        if (!"ACTIVE".equals(employee.getEmploymentStatus())) {
            throw new ServiceException("离职员工不允许绑定系统账号");
        }
        if (!SystemConstants.NORMAL.equals(employee.getStatus())) {
            throw new ServiceException("已停用员工不允许绑定系统账号");
        }
        if (SystemConstants.SUPER_ADMIN_USER_ID.equals(userId)) {
            throw new ServiceException("超级管理员账号不允许绑定员工");
        }

        userService.checkUserDataScope(userId);
        SysUserVo user = userService.selectUserByIdForUpdate(userId);
        if (user == null) {
            throw new ServiceException("系统账号不存在或已删除");
        }
        Employee occupied = employeeMapper.selectByUserId(userId);
        if (occupied != null && !employeeId.equals(occupied.getId())) {
            throw new ServiceException("该系统账号已绑定其他员工");
        }
        if (employeeMapper.bindUserIfUnbound(employeeId, userId) == 0) {
            throw new ServiceException("员工账号绑定失败，员工可能已被其他操作绑定");
        }
        log.info("员工账号绑定成功 employeeId={}, employeeNo={}, employeeName={}, userId={}, userName={}",
            employeeId, employee.getEmployeeNo(), employee.getEmployeeName(), userId, user.getUserName());
        SpringUtils.context().publishEvent(OnlineUserCleanEvent.byUsers(List.of(userId)));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unbindUser(Long employeeId) {
        EmployeeVo employee = queryById(employeeId);
        Long userId = employee.getUserId();
        if (userId == null) {
            return true;
        }
        if (employeeMapper.unbindUser(employeeId, userId) == 0) {
            throw new ServiceException("员工账号解绑失败，绑定关系可能已变更");
        }
        log.info("员工账号解绑成功 employeeId={}, employeeNo={}, employeeName={}, userId={}, userName={}",
            employeeId, employee.getEmployeeNo(), employee.getEmployeeName(), userId, employee.getUserName());
        SpringUtils.context().publishEvent(OnlineUserCleanEvent.byUsers(List.of(userId)));
        return true;
    }

    @Override
    public PageResult<EmployeeUserOptionVo> selectUserOptions(EmployeeUserOptionQuery query, PageQuery pageQuery) {
        SysUserBo userQuery = new SysUserBo();
        userQuery.setUserName(StringUtils.trim(query.getUserName()));
        userQuery.setNickName(StringUtils.trim(query.getNickName()));
        userQuery.setPhoneNumber(StringUtils.trim(query.getPhoneNumber()));
        userQuery.setExcludeUserIds(String.valueOf(SystemConstants.SUPER_ADMIN_USER_ID));
        PageResult<SysUserVo> userPage = userService.selectPageUserList(userQuery, pageQuery);
        List<SysUserVo> users = List.copyOf(userPage.getRows());
        Set<Long> boundUserIds = employeeMapper.selectBoundUserIds(users.stream().map(SysUserVo::getUserId).toList());
        List<EmployeeUserOptionVo> rows = users.stream().map(user -> toUserOption(user, boundUserIds)).toList();
        return PageResult.build(rows, userPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(ids);
        List<EmployeeVo> employees = employeeMapper.selectEmployeesByIds(distinctIds);
        if (employees.size() != distinctIds.size()) {
            throw new ServiceException("员工不存在或无权删除");
        }
        if (employees.stream().anyMatch(employee -> employee.getUserId() != null)) {
            throw new ServiceException("已绑定系统用户的员工不能删除，请先解除绑定");
        }
        for (EmployeeReferenceChecker checker : referenceCheckers) {
            checker.findDeleteBlockReason(distinctIds).ifPresent(reason -> {
                throw new ServiceException(reason);
            });
        }
        return employeeMapper.deleteEmployeesByIds(distinctIds) > 0;
    }

    @Override
    public List<EmployeeOptionVo> selectEmployeeOptions() {
        return employeeMapper.selectEmployeeOptions();
    }

    @Override
    public List<EmployeeOptionVo> selectEmployeeOptionsByIds(Collection<Long> ids) {
        return employeeMapper.selectEmployeeOptionsByIds(ids);
    }

    @Override
    public Long findActiveEmployeeIdByUserId(Long userId) {
        return userId == null ? null : employeeMapper.selectActiveEmployeeIdByUserId(userId);
    }

    @Override
    public List<Tree<Long>> selectDeptTree() {
        SysDeptBo bo = new SysDeptBo();
        bo.setStatus(SystemConstants.NORMAL);
        return deptService.selectDeptTreeList(bo);
    }

    @Override
    public List<SysPostVo> selectPostOptions(Long deptId) {
        deptService.checkDeptDataScope(deptId);
        SysPostBo bo = new SysPostBo();
        bo.setDeptId(deptId);
        bo.setStatus(SystemConstants.NORMAL);
        return postService.selectPostList(bo);
    }

    private List<Long> resolveDeptIds(Long deptId) {
        if (deptId == null) {
            return null;
        }
        validateDept(deptId);
        SysDeptBo deptBo = new SysDeptBo();
        deptBo.setBelongDeptId(deptId);
        List<Long> deptIds = deptService.selectDeptList(deptBo).stream().map(SysDeptVo::getDeptId).toList();
        return deptIds.isEmpty() ? List.of(deptId) : deptIds;
    }

    private void validateBeforeSave(EmployeeBo bo, Long excludeId) {
        if (employeeMapper.countByEmployeeNo(bo.getEmployeeNo(), excludeId) > 0) {
            throw new ServiceException("员工编号已存在");
        }
        validateDept(bo.getDeptId());
        validatePost(bo.getPostId(), bo.getDeptId());
        validateDict(DICT_GENDER, bo.getGender(), false, "性别");
        validateDict(DICT_ID_TYPE, bo.getIdType(), false, "证件类型");
        validateDict(DICT_EMPLOYMENT_TYPE, bo.getEmploymentType(), false, "用工类型");
        validateDict(DICT_EMPLOYMENT_STATUS, bo.getEmploymentStatus(), true, "员工状态");
        validateDict(DICT_NORMAL_DISABLE, bo.getStatus(), true, "状态");
        if (bo.getLeaveDate() != null && bo.getEntryDate() != null && bo.getLeaveDate().isBefore(bo.getEntryDate())) {
            throw new ServiceException("离职日期不能早于入职日期");
        }
    }

    private void validateDept(Long deptId) {
        deptService.checkDeptDataScope(deptId);
        SysDeptVo dept = deptService.selectDeptById(deptId);
        if (dept == null) {
            throw new ServiceException("所属部门不存在");
        }
        if (!SystemConstants.NORMAL.equals(dept.getStatus())) {
            throw new ServiceException("所属部门已停用");
        }
    }

    private void validatePost(Long postId, Long deptId) {
        if (postId == null) {
            return;
        }
        SysPostVo post = postService.selectPostById(postId);
        if (post == null) {
            throw new ServiceException("主岗位不存在");
        }
        if (!SystemConstants.NORMAL.equals(post.getStatus())) {
            throw new ServiceException("主岗位已停用");
        }
        if (!deptId.equals(post.getDeptId())) {
            throw new ServiceException("主岗位不属于所选部门");
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

    private void normalize(EmployeeBo bo) {
        bo.setEmployeeNo(StringUtils.trim(bo.getEmployeeNo()));
        bo.setEmployeeName(StringUtils.trim(bo.getEmployeeName()));
        bo.setMobile(StringUtils.trim(bo.getMobile()));
        bo.setEmail(StringUtils.trim(bo.getEmail()));
        bo.setIdNo(StringUtils.trim(bo.getIdNo()));
        if (bo.getSort() == null) {
            bo.setSort(0);
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(SystemConstants.NORMAL);
        }
        if (StringUtils.isBlank(bo.getEmploymentStatus())) {
            bo.setEmploymentStatus("ACTIVE");
        }
    }

    private EmployeeExportVo toExportVo(EmployeeVo source) {
        EmployeeExportVo target = new EmployeeExportVo();
        target.setEmployeeNo(source.getEmployeeNo());
        target.setEmployeeName(source.getEmployeeName());
        target.setDeptName(source.getDeptName());
        target.setPostName(source.getPostName());
        target.setGender(source.getGender());
        target.setEmploymentType(source.getEmploymentType());
        target.setEmploymentStatus(source.getEmploymentStatus());
        target.setEntryDate(source.getEntryDate());
        target.setLeaveDate(source.getLeaveDate());
        target.setStatus(source.getStatus());
        if (StringUtils.isNotBlank(source.getMobile())) {
            target.setMobile(SensitiveStrategy.PHONE.desensitizer().apply(source.getMobile()));
        }
        if (StringUtils.isNotBlank(source.getEmail())) {
            target.setEmail(SensitiveStrategy.EMAIL.desensitizer().apply(source.getEmail()));
        }
        return target;
    }

    private Employee toEntity(EmployeeBo bo) {
        Employee target = new Employee();
        target.setId(bo.getId());
        target.setEmployeeNo(bo.getEmployeeNo());
        target.setEmployeeName(bo.getEmployeeName());
        target.setDeptId(bo.getDeptId());
        target.setPostId(bo.getPostId());
        target.setGender(bo.getGender());
        target.setMobile(bo.getMobile());
        target.setEmail(bo.getEmail());
        target.setIdType(bo.getIdType());
        target.setIdNo(bo.getIdNo());
        target.setBirthDate(bo.getBirthDate());
        target.setEntryDate(bo.getEntryDate());
        target.setLeaveDate(bo.getLeaveDate());
        target.setEmploymentType(bo.getEmploymentType());
        target.setEmploymentStatus(bo.getEmploymentStatus());
        target.setSort(bo.getSort());
        target.setStatus(bo.getStatus());
        target.setRemark(bo.getRemark());
        return target;
    }

    private EmployeeUserOptionVo toUserOption(SysUserVo user, Set<Long> boundUserIds) {
        EmployeeUserOptionVo target = new EmployeeUserOptionVo();
        target.setUserId(user.getUserId());
        target.setUserName(user.getUserName());
        target.setNickName(user.getNickName());
        target.setDeptName(user.getDeptName());
        target.setStatus(user.getStatus());
        target.setBound(boundUserIds.contains(user.getUserId()));
        return target;
    }
}
