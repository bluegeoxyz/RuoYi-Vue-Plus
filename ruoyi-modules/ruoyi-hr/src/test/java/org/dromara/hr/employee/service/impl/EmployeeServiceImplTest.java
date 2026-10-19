package org.dromara.hr.employee.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.service.DictService;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.hr.employee.domain.Employee;
import org.dromara.hr.employee.domain.bo.EmployeeBo;
import org.dromara.hr.employee.domain.vo.EmployeeVo;
import org.dromara.hr.employee.mapper.EmployeeMapper;
import org.dromara.hr.employee.service.EmployeeReferenceChecker;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysPostVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysPostService;
import org.dromara.system.service.ISysUserService;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 员工档案服务单元测试。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private ISysDeptService deptService;
    @Mock
    private ISysPostService postService;
    @Mock
    private ISysUserService userService;
    @Mock
    private DictService dictService;
    @Mock
    private List<EmployeeReferenceChecker> referenceCheckers;
    @Mock
    private ApplicationContext applicationContext;
    @InjectMocks
    private EmployeeServiceImpl service;
    private MockedStatic<SpringUtils> springUtils;

    @BeforeEach
    void setUp() {
        springUtils = org.mockito.Mockito.mockStatic(SpringUtils.class);
        springUtils.when(SpringUtils::context).thenReturn(applicationContext);
        when(referenceCheckers.iterator()).thenReturn(java.util.Collections.emptyIterator());
        SysDeptVo dept = new SysDeptVo();
        dept.setDeptId(100L);
        dept.setStatus("0");
        when(deptService.selectDeptById(100L)).thenReturn(dept);
        when(dictService.getAllDictByDictType(anyString())).thenAnswer(invocation -> switch ((String) invocation.getArgument(0)) {
            case "sys_user_gender" -> Map.of("0", "男", "1", "女", "2", "未知");
            case "zbm_id_type" -> Map.of("ID_CARD", "居民身份证", "PASSPORT", "护照", "OTHER", "其他");
            case "hr_employment_type" -> Map.of("FORMAL", "正式员工");
            case "hr_employment_status" -> Map.of("ACTIVE", "在职", "LEFT", "离职");
            case "sys_normal_disable" -> Map.of("0", "正常", "1", "停用");
            default -> Map.of();
        });
    }

    @AfterEach
    void tearDown() {
        springUtils.close();
    }

    @Test
    void insertShouldSucceedWithoutAccountBindingField() {
        EmployeeBo bo = validBo();
        doAnswer(invocation -> {
            Employee entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        }).when(employeeMapper).insert(any(Employee.class));

        assertTrue(service.insertByBo(bo));
        assertEquals(1L, bo.getId());
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).insert(captor.capture());
        assertEquals(null, captor.getValue().getUserId());
    }

    @Test
    void duplicateEmployeeNoShouldFail() {
        when(employeeMapper.countByEmployeeNo("E001", null)).thenReturn(1L);
        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertByBo(validBo()));
        assertEquals("员工编号已存在", exception.getMessage());
        verify(employeeMapper, never()).insert(any(Employee.class));
    }

    @Test
    void invalidDepartmentShouldFail() {
        when(deptService.selectDeptById(100L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.insertByBo(validBo()));
    }

    @Test
    void stoppedPostShouldFail() {
        EmployeeBo bo = validBo();
        bo.setPostId(200L);
        SysPostVo post = new SysPostVo();
        post.setPostId(200L);
        post.setDeptId(100L);
        post.setStatus("1");
        when(postService.selectPostById(200L)).thenReturn(post);
        assertThrows(ServiceException.class, () -> service.insertByBo(bo));
    }

    @Test
    void postFromAnotherDepartmentShouldFail() {
        EmployeeBo bo = validBo();
        bo.setPostId(200L);
        SysPostVo post = new SysPostVo();
        post.setPostId(200L);
        post.setDeptId(101L);
        post.setStatus("0");
        when(postService.selectPostById(200L)).thenReturn(post);
        assertThrows(ServiceException.class, () -> service.insertByBo(bo));
    }

    @Test
    void invalidDictionaryValueShouldFail() {
        EmployeeBo bo = validBo();
        bo.setEmploymentStatus("UNKNOWN");
        assertThrows(ServiceException.class, () -> service.insertByBo(bo));
    }

    @Test
    void leaveDateBeforeEntryDateShouldFail() {
        EmployeeBo bo = validBo();
        bo.setEntryDate(LocalDate.of(2026, 9, 2));
        bo.setLeaveDate(LocalDate.of(2026, 9, 1));
        assertThrows(ServiceException.class, () -> service.insertByBo(bo));
    }

    @Test
    void detailOutsideDataScopeShouldLookLikeNotFound() {
        when(employeeMapper.selectEmployeeById(8L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.queryById(8L));
    }

    @Test
    void pageQueryShouldUseDataPermissionMapperEntry() {
        Page<EmployeeVo> page = new Page<>(1, 10);
        page.setRecords(List.of(new EmployeeVo()));
        page.setTotal(1L);
        when(employeeMapper.selectEmployeePage(any(), any(), isNull())).thenReturn(page);
        assertEquals(1L, service.queryPageList(new EmployeeBo(), new PageQuery(10, 1)).getTotal());
    }

    @Test
    void updateShouldPreserveBoundUserId() {
        EmployeeVo current = new EmployeeVo();
        current.setId(1L);
        current.setUserId(88L);
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(current);
        when(employeeMapper.updateById(any(Employee.class))).thenReturn(1);
        EmployeeBo bo = validBo();
        bo.setId(1L);
        assertTrue(service.updateByBo(bo));
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).updateById(captor.capture());
        assertEquals(88L, captor.getValue().getUserId());
    }

    @Test
    void employeeBoShouldNotExposeUserId() {
        assertThrows(NoSuchFieldException.class, () -> EmployeeBo.class.getDeclaredField("userId"));
    }

    @Test
    void bindUserShouldSucceedForActiveEmployeeAndExistingAccount() {
        EmployeeVo employee = activeEmployee(1L, null);
        SysUserVo user = user(88L, "zhangsan");
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(employee);
        when(userService.selectUserByIdForUpdate(88L)).thenReturn(user);
        when(employeeMapper.bindUserIfUnbound(1L, 88L)).thenReturn(1);

        assertTrue(service.bindUser(1L, 88L));
        verify(userService).checkUserDataScope(88L);
        verify(employeeMapper).selectByUserId(88L);
    }

    @Test
    void repeatedBindSameUserShouldBeIdempotent() {
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(activeEmployee(1L, 88L));
        assertTrue(service.bindUser(1L, 88L));
        verify(userService, never()).selectUserByIdForUpdate(anyLong());
    }

    @Test
    void bindDifferentUserShouldFailWhenEmployeeAlreadyBound() {
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(activeEmployee(1L, 88L));
        assertThrows(ServiceException.class, () -> service.bindUser(1L, 99L));
    }

    @Test
    void bindShouldFailWhenUserOccupiedByAnotherEmployee() {
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(activeEmployee(1L, null));
        when(userService.selectUserByIdForUpdate(88L)).thenReturn(user(88L, "zhangsan"));
        Employee occupied = new Employee();
        occupied.setId(2L);
        when(employeeMapper.selectByUserId(88L)).thenReturn(occupied);
        assertThrows(ServiceException.class, () -> service.bindUser(1L, 88L));
        verify(employeeMapper, never()).bindUserIfUnbound(anyLong(), anyLong());
    }

    @Test
    void bindShouldFailWhenEmployeeMissingOrOutsideDataScope() {
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.bindUser(1L, 88L));
    }

    @Test
    void bindShouldFailWhenUserMissingOrDeleted() {
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(activeEmployee(1L, null));
        when(userService.selectUserByIdForUpdate(88L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.bindUser(1L, 88L));
    }

    @Test
    void bindShouldFailWithoutUserDataScope() {
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(activeEmployee(1L, null));
        doThrow(new ServiceException("没有权限访问用户数据！")).when(userService).checkUserDataScope(88L);
        assertThrows(ServiceException.class, () -> service.bindUser(1L, 88L));
        verify(userService, never()).selectUserByIdForUpdate(anyLong());
    }

    @Test
    void bindShouldFailForLeftOrDisabledEmployee() {
        EmployeeVo left = activeEmployee(1L, null);
        left.setEmploymentStatus("LEFT");
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(left);
        assertThrows(ServiceException.class, () -> service.bindUser(1L, 88L));

        EmployeeVo disabled = activeEmployee(2L, null);
        disabled.setStatus("1");
        when(employeeMapper.selectEmployeeById(2L)).thenReturn(disabled);
        assertThrows(ServiceException.class, () -> service.bindUser(2L, 88L));
    }

    @Test
    void bindShouldRejectSuperAdmin() {
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(activeEmployee(1L, null));
        assertThrows(ServiceException.class, () -> service.bindUser(1L, SystemConstants.SUPER_ADMIN_USER_ID));
        verify(userService, never()).checkUserDataScope(anyLong());
    }

    @Test
    void conditionalBindFailureShouldRejectConcurrentSecondBinding() {
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(activeEmployee(1L, null));
        when(userService.selectUserByIdForUpdate(88L)).thenReturn(user(88L, "zhangsan"));
        when(employeeMapper.bindUserIfUnbound(1L, 88L)).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.bindUser(1L, 88L));
    }

    @Test
    void unbindShouldSucceedAndRepeatedUnbindShouldBeIdempotent() {
        EmployeeVo bound = activeEmployee(1L, 88L);
        bound.setUserName("zhangsan");
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(bound);
        when(employeeMapper.unbindUser(1L, 88L)).thenReturn(1);
        assertTrue(service.unbindUser(1L));

        when(employeeMapper.selectEmployeeById(1L)).thenReturn(activeEmployee(1L, null));
        assertTrue(service.unbindUser(1L));
    }

    @Test
    void userOptionsShouldReuseSystemScopedPageAndMarkOccupiedAccounts() {
        SysUserVo available = user(88L, "zhangsan");
        SysUserVo occupied = user(99L, "lisi");
        when(userService.selectPageUserList(any(), any())).thenReturn(PageResult.build(List.of(available, occupied), 2));
        when(employeeMapper.selectBoundUserIds(List.of(88L, 99L))).thenReturn(java.util.Set.of(99L));

        var result = service.selectUserOptions(new org.dromara.hr.employee.domain.bo.EmployeeUserOptionQuery(), new PageQuery(10, 1));
        assertEquals(2L, result.getTotal());
        assertEquals(List.of(false, true), result.getRows().stream().map(option -> option.getBound()).toList());
    }

    @Test
    void changeStatusShouldUseProtectedDetailAndUpdateEntrances() {
        EmployeeVo current = new EmployeeVo();
        current.setId(1L);
        when(employeeMapper.selectEmployeeById(1L)).thenReturn(current);
        when(employeeMapper.updateById(any(Employee.class))).thenReturn(1);
        assertTrue(service.changeStatus(1L, "1"));
    }

    @Test
    void boundEmployeeCannotBeDeleted() {
        EmployeeVo employee = new EmployeeVo();
        employee.setId(1L);
        employee.setUserId(88L);
        when(employeeMapper.selectEmployeesByIds(any())).thenReturn(List.of(employee));
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
        verify(employeeMapper, never()).deleteEmployeesByIds(any());
    }

    @Test
    void unboundEmployeeCanBeDeleted() {
        EmployeeVo employee = new EmployeeVo();
        employee.setId(1L);
        when(employeeMapper.selectEmployeesByIds(any())).thenReturn(List.of(employee));
        when(employeeMapper.deleteEmployeesByIds(any())).thenReturn(1);
        assertTrue(service.deleteWithValidByIds(List.of(1L)));
    }

    @Test
    void externallyReferencedEmployeeCannotBeDeleted() {
        EmployeeVo employee = new EmployeeVo();
        employee.setId(1L);
        when(employeeMapper.selectEmployeesByIds(any())).thenReturn(List.of(employee));
        EmployeeReferenceChecker checker = ids -> Optional.of("员工存在项目成员历史关系，不能删除");
        when(referenceCheckers.iterator()).thenReturn(List.of(checker).iterator());

        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
        verify(employeeMapper, never()).deleteEmployeesByIds(any());
    }

    @Test
    void optionsShouldUseProtectedMapperEntry() {
        service.selectEmployeeOptions();
        verify(employeeMapper).selectEmployeeOptions();
    }

    @Test
    void postOptionsShouldCheckDepartmentScope() {
        service.selectPostOptions(100L);
        verify(deptService).checkDeptDataScope(100L);
        verify(postService).selectPostList(any());
    }

    private EmployeeBo validBo() {
        EmployeeBo bo = new EmployeeBo();
        bo.setEmployeeNo(" E001 ");
        bo.setEmployeeName(" 张三 ");
        bo.setDeptId(100L);
        bo.setGender("0");
        bo.setIdType("ID_CARD");
        bo.setEmploymentType("FORMAL");
        bo.setEmploymentStatus("ACTIVE");
        bo.setStatus("0");
        return bo;
    }

    private EmployeeVo activeEmployee(Long id, Long userId) {
        EmployeeVo employee = new EmployeeVo();
        employee.setId(id);
        employee.setEmployeeNo("E001");
        employee.setEmployeeName("张三");
        employee.setEmploymentStatus("ACTIVE");
        employee.setStatus("0");
        employee.setUserId(userId);
        return employee;
    }

    private SysUserVo user(Long userId, String userName) {
        SysUserVo user = new SysUserVo();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setNickName(userName);
        user.setStatus("0");
        return user;
    }
}
