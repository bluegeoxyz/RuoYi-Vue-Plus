package org.dromara.property.workspace.service;

import org.dromara.property.scope.PropertyBusinessDomain;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceProjectPermissionPolicyTest {

    @Test
    void financePermissionShouldOnlyEnableFinanceDomain() {
        List<PropertyBusinessDomain> domains = WorkspaceProjectPermissionPolicy.permittedDomains(
            Set.of("property:payment:add")::contains);
        assertEquals(List.of(PropertyBusinessDomain.FINANCE), domains);
    }

    @Test
    void customerRelationPermissionShouldEnableCustomerRelationDomain() {
        List<PropertyBusinessDomain> domains = WorkspaceProjectPermissionPolicy.permittedDomains(
            Set.of("property:customerRelation:add")::contains);
        assertEquals(List.of(PropertyBusinessDomain.CUSTOMER_RELATION), domains);
    }

    @Test
    void warehouseAndHrPermissionsShouldNotEnablePropertyWorkspace() {
        Set<String> permissions = Set.of("warehouse:inbound:add", "hr:employee:list");
        assertTrue(WorkspaceProjectPermissionPolicy.permittedDomains(permissions::contains).isEmpty());
    }
}
