package org.dromara.warehouse.context.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.dromara.warehouse.location.controller.WarehouseLocationMaintainContextOptionController;
import org.dromara.warehouse.material.category.controller.MaterialCategoryMaintainContextOptionController;
import org.dromara.warehouse.material.controller.MaterialMaintainContextOptionController;
import org.dromara.warehouse.zone.controller.WarehouseZoneMaintainContextOptionController;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WarehouseMasterDataMaintainContextControllerTest {

    @Test
    void everyEndpointUsesOnlyItsOwnAddOrEditPermissions() {
        assertController(WarehouseZoneMaintainContextOptionController.class, "warehouse:zone");
        assertController(WarehouseLocationMaintainContextOptionController.class, "warehouse:location");
        assertController(MaterialCategoryMaintainContextOptionController.class, "warehouse:materialCategory");
        assertController(MaterialMaintainContextOptionController.class, "warehouse:material");
    }

    private static void assertController(Class<?> type, String permissionPrefix) {
        for (Method method : type.getDeclaredMethods()) {
            SaCheckPermission permission = AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class);
            assertNotNull(permission, type.getSimpleName() + "." + method.getName());
            assertEquals(Set.of(permissionPrefix + ":add", permissionPrefix + ":edit"),
                Set.copyOf(Arrays.asList(permission.value())));
            assertEquals(SaMode.OR, permission.mode());
        }
    }
}
