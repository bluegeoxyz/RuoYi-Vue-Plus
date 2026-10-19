package org.dromara.hr.employee.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.hr.employee.domain.Employee;
import org.dromara.hr.employee.domain.bo.EmployeeBo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Employee Mapper 数据权限入口契约测试。 */
class EmployeeMapperDataPermissionTest {

    @Test
    void allBusinessReadAndWriteEntrancesMustDeclareDataPermission() throws Exception {
        assertProtected("selectEmployeePage", com.baomidou.mybatisplus.extension.plugins.pagination.Page.class,
            EmployeeBo.class, List.class);
        assertProtected("selectEmployeeList", EmployeeBo.class, List.class);
        assertProtected("selectEmployeeById", Long.class);
        assertProtected("selectEmployeesByIds", Collection.class);
        assertProtected("selectEmployeeOptions");
        assertProtected("selectEmployeeOptionsByIds", Collection.class);
        assertProtected("update", Employee.class, Wrapper.class);
        assertProtected("updateById", Employee.class);
        assertProtected("bindUserIfUnbound", Long.class, Long.class);
        assertProtected("unbindUser", Long.class, Long.class);
        assertProtected("deleteEmployeesByIds", Collection.class);
    }

    private void assertProtected(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = EmployeeMapper.class.getMethod(methodName, parameterTypes);
        assertNotNull(method.getAnnotation(DataPermission.class), methodName + " 必须启用数据权限");
    }
}
