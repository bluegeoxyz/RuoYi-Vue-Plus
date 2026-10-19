package org.dromara.warehouse.context.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.warehouse.inbound.controller.InboundOrderController;
import org.dromara.warehouse.location.controller.WarehouseLocationController;
import org.dromara.warehouse.material.category.controller.MaterialCategoryController;
import org.dromara.warehouse.material.controller.MaterialController;
import org.dromara.warehouse.outbound.controller.OutboundOrderController;
import org.dromara.warehouse.stocktake.controller.StocktakeOrderController;
import org.dromara.warehouse.transfer.controller.TransferOrderController;
import org.dromara.warehouse.unit.controller.UnitController;
import org.dromara.warehouse.warehouse.controller.WarehouseController;
import org.dromara.warehouse.zone.controller.WarehouseZoneController;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WarehouseEditInitializationControllerTest {

    @Test
    void everyEditContextUsesOnlyItsModuleEditPermission() throws NoSuchMethodException {
        Map<Class<?>, String> controllers = Map.of(
            WarehouseController.class, "warehouse:warehouse:edit",
            WarehouseZoneController.class, "warehouse:zone:edit",
            WarehouseLocationController.class, "warehouse:location:edit",
            MaterialCategoryController.class, "warehouse:materialCategory:edit",
            UnitController.class, "warehouse:unit:edit",
            MaterialController.class, "warehouse:material:edit",
            InboundOrderController.class, "warehouse:inbound:edit",
            OutboundOrderController.class, "warehouse:outbound:edit",
            TransferOrderController.class, "warehouse:transfer:edit",
            StocktakeOrderController.class, "warehouse:stocktake:edit");
        for (Map.Entry<Class<?>, String> entry : controllers.entrySet()) {
            Method method = entry.getKey().getDeclaredMethod("editContext", Long.class);
            SaCheckPermission permission = AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class);
            assertNotNull(permission);
            assertArrayEquals(new String[]{entry.getValue()}, permission.value());
        }
    }

    @Test
    void originalDetailEndpointsRemainQueryOnly() throws NoSuchMethodException {
        Map<Class<?>, String> controllers = Map.of(
            WarehouseController.class, "warehouse:warehouse:query",
            WarehouseZoneController.class, "warehouse:zone:query",
            WarehouseLocationController.class, "warehouse:location:query",
            MaterialCategoryController.class, "warehouse:materialCategory:query",
            UnitController.class, "warehouse:unit:query",
            MaterialController.class, "warehouse:material:query",
            InboundOrderController.class, "warehouse:inbound:query",
            OutboundOrderController.class, "warehouse:outbound:query",
            TransferOrderController.class, "warehouse:transfer:query",
            StocktakeOrderController.class, "warehouse:stocktake:query");
        for (Map.Entry<Class<?>, String> entry : controllers.entrySet()) {
            Method method = entry.getKey().getDeclaredMethod("getInfo", Long.class);
            SaCheckPermission permission = AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class);
            assertNotNull(permission);
            assertArrayEquals(new String[]{entry.getValue()}, permission.value());
        }
    }
}
