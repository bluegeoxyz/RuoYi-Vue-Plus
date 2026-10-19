package org.dromara.property.project.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.domain.bo.ProjectBo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/** 物业项目接口权限契约测试。 */
class ProjectControllerPermissionTest {

    @Test
    void entrancesShouldUseSpecifiedPermissions() throws Exception {
        assertPermission("list", "property:project:list", ProjectBo.class, PageQuery.class);
        assertPermission("export", "property:project:export", ProjectBo.class, HttpServletResponse.class);
        assertPermission("getInfo", "property:project:query", Long.class);
        assertPermission("add", "property:project:add", ProjectBo.class);
        assertPermission("edit", "property:project:edit", ProjectBo.class);
        assertPermission("changeStatus", "property:project:edit", ProjectBo.class);
        assertPermission("remove", "property:project:remove", Long[].class);
        assertPermission("options", "property:project:list");
    }

    private void assertPermission(String methodName, String permission, Class<?>... parameterTypes) throws Exception {
        Method method = ProjectController.class.getDeclaredMethod(methodName, parameterTypes);
        assertArrayEquals(new String[]{permission}, method.getAnnotation(SaCheckPermission.class).value());
    }
}
