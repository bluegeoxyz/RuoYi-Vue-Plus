package org.dromara.warehouse.context.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.dromara.warehouse.outbound.controller.OutboundContextOptionController;
import org.dromara.warehouse.outbound.controller.OutboundQueryContextOptionController;
import org.dromara.warehouse.outbound.controller.OutboundOrderController;
import org.dromara.warehouse.inbound.controller.InboundQueryContextOptionController;
import org.dromara.warehouse.stocktake.controller.StocktakeContextOptionController;
import org.dromara.warehouse.stocktake.controller.StocktakeQueryContextOptionController;
import org.dromara.warehouse.stocktake.controller.StocktakeOrderController;
import org.dromara.warehouse.transfer.controller.TransferContextOptionController;
import org.dromara.warehouse.transfer.controller.TransferQueryContextOptionController;
import org.dromara.warehouse.transfer.controller.TransferOrderController;
import org.dromara.warehouse.inventory.controller.InventoryContextOptionController;
import org.dromara.warehouse.inventory.transaction.controller.InventoryTransactionContextOptionController;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WarehouseDocumentContextOptionControllerTest {
    @Test
    void everyContextEndpointUsesOnlyItsExistingMaintainPermissions() {
        assertContextController(OutboundContextOptionController.class, "warehouse:outbound:add", "warehouse:outbound:edit");
        assertContextController(TransferContextOptionController.class, "warehouse:transfer:add", "warehouse:transfer:edit");
        assertContextController(StocktakeContextOptionController.class, "warehouse:stocktake:add", "warehouse:stocktake:edit");
    }

    @Test
    void createAndEditOperationsRemainIndependentlyProtected() throws Exception {
        assertWritePermissions(OutboundOrderController.class, org.dromara.warehouse.outbound.domain.bo.OutboundOrderBo.class, "warehouse:outbound");
        assertWritePermissions(TransferOrderController.class, org.dromara.warehouse.transfer.domain.bo.TransferOrderBo.class, "warehouse:transfer");
        assertWritePermissions(StocktakeOrderController.class, org.dromara.warehouse.stocktake.domain.bo.StocktakeOrderBo.class, "warehouse:stocktake");
    }

    @Test
    void inventoryReadContextEndpointsUseOnlyTheirOwnReadPermissions() {
        assertContextController(InventoryContextOptionController.class,
            "warehouse:inventory:list", "warehouse:inventory:query", 6);
        assertContextController(InventoryTransactionContextOptionController.class,
            "warehouse:inventoryTransaction:list", "warehouse:inventoryTransaction:query", 6);
    }

    @Test
    void documentReadContextEndpointsUseOnlyTheirOwnReadPermissions() {
        assertContextController(InboundQueryContextOptionController.class,
            "warehouse:inbound:list", "warehouse:inbound:query", 1);
        assertContextController(OutboundQueryContextOptionController.class,
            "warehouse:outbound:list", "warehouse:outbound:query", 1);
        assertContextController(TransferQueryContextOptionController.class,
            "warehouse:transfer:list", "warehouse:transfer:query", 1);
        assertContextController(StocktakeQueryContextOptionController.class,
            "warehouse:stocktake:list", "warehouse:stocktake:query", 1);
    }

    private static void assertContextController(Class<?> type, String add, String edit) {
        assertContextController(type, add, edit, 5);
    }

    private static void assertContextController(Class<?> type, String first, String second, int endpointCount) {
        for (Method method : type.getDeclaredMethods()) {
            SaCheckPermission permission = AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class);
            assertNotNull(permission, type.getSimpleName() + "." + method.getName());
            assertEquals(Set.of(first, second), Set.copyOf(Arrays.asList(permission.value())));
            assertEquals(SaMode.OR, permission.mode());
        }
        assertEquals(endpointCount, List.of(type.getDeclaredMethods()).size());
    }

    private static void assertWritePermissions(Class<?> controller, Class<?> boType, String prefix) throws Exception {
        SaCheckPermission add = controller.getDeclaredMethod("add", boType).getAnnotation(SaCheckPermission.class);
        SaCheckPermission edit = controller.getDeclaredMethod("edit", boType).getAnnotation(SaCheckPermission.class);
        assertEquals(Set.of(prefix + ":add"), Set.copyOf(Arrays.asList(add.value())));
        assertEquals(Set.of(prefix + ":edit"), Set.copyOf(Arrays.asList(edit.value())));
    }
}
