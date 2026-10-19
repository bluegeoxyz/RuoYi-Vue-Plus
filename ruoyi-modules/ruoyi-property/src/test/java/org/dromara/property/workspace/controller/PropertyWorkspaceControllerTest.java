package org.dromara.property.workspace.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PropertyWorkspaceControllerTest {

    @Test
    void projectsShouldUseExistingBusinessPermissionsWithOrSemantics() throws Exception {
        SaCheckPermission permission = PropertyWorkspaceController.class.getMethod("projects")
            .getAnnotation(SaCheckPermission.class);

        assertNotNull(permission);
        assertEquals(SaMode.OR, permission.mode());
        Set<String> permissions = Set.of(permission.value());
        assertFalse(permissions.isEmpty());
        assertFalse(permissions.stream().anyMatch(value -> value.startsWith("warehouse:")));
        assertFalse(permissions.stream().anyMatch(value -> value.startsWith("hr:")));
        assertFalse(permissions.stream().anyMatch(value -> value.equals("property:project")));
    }
}
