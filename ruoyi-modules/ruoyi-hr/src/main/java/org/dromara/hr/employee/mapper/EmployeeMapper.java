package org.dromara.hr.employee.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.common.mybatis.core.query.LambdaJoinQueryBuilder;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.hr.employee.domain.Employee;
import org.dromara.hr.employee.domain.bo.EmployeeBo;
import org.dromara.hr.employee.domain.vo.EmployeeOptionVo;
import org.dromara.hr.employee.domain.vo.EmployeeVo;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.SysPost;
import org.dromara.system.domain.SysUser;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 员工档案数据层。所有业务读取入口均启用部门数据权限。
 */
public interface EmployeeMapper extends BaseMapperPlus<Employee, EmployeeVo>, MPJBaseMapper<Employee> {

    @DataPermission({
        @DataColumn(key = "deptName", value = "e.dept_id"),
        @DataColumn(key = "userName", value = "e.create_by")
    })
    default Page<EmployeeVo> selectEmployeePage(Page<EmployeeVo> page, EmployeeBo bo, List<Long> deptIds) {
        return this.selectJoinPage(page, EmployeeVo.class, buildJoinWrapper(bo, deptIds));
    }

    @DataPermission({
        @DataColumn(key = "deptName", value = "e.dept_id"),
        @DataColumn(key = "userName", value = "e.create_by")
    })
    default List<EmployeeVo> selectEmployeeList(EmployeeBo bo, List<Long> deptIds) {
        return this.selectJoinList(EmployeeVo.class, buildJoinWrapper(bo, deptIds));
    }

    @DataPermission({
        @DataColumn(key = "deptName", value = "e.dept_id"),
        @DataColumn(key = "userName", value = "e.create_by")
    })
    default EmployeeVo selectEmployeeById(Long id) {
        return this.selectJoinOne(EmployeeVo.class, buildBaseJoinBuilder().eq("e", Employee::getId, id).build());
    }

    @DataPermission({
        @DataColumn(key = "deptName", value = "e.dept_id"),
        @DataColumn(key = "userName", value = "e.create_by")
    })
    default List<EmployeeVo> selectEmployeesByIds(Collection<Long> ids) {
        return this.selectJoinList(EmployeeVo.class, buildBaseJoinBuilder().in("e", Employee::getId, ids).build());
    }

    @DataPermission({
        @DataColumn(key = "deptName", value = "e.dept_id"),
        @DataColumn(key = "userName", value = "e.create_by")
    })
    default List<EmployeeOptionVo> selectEmployeeOptions() {
        MPJLambdaWrapper<Employee> wrapper = QueryBuilder.lambdaJoin("e", Employee.class)
            .select(Employee::getId, Employee::getEmployeeNo, Employee::getEmployeeName, Employee::getDeptId)
            .selectAs("d", SysDept::getDeptName, EmployeeOptionVo::getDeptName)
            .selectAs("p", SysPost::getPostName, EmployeeOptionVo::getPostName)
            .leftJoin(SysDept.class, "d", SysDept::getDeptId, Employee::getDeptId)
            .leftJoin(SysPost.class, "p", SysPost::getPostId, Employee::getPostId)
            .eq("e", Employee::getEmploymentStatus, "ACTIVE")
            .eq("e", Employee::getStatus, "0")
            .orderByAsc("e", Employee::getSort)
            .orderByAsc("e", Employee::getId)
            .build();
        return this.selectJoinList(EmployeeOptionVo.class, wrapper);
    }

