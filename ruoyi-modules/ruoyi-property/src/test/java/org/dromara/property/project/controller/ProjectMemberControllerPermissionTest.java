package org.dromara.property.project.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.domain.bo.ProjectMemberBo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/** 项目成员接口权限契约测试。 */
class ProjectMemberControllerPermissionTest {

    @Test
    void entrancesShouldUseSpecifiedPermissions() throws Exception {
        assertPermission("list", "property:projectMember:list", ProjectMemberBo.class, PageQuery.class);
        assertPermission("export", "property:projectMember:export", ProjectMemberBo.class, HttpServletResponse.class);
        assertPermission("getInfo", "property:projectMember:query", Long.class);
        assertPermission("add", "property:projectMember:add", ProjectMemberBo.class);
        assertPermission("edit", "property:projectMember:edit", ProjectMemberBo.class);
        assertPermission("end", "property:projectMember:remove", Long.class, ProjectMemberBo.class);
        assertPermission("setManager", "property:projectMember:edit", Long.class);
        assertPermission("cancelManager", "property:projectMember:edit", Long.class);
        assertPermission("remove", "property:projectMember:remove", Long[].class);
        assertPermission("options", "property:projectMember:list", Long.class);
    }

    private void assertPermission(String methodName, String permission, Class<?>... parameterTypes) throws Exception {
        Method method = ProjectMemberController.class.getDeclaredMethod(methodName, parameterTypes);
        assertArrayEquals(new String[]{permission}, method.getAnnotation(SaCheckPermission.class).value());
    }
}
