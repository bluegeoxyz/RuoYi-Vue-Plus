package org.dromara.property.charge.context;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.property.charge.binding.controller.ChargeBindingContextOptionController;
import org.dromara.property.charge.payment.controller.PaymentContextOptionController;
import org.dromara.property.charge.receivable.controller.ReceivableContextOptionController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FinanceContextOptionControllerPermissionTest {
    @Test
    void routesUseExistingBusinessPermissions() {
        assertPermission(ReceivableContextOptionController.class, "chargeItems", "property:receivable:list");
        for (String name : new String[]{"rooms", "customers", "receivables"}) {
            SaCheckPermission annotation = method(PaymentContextOptionController.class, name).getAnnotation(SaCheckPermission.class);
            assertArrayEquals(new String[]{"property:payment:add", "property:payment:allocate"}, annotation.value());
        }
        assertPermission(ChargeBindingContextOptionController.class, "createItems", "property:chargeBinding:add");
        assertPermission(ChargeBindingContextOptionController.class, "createRules", "property:chargeBinding:add");
        assertPermission(ChargeBindingContextOptionController.class, "editItems", "property:chargeBinding:edit");
        assertPermission(ChargeBindingContextOptionController.class, "editRules", "property:chargeBinding:edit");
        assertPermission(ChargeBindingContextOptionController.class, "listProjects", "property:chargeBinding:list");
        assertPermission(ChargeBindingContextOptionController.class, "listBuildings", "property:chargeBinding:list");
        assertPermission(ChargeBindingContextOptionController.class, "listUnits", "property:chargeBinding:list");
        assertPermission(ChargeBindingContextOptionController.class, "listRooms", "property:chargeBinding:list");
    }

    private void assertPermission(Class<?> type, String name, String permission) {
        assertArrayEquals(new String[]{permission}, method(type, name).getAnnotation(SaCheckPermission.class).value());
    }

    private Method method(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredMethods()).filter(candidate -> candidate.getName().equals(name))
            .findFirst().orElseThrow();
    }
}
