package org.dromara.property.workspace.service;

import org.dromara.property.scope.PropertyBusinessDomain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 工作项目所覆盖的业务域与既有权限映射。
 *
 * <p>映射完全由服务端维护，不接受前端场景或权限字符串。</p>
 */
public final class WorkspaceProjectPermissionPolicy {

    private static final Map<PropertyBusinessDomain, List<String>> DOMAIN_PERMISSIONS = buildPolicy();

    private WorkspaceProjectPermissionPolicy() {
    }

    public static List<PropertyBusinessDomain> permittedDomains(Predicate<String> permissionChecker) {
        return DOMAIN_PERMISSIONS.entrySet().stream()
            .filter(entry -> entry.getValue().stream().anyMatch(permissionChecker))
            .map(Map.Entry::getKey)
            .toList();
    }

    private static Map<PropertyBusinessDomain, List<String>> buildPolicy() {
        EnumMap<PropertyBusinessDomain, List<String>> policy = new EnumMap<>(PropertyBusinessDomain.class);
        policy.put(PropertyBusinessDomain.PROJECT_BASE, List.of(
            "property:project:list", "property:projectMember:list"));
        policy.put(PropertyBusinessDomain.RESOURCE, List.of(
            "property:building:list", "property:unit:list", "property:room:list",
            "property:customer:list", "property:resident:list"));
        policy.put(PropertyBusinessDomain.CUSTOMER_RELATION, List.of(
            "property:customerRelation:list", "property:customerRelation:add",
            "property:customerRelation:edit", "property:customerRelation:moveIn",
            "property:customerRelation:moveOut"));
        policy.put(PropertyBusinessDomain.FINANCE, List.of(
            "property:chargeItem:list", "property:chargeRule:list",
            "property:chargeBinding:list", "property:chargeBinding:add", "property:chargeBinding:edit",
            "property:receivable:list", "property:receivable:generate",
            "property:payment:list", "property:payment:add"));
        return Map.copyOf(policy);
    }
}
