package org.dromara.warehouse.context.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.dromara.warehouse.location.controller.WarehouseLocationQueryContextOptionController;
import org.dromara.warehouse.material.category.controller.MaterialCategoryQueryContextOptionController;
import org.dromara.warehouse.material.controller.MaterialQueryContextOptionController;
import org.dromara.warehouse.zone.controller.WarehouseZoneQueryContextOptionController;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WarehouseMasterDataQueryContextControllerTest {

    @Test
    void everyEndpointUsesOnlyItsOwnReadPermissions() {
        assertController(WarehouseZoneQueryContextOptionController.class, "warehouse:zone");
        assertController(WarehouseLocationQueryContextOptionController.class, "warehouse:location");
        assertController(MaterialCategoryQueryContextOptionController.class, "warehouse:materialCategory");
        assertController(MaterialQueryContextOptionController.class, "warehouse:material");
    }

    private static void assertController(Class<?> type, String permissionPrefix) {
        for (Method method : type.getDeclaredMethods()) {
            SaCheckPermission permission = AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class);
            assertNotNull(permission, type.getSimpleName() + "." + method.getName());
            assertEquals(Set.of(permissionPrefix + ":list", permissionPrefix + ":query"),
                Set.copyOf(Arrays.asList(permission.value())));
            assertEquals(SaMode.OR, permission.mode());
        }
    }
}
