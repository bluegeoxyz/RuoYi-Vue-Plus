package org.dromara.hr.employee.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/** 员工账号绑定入口权限契约测试。 */
class EmployeeControllerPermissionTest {

    @Test
    void accountBindingEntrancesShouldUseDedicatedPermission() throws Exception {
        assertPermission("bindUser", Long.class, org.dromara.hr.employee.domain.bo.EmployeeBindUserBo.class);
        assertPermission("unbindUser", Long.class);
        assertPermission("userOptions", org.dromara.hr.employee.domain.bo.EmployeeUserOptionQuery.class,
            org.dromara.common.mybatis.core.page.PageQuery.class);
    }

    private void assertPermission(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = EmployeeController.class.getDeclaredMethod(methodName, parameterTypes);
        assertArrayEquals(new String[]{"hr:employee:bindUser"}, method.getAnnotation(SaCheckPermission.class).value());
    }
}
