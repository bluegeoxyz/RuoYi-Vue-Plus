package org.dromara.property.customer.relation.context.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CustomerRelationContextOptionControllerPermissionTest {

    @Test
    void listAndCreateRoutesKeepIndependentPermissions() throws Exception {
        for (String method : new String[]{"listProjects", "listBuildings", "listUnits", "listRooms"}) {
            assertPermission(method, "property:customerRelation:list");
        }
        for (String method : new String[]{"createProjects", "createBuildings", "createUnits", "createRooms", "createCustomers"}) {
            assertPermission(method, "property:customerRelation:add");
        }
    }

    private void assertPermission(String methodName, String permission) throws Exception {
        Method method = java.util.Arrays.stream(CustomerRelationContextOptionController.class.getDeclaredMethods())
            .filter(candidate -> candidate.getName().equals(methodName)).findFirst().orElseThrow();
        assertArrayEquals(new String[]{permission}, method.getAnnotation(SaCheckPermission.class).value());
    }
}
