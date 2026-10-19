package org.dromara.common.security.business;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class BusinessContextAccessServiceTest {

    private final BusinessContextAccessService service = new BusinessContextAccessServiceImpl();

    @Test
    void allowsDeclaredContextAndReference() {
        try (MockedStatic<StpUtil> stp = permissions("property:customerRelation:add")) {
            assertDoesNotThrow(() -> service.requireAccess(
                BusinessContext.CUSTOMER_RELATION_CREATE, ReferenceType.ROOM));
        }
    }

    @Test
    void customerRelationListAndCreateStayIndependent() {
        try (MockedStatic<StpUtil> stp = permissions("property:customerRelation:list")) {
            assertDoesNotThrow(() -> service.requireAccess(BusinessContext.CUSTOMER_RELATION_LIST, ReferenceType.ROOM));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CUSTOMER_RELATION_CREATE, ReferenceType.ROOM));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CUSTOMER_RELATION_LIST, ReferenceType.CUSTOMER));
        }
        try (MockedStatic<StpUtil> stp = permissions("property:project:list", "property:building:list",
            "property:unit:list", "property:room:list")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CUSTOMER_RELATION_LIST, ReferenceType.PROJECT));
        }
    }

    @Test
    void financeContextsRejectManagementPermissionBypassAndStayIndependent() {
        try (MockedStatic<StpUtil> stp = permissions("property:chargeItem:list", "property:chargeRule:list", "property:room:list")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.RECEIVABLE_QUERY, ReferenceType.CHARGE_ITEM));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.PAYMENT_CREATE, ReferenceType.RECEIVABLE));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CHARGE_BINDING_CREATE, ReferenceType.CHARGE_RULE));
        }
        try (MockedStatic<StpUtil> stp = permissions("property:receivable:list")) {
            assertDoesNotThrow(() -> service.requireAccess(BusinessContext.RECEIVABLE_QUERY, ReferenceType.CHARGE_ITEM));
        }
        try (MockedStatic<StpUtil> stp = permissions("property:chargeBinding:add")) {
            assertDoesNotThrow(() -> service.requireAccess(BusinessContext.CHARGE_BINDING_CREATE, ReferenceType.CHARGE_ITEM));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CHARGE_BINDING_EDIT, ReferenceType.CHARGE_ITEM));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.ROOM));
        }
        try (MockedStatic<StpUtil> stp = permissions("property:chargeBinding:list")) {
            assertDoesNotThrow(() -> service.requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.ROOM));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CHARGE_BINDING_CREATE, ReferenceType.ROOM));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.CHARGE_ITEM));
        }
    }

    @Test
    void residentQueryUsesResidentPermissionsWithoutResourceManagementPermissions() {
        try (MockedStatic<StpUtil> stp = permissions("property:resident:list")) {
            assertDoesNotThrow(() -> service.requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.BUILDING));
            assertDoesNotThrow(() -> service.requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.UNIT));
            assertDoesNotThrow(() -> service.requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.ROOM));
        }
        try (MockedStatic<StpUtil> stp = permissions("property:building:list", "property:unit:list", "property:room:list")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.BUILDING));
        }
    }

    @Test
    void rejectsUndeclaredContextAndReference() {
        try (MockedStatic<StpUtil> stp = permissions("property:customerRelation:add")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(null, ReferenceType.ROOM));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CUSTOMER_RELATION_CREATE, ReferenceType.INVENTORY));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.CUSTOMER_RELATION_CREATE, null));
        }
    }

    @Test
    void supportsAnyPermissionCombination() {
        try (MockedStatic<StpUtil> stp = permissions("warehouse:inbound:add")) {
            assertDoesNotThrow(() -> service.requireAccess(
                BusinessContext.INBOUND_MAINTAIN, ReferenceType.MATERIAL));
        }
        try (MockedStatic<StpUtil> stp = permissions("warehouse:inbound:edit")) {
            assertDoesNotThrow(() -> service.requireAccess(
                BusinessContext.INBOUND_MAINTAIN, ReferenceType.MATERIAL));
        }
    }

    @Test
    void supportsAllPermissionCombination() {
        try (MockedStatic<StpUtil> stp = permissions("property:payment:add")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.PAYMENT_CREATE, ReferenceType.RECEIVABLE));
        }
        try (MockedStatic<StpUtil> stp = permissions(
            "property:payment:add", "property:payment:allocate")) {
            assertDoesNotThrow(() -> service.requireAccess(
                BusinessContext.PAYMENT_CREATE, ReferenceType.RECEIVABLE));
        }
    }

    @Test
    void rejectsUnauthenticatedSession() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::checkLogin).thenThrow(NotLoginException.newInstance(
                "login", NotLoginException.NOT_TOKEN, "未登录", null));
            assertThrows(NotLoginException.class,
                () -> service.requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.WAREHOUSE));
        }
    }

    @Test
    void rejectsMissingBusinessPermissionAndManagementListBypass() {
        try (MockedStatic<StpUtil> stp = permissions(
            "warehouse:warehouse:list", "warehouse:material:list", "warehouse:location:list",
            "warehouse:zone:list", "warehouse:unit:list", "warehouse:inventory:list")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.WAREHOUSE));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.OUTBOUND_MAINTAIN, ReferenceType.INVENTORY));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.TRANSFER_MAINTAIN, ReferenceType.LOCATION));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.STOCKTAKE_MAINTAIN, ReferenceType.MATERIAL));
        }
    }

    @Test
    void remainingWarehouseContextsAcceptTheirOwnAddOrEditPermission() {
        for (BusinessContext context : Set.of(BusinessContext.OUTBOUND_MAINTAIN,
            BusinessContext.TRANSFER_MAINTAIN, BusinessContext.STOCKTAKE_MAINTAIN)) {
            String prefix = switch (context) {
                case OUTBOUND_MAINTAIN -> "warehouse:outbound:";
                case TRANSFER_MAINTAIN -> "warehouse:transfer:";
                case STOCKTAKE_MAINTAIN -> "warehouse:stocktake:";
                default -> throw new IllegalStateException();
            };
            try (MockedStatic<StpUtil> stp = permissions(prefix + "add")) {
                assertDoesNotThrow(() -> service.requireAccess(context, ReferenceType.INVENTORY));
            }
            try (MockedStatic<StpUtil> stp = permissions(prefix + "edit")) {
                assertDoesNotThrow(() -> service.requireAccess(context, ReferenceType.INVENTORY));
            }
        }
    }

    @Test
    void inventoryReadContextsAcceptOnlyTheirOwnReadPermissions() {
        for (String permission : Set.of("warehouse:inventory:list", "warehouse:inventory:query")) {
            try (MockedStatic<StpUtil> stp = permissions(permission)) {
                assertDoesNotThrow(() -> service.requireAccess(
                    BusinessContext.INVENTORY_QUERY, ReferenceType.MATERIAL_CATEGORY));
                assertThrows(NotPermissionException.class, () -> service.requireAccess(
                    BusinessContext.INVENTORY_TRANSACTION_QUERY, ReferenceType.WAREHOUSE));
            }
        }
        for (String permission : Set.of("warehouse:inventoryTransaction:list", "warehouse:inventoryTransaction:query")) {
            try (MockedStatic<StpUtil> stp = permissions(permission)) {
                assertDoesNotThrow(() -> service.requireAccess(
                    BusinessContext.INVENTORY_TRANSACTION_QUERY, ReferenceType.LOCATION));
                assertThrows(NotPermissionException.class, () -> service.requireAccess(
                    BusinessContext.INVENTORY_QUERY, ReferenceType.WAREHOUSE));
            }
        }
    }

    @Test
    void inventoryReadContextsRejectMasterDataPermissionBypass() {
        try (MockedStatic<StpUtil> stp = permissions(
            "warehouse:warehouse:list", "warehouse:material:list", "warehouse:materialCategory:list",
            "warehouse:zone:list", "warehouse:location:list")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.INVENTORY_QUERY, ReferenceType.WAREHOUSE));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.INVENTORY_TRANSACTION_QUERY, ReferenceType.MATERIAL));
        }
    }

    @Test
    void documentQueryContextsAcceptOnlyTheirOwnListOrQueryPermission() {
        assertDocumentQueryContext(BusinessContext.INBOUND_QUERY, "warehouse:inbound");
        assertDocumentQueryContext(BusinessContext.OUTBOUND_QUERY, "warehouse:outbound");
        assertDocumentQueryContext(BusinessContext.TRANSFER_QUERY, "warehouse:transfer");
        assertDocumentQueryContext(BusinessContext.STOCKTAKE_QUERY, "warehouse:stocktake");
    }

    @Test
    void documentQueryContextsRejectMasterDataAndMaintainPermissionBypass() {
        try (MockedStatic<StpUtil> stp = permissions("warehouse:warehouse:list")) {
            for (BusinessContext context : Set.of(BusinessContext.INBOUND_QUERY,
                BusinessContext.OUTBOUND_QUERY, BusinessContext.TRANSFER_QUERY,
                BusinessContext.STOCKTAKE_QUERY)) {
                assertThrows(NotPermissionException.class,
                    () -> service.requireAccess(context, ReferenceType.WAREHOUSE));
            }
        }
        try (MockedStatic<StpUtil> stp = permissions("warehouse:inbound:list")) {
            assertDoesNotThrow(() -> service.requireAccess(BusinessContext.INBOUND_QUERY, ReferenceType.WAREHOUSE));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.WAREHOUSE));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.INBOUND_QUERY, ReferenceType.MATERIAL));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.OUTBOUND_QUERY, ReferenceType.WAREHOUSE));
        }
    }

    @Test
    void warehouseMasterDataQueryContextsAcceptOnlyTheirOwnReadPermissions() {
        assertMasterDataQueryContext(BusinessContext.WAREHOUSE_ZONE_QUERY, "warehouse:zone", ReferenceType.WAREHOUSE);
        assertMasterDataQueryContext(BusinessContext.WAREHOUSE_LOCATION_QUERY, "warehouse:location", ReferenceType.ZONE);
        assertMasterDataQueryContext(BusinessContext.WAREHOUSE_MATERIAL_CATEGORY_QUERY,
            "warehouse:materialCategory", ReferenceType.COMPANY);
        assertMasterDataQueryContext(BusinessContext.WAREHOUSE_MATERIAL_QUERY,
            "warehouse:material", ReferenceType.MEASUREMENT_UNIT);

        try (MockedStatic<StpUtil> stp = permissions("warehouse:warehouse:list", "warehouse:unit:list")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.WAREHOUSE_ZONE_QUERY, ReferenceType.WAREHOUSE));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.WAREHOUSE_MATERIAL_QUERY, ReferenceType.MEASUREMENT_UNIT));
        }
        try (MockedStatic<StpUtil> stp = permissions("warehouse:material:list")) {
            assertDoesNotThrow(() -> service.requireAccess(
                BusinessContext.WAREHOUSE_MATERIAL_QUERY, ReferenceType.MATERIAL_CATEGORY));
            assertThrows(NotPermissionException.class, () -> service.requireAccess(
                BusinessContext.WAREHOUSE_MATERIAL_CATEGORY_QUERY, ReferenceType.COMPANY));
            assertThrows(NotPermissionException.class, () -> service.requireAccess(
                BusinessContext.WAREHOUSE_MATERIAL_QUERY, ReferenceType.WAREHOUSE));
        }
    }

    @Test
    void warehouseMasterDataMaintainContextsAcceptOnlyTheirOwnAddOrEditPermissions() {
        assertMasterDataMaintainContext(BusinessContext.WAREHOUSE_ZONE_MAINTAIN,
            "warehouse:zone", ReferenceType.WAREHOUSE);
        assertMasterDataMaintainContext(BusinessContext.WAREHOUSE_LOCATION_MAINTAIN,
            "warehouse:location", ReferenceType.ZONE);
        assertMasterDataMaintainContext(BusinessContext.WAREHOUSE_MATERIAL_CATEGORY_MAINTAIN,
            "warehouse:materialCategory", ReferenceType.MATERIAL_CATEGORY);
        assertMasterDataMaintainContext(BusinessContext.WAREHOUSE_MATERIAL_MAINTAIN,
            "warehouse:material", ReferenceType.MEASUREMENT_UNIT);

        try (MockedStatic<StpUtil> stp = permissions(
            "warehouse:zone:list", "warehouse:warehouse:list", "warehouse:unit:list")) {
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.WAREHOUSE_ZONE_MAINTAIN, ReferenceType.WAREHOUSE));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.WAREHOUSE_MATERIAL_MAINTAIN,
                    ReferenceType.MEASUREMENT_UNIT));
        }
        try (MockedStatic<StpUtil> stp = permissions("warehouse:material:add")) {
            assertDoesNotThrow(() -> service.requireAccess(
                BusinessContext.WAREHOUSE_MATERIAL_MAINTAIN, ReferenceType.MATERIAL_CATEGORY));
            assertThrows(NotPermissionException.class, () -> service.requireAccess(
                BusinessContext.WAREHOUSE_MATERIAL_CATEGORY_MAINTAIN, ReferenceType.COMPANY));
            assertThrows(NotPermissionException.class, () -> service.requireAccess(
                BusinessContext.WAREHOUSE_MATERIAL_QUERY, ReferenceType.MEASUREMENT_UNIT));
            assertThrows(NotPermissionException.class, () -> service.requireAccess(
                BusinessContext.WAREHOUSE_MATERIAL_MAINTAIN, ReferenceType.WAREHOUSE));
        }
    }

    @Test
    void contextsDoNotAuthorizeEachOther() {
        try (MockedStatic<StpUtil> stp = permissions("warehouse:outbound:add")) {
            assertDoesNotThrow(() -> service.requireAccess(
                BusinessContext.OUTBOUND_MAINTAIN, ReferenceType.INVENTORY));
            assertThrows(NotPermissionException.class,
                () -> service.requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.MATERIAL));
        }
    }

    @Test
    void everyDeclaredContextHasAnExplicitPolicy() {
        Set<BusinessContext> declared = EnumSet.allOf(BusinessContext.class);
        Set<BusinessContext> configured = EnumSet.noneOf(BusinessContext.class);
        declared.stream().filter(context -> BusinessContextPolicy.find(context).isPresent())
            .forEach(configured::add);
        assertEquals(declared, configured);
    }

    @Test
    void serviceDoesNotReplaceProjectOrWarehouseScopeServices() {
        assertEquals(0, BusinessContextAccessServiceImpl.class.getDeclaredFields().length);
        for (Field field : BusinessContextAccessServiceImpl.class.getDeclaredFields()) {
            assertFalse(field.getType().getName().contains("PropertyProjectScopeService"));
            assertFalse(field.getType().getName().contains("WarehouseAccessService"));
        }
    }

    private static MockedStatic<StpUtil> permissions(String... granted) {
        Set<String> permissionSet = Set.of(granted);
        MockedStatic<StpUtil> stp = mockStatic(StpUtil.class);
        stp.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
        stp.when(() -> StpUtil.hasPermission(org.mockito.ArgumentMatchers.anyString()))
            .thenAnswer(invocation -> permissionSet.contains(invocation.getArgument(0)));
        return stp;
    }

    private void assertDocumentQueryContext(BusinessContext context, String permissionPrefix) {
        for (String suffix : Set.of(":list", ":query")) {
            try (MockedStatic<StpUtil> stp = permissions(permissionPrefix + suffix)) {
                assertDoesNotThrow(() -> service.requireAccess(context, ReferenceType.WAREHOUSE));
            }
        }
    }

    private void assertMasterDataQueryContext(BusinessContext context, String permissionPrefix,
                                              ReferenceType referenceType) {
        for (String suffix : Set.of(":list", ":query")) {
            try (MockedStatic<StpUtil> stp = permissions(permissionPrefix + suffix)) {
                assertDoesNotThrow(() -> service.requireAccess(context, referenceType));
            }
        }
    }

    private void assertMasterDataMaintainContext(BusinessContext context, String permissionPrefix,
                                                 ReferenceType referenceType) {
        for (String suffix : Set.of(":add", ":edit")) {
            try (MockedStatic<StpUtil> stp = permissions(permissionPrefix + suffix)) {
                assertDoesNotThrow(() -> service.requireAccess(context, referenceType));
            }
        }
    }
}