    @DataPermission({
        @DataColumn(key = "deptName", value = "e.dept_id"),
        @DataColumn(key = "userName", value = "e.create_by")
    })
    default List<EmployeeOptionVo> selectEmployeeOptionsByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        MPJLambdaWrapper<Employee> wrapper = QueryBuilder.lambdaJoin("e", Employee.class)
            .select(Employee::getId, Employee::getEmployeeNo, Employee::getEmployeeName, Employee::getDeptId)
            .selectAs("d", SysDept::getDeptName, EmployeeOptionVo::getDeptName)
            .selectAs("p", SysPost::getPostName, EmployeeOptionVo::getPostName)
            .leftJoin(SysDept.class, "d", SysDept::getDeptId, Employee::getDeptId)
            .leftJoin(SysPost.class, "p", SysPost::getPostId, Employee::getPostId)
            .in("e", Employee::getId, ids)
            .build();
        return this.selectJoinList(EmployeeOptionVo.class, wrapper);
    }

    default long countByEmployeeNo(String employeeNo, Long excludeId) {
        return lambda()
            .eq(Employee::getEmployeeNo, employeeNo)
            .ne(excludeId != null, Employee::getId, excludeId)
            .count();
    }

    /** 全局查询账号占用者；不附加员工数据权限，以保证一对一业务约束。 */
    default Employee selectByUserId(Long userId) {
        return lambda().eq(Employee::getUserId, userId).one();
    }

    /** Property 范围服务使用的绑定查询，不附加部门数据权限。 */
    default Long selectActiveEmployeeIdByUserId(Long userId) {
        Employee employee = lambda().select(Employee::getId).eq(Employee::getUserId, userId)
            .eq(Employee::getEmploymentStatus, "ACTIVE").eq(Employee::getStatus, "0").one();
        return employee == null ? null : employee.getId();
    }

    /** 全局查询候选账号的占用情况；仅用于返回不可绑定标记。 */
    default Set<Long> selectBoundUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Set.of();
        }
        return lambda()
            .select(Employee::getUserId)
            .in(Employee::getUserId, userIds)
            .list()
            .stream()
            .map(Employee::getUserId)
            .collect(Collectors.toSet());
    }

    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default int bindUserIfUnbound(Long employeeId, Long userId) {
        return lambda()
            .set(Employee::getUserId, userId)
            .eq(Employee::getId, employeeId)
            .isNull(Employee::getUserId)
            .updateCount();
    }

    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default int unbindUser(Long employeeId, Long userId) {
        return lambda()
            .set(Employee::getUserId, null)
            .eq(Employee::getId, employeeId)
            .eq(Employee::getUserId, userId)
            .updateCount();
    }

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    int update(@Param(Constants.ENTITY) Employee employee,
               @Param(Constants.WRAPPER) Wrapper<Employee> updateWrapper);

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    int updateById(@Param(Constants.ENTITY) Employee employee);

    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default int deleteEmployeesByIds(Collection<Long> ids) {
        return this.deleteByIds(ids);
    }

    private MPJLambdaWrapper<Employee> buildJoinWrapper(EmployeeBo bo, List<Long> deptIds) {
        return buildBaseJoinBuilder()
            .likeIfText("e", Employee::getEmployeeNo, bo.getEmployeeNo())
            .likeIfText("e", Employee::getEmployeeName, bo.getEmployeeName())
            .inIfNotEmpty("e", Employee::getDeptId, deptIds)
            .eq(bo.getPostId() != null, "e", Employee::getPostId, bo.getPostId())
            .eqIfText("e", Employee::getEmploymentType, bo.getEmploymentType())
            .eqIfText("e", Employee::getEmploymentStatus, bo.getEmploymentStatus())
            .eqIfText("e", Employee::getStatus, bo.getStatus())
            .likeIfText("e", Employee::getMobile, bo.getMobile())
            .betweenParams("e", Employee::getCreateTime, bo.getParams(), "beginTime", "endTime")
            .orderByAsc("e", Employee::getSort)
            .orderByDesc("e", Employee::getId)
            .build();
    }

    private LambdaJoinQueryBuilder<Employee> buildBaseJoinBuilder() {
        return QueryBuilder.lambdaJoin("e", Employee.class)
            .selectAll(Employee.class)
            .selectAs("d", SysDept::getDeptName, EmployeeVo::getDeptName)
            .selectAs("p", SysPost::getPostName, EmployeeVo::getPostName)
            .selectAs("u", SysUser::getUserName, EmployeeVo::getUserName)
            .selectAs("u", SysUser::getNickName, EmployeeVo::getNickName)
            .selectAs("u", SysUser::getStatus, EmployeeVo::getUserStatus)
            .leftJoin(SysDept.class, "d", SysDept::getDeptId, Employee::getDeptId)
            .leftJoin(SysPost.class, "p", SysPost::getPostId, Employee::getPostId)
            .leftJoin(SysUser.class, "u", SysUser::getUserId, Employee::getUserId);
    }
}
