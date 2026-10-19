package org.dromara.warehouse.inbound.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InboundContextOptionControllerTest {

    @Test
    void everyContextEndpointUsesExistingInboundMaintainPermissionsWithOrSemantics() {
        for (Method method : InboundContextOptionController.class.getDeclaredMethods()) {
            SaCheckPermission permission = AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class);
            assertNotNull(permission, method.getName());
            assertEquals(Set.of("warehouse:inbound:add", "warehouse:inbound:edit"),
                Set.copyOf(Arrays.asList(permission.value())), method.getName());
            assertEquals(SaMode.OR, permission.mode(), method.getName());
        }
    }

    @Test
    void createAndEditOperationsKeepTheirOwnPermissions() throws Exception {
        SaCheckPermission add = InboundOrderController.class
            .getDeclaredMethod("add", org.dromara.warehouse.inbound.domain.bo.InboundOrderBo.class)
            .getAnnotation(SaCheckPermission.class);
        SaCheckPermission edit = InboundOrderController.class
            .getDeclaredMethod("edit", org.dromara.warehouse.inbound.domain.bo.InboundOrderBo.class)
            .getAnnotation(SaCheckPermission.class);

        assertEquals(Set.of("warehouse:inbound:add"), Set.copyOf(Arrays.asList(add.value())));
        assertEquals(Set.of("warehouse:inbound:edit"), Set.copyOf(Arrays.asList(edit.value())));
    }
}
