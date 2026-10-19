package org.dromara.property.resident.context;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.dromara.property.resident.context.controller.ResidentContextOptionController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ResidentContextOptionControllerPermissionTest {

    @Test
    void queryRoutesUseResidentReadPermissionsWithOrSemantics() {
        for (String name : new String[]{"buildings", "units", "rooms"}) {
            Method method = Arrays.stream(ResidentContextOptionController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name)).findFirst().orElseThrow();
            SaCheckPermission annotation = method.getAnnotation(SaCheckPermission.class);
            assertArrayEquals(new String[]{"property:resident:list", "property:resident:query"}, annotation.value());
            assertEquals(SaMode.OR, annotation.mode());
        }
    }
}
