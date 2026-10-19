package org.dromara.warehouse.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.location.controller.WarehouseLocationController;
import org.dromara.warehouse.inventory.controller.InventoryController;
import org.dromara.warehouse.inventory.transaction.controller.InventoryTransactionController;
import org.dromara.warehouse.inbound.controller.InboundOrderController;
import org.dromara.warehouse.inbound.domain.bo.InboundOrderBo;
import org.dromara.warehouse.outbound.controller.OutboundOrderController;
import org.dromara.warehouse.outbound.domain.bo.OutboundOrderBo;
import org.dromara.warehouse.transfer.controller.TransferOrderController;
import org.dromara.warehouse.transfer.domain.bo.TransferOrderBo;
import org.dromara.warehouse.stocktake.controller.StocktakeOrderController;
import org.dromara.warehouse.stocktake.domain.bo.StocktakeOrderBo;
import org.dromara.warehouse.inventory.domain.bo.InventoryBo;
import org.dromara.warehouse.inventory.transaction.domain.bo.InventoryTransactionBo;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.warehouse.location.domain.bo.WarehouseLocationBo;
import org.dromara.warehouse.material.category.controller.MaterialCategoryController;
import org.dromara.warehouse.material.controller.MaterialController;
import org.dromara.warehouse.unit.controller.UnitController;
import org.dromara.warehouse.warehouse.controller.WarehouseController;
import org.dromara.warehouse.warehouse.domain.bo.WarehouseBo;
import org.dromara.warehouse.zone.controller.WarehouseZoneController;
import org.dromara.warehouse.zone.domain.bo.WarehouseZoneBo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Warehouse 接口权限契约测试。 */
class WarehouseControllerPermissionTest {
    @Test void warehouseEntrancesHavePermissions() throws Exception {
        assertPermission(WarehouseController.class, "list", "warehouse:warehouse:list", WarehouseBo.class, PageQuery.class);
        assertPermission(WarehouseController.class, "getInfo", "warehouse:warehouse:query", Long.class);
        assertPermission(WarehouseController.class, "add", "warehouse:warehouse:add", WarehouseBo.class);
        assertPermission(WarehouseController.class, "edit", "warehouse:warehouse:edit", WarehouseBo.class);
        assertPermission(WarehouseController.class, "remove", "warehouse:warehouse:remove", Long[].class);
        assertPermission(WarehouseController.class, "options", "warehouse:warehouse:list");
        assertPermission(WarehouseController.class, "projectOptions", "warehouse:warehouse:list");
    }
    @Test void zoneAndLocationOptionsUseListPermission() throws Exception {
        assertPermission(WarehouseZoneController.class, "options", "warehouse:zone:list", Long.class);
        assertPermission(WarehouseLocationController.class, "options", "warehouse:location:list", Long.class, Long.class);
    }
    @Test void materialMasterDataOptionsUseListPermission() throws Exception {
        assertPermission(MaterialCategoryController.class, "options", "warehouse:materialCategory:list", Long.class);
        assertPermission(UnitController.class, "options", "warehouse:unit:list");
        assertPermission(MaterialController.class, "options", "warehouse:material:list", Long.class);
        assertPermission(MaterialController.class, "companyOptions", "warehouse:material:list");
    }
    @Test void inventoryOnlyExposesReadApis() throws Exception {
        assertPermission(InventoryController.class, "list", "warehouse:inventory:list", InventoryBo.class, PageQuery.class);
        assertPermission(InventoryController.class, "summary", "warehouse:inventory:list", InventoryBo.class, PageQuery.class);
        assertPermission(InventoryController.class, "getInfo", "warehouse:inventory:query", Long.class);
        assertFalse(java.util.Arrays.stream(InventoryController.class.getDeclaredMethods())
            .anyMatch(method -> java.util.Set.of("add", "edit", "remove").contains(method.getName())));
    }
    @Test void inventoryTransactionOnlyExposesReadApis() throws Exception {
        assertPermission(InventoryTransactionController.class, "list", "warehouse:inventoryTransaction:list", InventoryTransactionBo.class, PageQuery.class);
        assertPermission(InventoryTransactionController.class, "getInfo", "warehouse:inventoryTransaction:query", Long.class);
        assertPermission(InventoryTransactionController.class, "export", "warehouse:inventoryTransaction:export", InventoryTransactionBo.class, HttpServletResponse.class);
        assertFalse(java.util.Arrays.stream(InventoryTransactionController.class.getDeclaredMethods())
            .anyMatch(method -> java.util.Set.of("add", "edit", "remove").contains(method.getName())));
    }
    @Test void inboundActionsHaveIndependentPermissions() throws Exception {
        assertPermission(InboundOrderController.class, "list", "warehouse:inbound:list", InboundOrderBo.class, PageQuery.class);
        assertPermission(InboundOrderController.class, "getInfo", "warehouse:inbound:query", Long.class);
        assertPermission(InboundOrderController.class, "add", "warehouse:inbound:add", InboundOrderBo.class);
        assertPermission(InboundOrderController.class, "edit", "warehouse:inbound:edit", InboundOrderBo.class);
        assertPermission(InboundOrderController.class, "confirm", "warehouse:inbound:confirm", Long.class);
        assertPermission(InboundOrderController.class, "complete", "warehouse:inbound:complete", Long.class);
        assertPermission(InboundOrderController.class, "cancel", "warehouse:inbound:cancel", Long.class);
        assertPermission(InboundOrderController.class, "remove", "warehouse:inbound:remove", Long[].class);
    }
    @Test void outboundActionsHaveIndependentPermissions() throws Exception {
        assertPermission(OutboundOrderController.class,"list","warehouse:outbound:list",OutboundOrderBo.class,PageQuery.class);
        assertPermission(OutboundOrderController.class,"getInfo","warehouse:outbound:query",Long.class);
        assertPermission(OutboundOrderController.class,"add","warehouse:outbound:add",OutboundOrderBo.class);
        assertPermission(OutboundOrderController.class,"edit","warehouse:outbound:edit",OutboundOrderBo.class);
        assertPermission(OutboundOrderController.class,"confirm","warehouse:outbound:confirm",Long.class);
        assertPermission(OutboundOrderController.class,"complete","warehouse:outbound:complete",Long.class);
        assertPermission(OutboundOrderController.class,"cancel","warehouse:outbound:cancel",Long.class);
        assertPermission(OutboundOrderController.class,"remove","warehouse:outbound:remove",Long[].class);
    }
    @Test void transferActionsHaveIndependentPermissions() throws Exception {
        assertPermission(TransferOrderController.class,"list","warehouse:transfer:list",TransferOrderBo.class,PageQuery.class);
        assertPermission(TransferOrderController.class,"getInfo","warehouse:transfer:query",Long.class);
        assertPermission(TransferOrderController.class,"add","warehouse:transfer:add",TransferOrderBo.class);
        assertPermission(TransferOrderController.class,"edit","warehouse:transfer:edit",TransferOrderBo.class);
        assertPermission(TransferOrderController.class,"confirm","warehouse:transfer:confirm",Long.class);
        assertPermission(TransferOrderController.class,"complete","warehouse:transfer:complete",Long.class);
        assertPermission(TransferOrderController.class,"cancel","warehouse:transfer:cancel",Long.class);
        assertPermission(TransferOrderController.class,"remove","warehouse:transfer:remove",Long[].class);
    }
    @Test void stocktakeActionsHaveIndependentPermissions() throws Exception {
        assertPermission(StocktakeOrderController.class,"list","warehouse:stocktake:list",StocktakeOrderBo.class,PageQuery.class);
        assertPermission(StocktakeOrderController.class,"export","warehouse:stocktake:export",StocktakeOrderBo.class,HttpServletResponse.class);
        assertPermission(StocktakeOrderController.class,"getInfo","warehouse:stocktake:query",Long.class);
        assertPermission(StocktakeOrderController.class,"add","warehouse:stocktake:add",StocktakeOrderBo.class);
        assertPermission(StocktakeOrderController.class,"edit","warehouse:stocktake:edit",StocktakeOrderBo.class);
        assertPermission(StocktakeOrderController.class,"confirm","warehouse:stocktake:confirm",Long.class);
        assertPermission(StocktakeOrderController.class,"complete","warehouse:stocktake:complete",Long.class);
        assertPermission(StocktakeOrderController.class,"cancel","warehouse:stocktake:cancel",Long.class);
        assertPermission(StocktakeOrderController.class,"remove","warehouse:stocktake:remove",Long[].class);
    }
    private void assertPermission(Class<?> type, String name, String permission, Class<?>... params) throws Exception {
        Method method = type.getDeclaredMethod(name, params);
        assertArrayEquals(new String[]{permission}, method.getAnnotation(SaCheckPermission.class).value());
    }
}
