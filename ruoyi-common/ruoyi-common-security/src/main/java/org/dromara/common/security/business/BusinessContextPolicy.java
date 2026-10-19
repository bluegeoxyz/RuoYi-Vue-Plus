package org.dromara.common.security.business;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 业务上下文静态授权策略。
 *
 * @param mode 权限组合模式
 * @param permissions 当前系统已有的业务权限
 * @param referenceTypes 允许该上下文读取的引用类型
 */
public record BusinessContextPolicy(
    PermissionMode mode,
    List<String> permissions,
    Set<ReferenceType> referenceTypes
) {

    private static final Map<BusinessContext, BusinessContextPolicy> POLICIES = policies();

    public BusinessContextPolicy {
        permissions = List.copyOf(permissions);
        referenceTypes = Set.copyOf(referenceTypes);
        if (permissions.isEmpty()) {
            throw new IllegalArgumentException("业务上下文必须声明至少一个权限");
        }
    }

    /**
     * 查找业务上下文策略。未登记的上下文返回空，由调用方默认拒绝。
     *
     * @param context 业务上下文
     * @return 静态策略
     */
    public static Optional<BusinessContextPolicy> find(BusinessContext context) {
        return Optional.ofNullable(context == null ? null : POLICIES.get(context));
    }

    private static Map<BusinessContext, BusinessContextPolicy> policies() {
        Map<BusinessContext, BusinessContextPolicy> policies = new EnumMap<>(BusinessContext.class);
        policies.put(BusinessContext.CUSTOMER_RELATION_LIST, all(
            List.of("property:customerRelation:list"),
            ReferenceType.PROJECT, ReferenceType.BUILDING, ReferenceType.UNIT, ReferenceType.ROOM));
        policies.put(BusinessContext.CUSTOMER_RELATION_CREATE, all(
            List.of("property:customerRelation:add"),
            ReferenceType.PROJECT, ReferenceType.BUILDING, ReferenceType.UNIT,
            ReferenceType.ROOM, ReferenceType.CUSTOMER));
        policies.put(BusinessContext.CUSTOMER_RELATION_EDIT, all(
            List.of("property:customerRelation:edit"),
            ReferenceType.PROJECT, ReferenceType.BUILDING, ReferenceType.UNIT,
            ReferenceType.ROOM, ReferenceType.CUSTOMER));
        policies.put(BusinessContext.RESIDENT_QUERY, any(
            List.of("property:resident:list", "property:resident:query"),
            ReferenceType.BUILDING, ReferenceType.UNIT, ReferenceType.ROOM));
        policies.put(BusinessContext.RECEIVABLE_QUERY, any(
            List.of("property:receivable:list", "property:receivable:query"),
            ReferenceType.PROJECT, ReferenceType.BUILDING, ReferenceType.UNIT,
            ReferenceType.ROOM, ReferenceType.CUSTOMER, ReferenceType.RECEIVABLE,
            ReferenceType.CHARGE_ITEM));
        policies.put(BusinessContext.RECEIVABLE_GENERATE, all(
            List.of("property:receivable:generate"),
            ReferenceType.PROJECT, ReferenceType.BUILDING, ReferenceType.UNIT,
            ReferenceType.ROOM, ReferenceType.CUSTOMER, ReferenceType.CHARGE_ITEM,
            ReferenceType.CHARGE_RULE));
        policies.put(BusinessContext.PAYMENT_CREATE, all(
            List.of("property:payment:add", "property:payment:allocate"),
            ReferenceType.PROJECT, ReferenceType.ROOM, ReferenceType.CUSTOMER,
            ReferenceType.RECEIVABLE));
        policies.put(BusinessContext.CHARGE_BINDING_QUERY, all(
            List.of("property:chargeBinding:list"),
            ReferenceType.PROJECT, ReferenceType.BUILDING, ReferenceType.UNIT, ReferenceType.ROOM));
        policies.put(BusinessContext.CHARGE_BINDING_CREATE, all(
            List.of("property:chargeBinding:add"),
            ReferenceType.PROJECT, ReferenceType.ROOM, ReferenceType.CHARGE_ITEM, ReferenceType.CHARGE_RULE));
        policies.put(BusinessContext.CHARGE_BINDING_EDIT, all(
            List.of("property:chargeBinding:edit"),
            ReferenceType.PROJECT, ReferenceType.ROOM, ReferenceType.CHARGE_ITEM, ReferenceType.CHARGE_RULE));
        policies.put(BusinessContext.INBOUND_MAINTAIN, any(
            List.of("warehouse:inbound:add", "warehouse:inbound:edit"),
            ReferenceType.WAREHOUSE, ReferenceType.ZONE, ReferenceType.LOCATION,
            ReferenceType.MEASUREMENT_UNIT, ReferenceType.MATERIAL));
        policies.put(BusinessContext.OUTBOUND_MAINTAIN, any(
            List.of("warehouse:outbound:add", "warehouse:outbound:edit"),
            ReferenceType.WAREHOUSE, ReferenceType.ZONE, ReferenceType.LOCATION,
            ReferenceType.MEASUREMENT_UNIT, ReferenceType.MATERIAL, ReferenceType.INVENTORY));
        policies.put(BusinessContext.TRANSFER_MAINTAIN, any(
            List.of("warehouse:transfer:add", "warehouse:transfer:edit"),
            ReferenceType.WAREHOUSE, ReferenceType.ZONE, ReferenceType.LOCATION,
            ReferenceType.MEASUREMENT_UNIT, ReferenceType.MATERIAL, ReferenceType.INVENTORY));
        policies.put(BusinessContext.STOCKTAKE_MAINTAIN, any(
            List.of("warehouse:stocktake:add", "warehouse:stocktake:edit"),
            ReferenceType.WAREHOUSE, ReferenceType.ZONE, ReferenceType.LOCATION,
            ReferenceType.MEASUREMENT_UNIT, ReferenceType.MATERIAL, ReferenceType.INVENTORY));
        policies.put(BusinessContext.INBOUND_QUERY, any(
            List.of("warehouse:inbound:list", "warehouse:inbound:query"),
            ReferenceType.WAREHOUSE));
        policies.put(BusinessContext.OUTBOUND_QUERY, any(
            List.of("warehouse:outbound:list", "warehouse:outbound:query"),
            ReferenceType.WAREHOUSE));
        policies.put(BusinessContext.TRANSFER_QUERY, any(
            List.of("warehouse:transfer:list", "warehouse:transfer:query"),
            ReferenceType.WAREHOUSE));
        policies.put(BusinessContext.STOCKTAKE_QUERY, any(
            List.of("warehouse:stocktake:list", "warehouse:stocktake:query"),
            ReferenceType.WAREHOUSE));
        policies.put(BusinessContext.INVENTORY_QUERY, any(
            List.of("warehouse:inventory:list", "warehouse:inventory:query"),
            ReferenceType.WAREHOUSE, ReferenceType.ZONE, ReferenceType.LOCATION,
            ReferenceType.MATERIAL_CATEGORY, ReferenceType.MEASUREMENT_UNIT, ReferenceType.MATERIAL));
        policies.put(BusinessContext.INVENTORY_TRANSACTION_QUERY, any(
            List.of("warehouse:inventoryTransaction:list", "warehouse:inventoryTransaction:query"),
            ReferenceType.WAREHOUSE, ReferenceType.ZONE, ReferenceType.LOCATION,
            ReferenceType.MATERIAL_CATEGORY, ReferenceType.MEASUREMENT_UNIT, ReferenceType.MATERIAL));
        policies.put(BusinessContext.WAREHOUSE_ZONE_QUERY, any(
            List.of("warehouse:zone:list", "warehouse:zone:query"),
            ReferenceType.WAREHOUSE));
        policies.put(BusinessContext.WAREHOUSE_LOCATION_QUERY, any(
            List.of("warehouse:location:list", "warehouse:location:query"),
            ReferenceType.WAREHOUSE, ReferenceType.ZONE));
        policies.put(BusinessContext.WAREHOUSE_MATERIAL_CATEGORY_QUERY, any(
            List.of("warehouse:materialCategory:list", "warehouse:materialCategory:query"),
            ReferenceType.COMPANY));
        policies.put(BusinessContext.WAREHOUSE_MATERIAL_QUERY, any(
            List.of("warehouse:material:list", "warehouse:material:query"),
            ReferenceType.COMPANY, ReferenceType.MATERIAL_CATEGORY, ReferenceType.MEASUREMENT_UNIT));
        policies.put(BusinessContext.WAREHOUSE_ZONE_MAINTAIN, any(
            List.of("warehouse:zone:add", "warehouse:zone:edit"),
            ReferenceType.WAREHOUSE));
        policies.put(BusinessContext.WAREHOUSE_LOCATION_MAINTAIN, any(
            List.of("warehouse:location:add", "warehouse:location:edit"),
            ReferenceType.WAREHOUSE, ReferenceType.ZONE));
        policies.put(BusinessContext.WAREHOUSE_MATERIAL_CATEGORY_MAINTAIN, any(
            List.of("warehouse:materialCategory:add", "warehouse:materialCategory:edit"),
            ReferenceType.COMPANY, ReferenceType.MATERIAL_CATEGORY));
        policies.put(BusinessContext.WAREHOUSE_MATERIAL_MAINTAIN, any(
            List.of("warehouse:material:add", "warehouse:material:edit"),
            ReferenceType.COMPANY, ReferenceType.MATERIAL_CATEGORY, ReferenceType.MEASUREMENT_UNIT));
        return Map.copyOf(policies);
    }

    private static BusinessContextPolicy any(List<String> permissions, ReferenceType... referenceTypes) {
        return new BusinessContextPolicy(PermissionMode.ANY, permissions, Set.of(referenceTypes));
    }

    private static BusinessContextPolicy all(List<String> permissions, ReferenceType... referenceTypes) {
        return new BusinessContextPolicy(PermissionMode.ALL, permissions, Set.of(referenceTypes));
    }
}
