package org.dromara.property.customer.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.customer.domain.CustomerManagementGrant;
import org.dromara.property.customer.mapper.CustomerManagementGrantMapper;
import org.dromara.property.customer.service.*;
import org.dromara.property.scope.*;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysMenuService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerAccessPolicyImplTest {
    private final CustomerManagementGrantMapper grantMapper = mock(CustomerManagementGrantMapper.class);
    private final ISysRoleService roleService = mock(ISysRoleService.class);
    private final ISysMenuService menuService = mock(ISysMenuService.class);
    private final PropertyProjectScopeService projectScope = mock(PropertyProjectScopeService.class);
    private final CustomerSensitiveAccessAuditService auditService = mock(CustomerSensitiveAccessAuditService.class);
    private final CustomerAccessPolicyImpl policy = new CustomerAccessPolicyImpl(grantMapper, roleService, menuService,
        projectScope, auditService);

    @Test void noGrantMustDenyEvenWithFunctionalPermission() {
        try (MockedStatic<LoginHelper> login = login(20L, false); MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.hasPermission("property:customer:list")).thenReturn(true);
            when(grantMapper.selectEffective("USER", List.of(20L))).thenReturn(List.of());
            when(roleService.selectRolesByUserId(20L)).thenReturn(List.of());
            assertThrows(ServiceException.class, () -> policy.assertEnterpriseMasterAccess(CustomerAction.LIST));
        }
    }

    @Test void directUserGrantMustAuthorizeAndBeReadEveryRequest() {
        try (MockedStatic<LoginHelper> login = login(21L, false); MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.hasPermission("property:customer:query")).thenReturn(true);
            when(grantMapper.selectEffective("USER", List.of(21L))).thenReturn(List.of(grant("USER", 21L)));
            when(roleService.selectRolesByUserId(21L)).thenReturn(List.of());
            assertDoesNotThrow(() -> policy.assertEnterpriseMasterAccess(CustomerAction.DETAIL));
            assertDoesNotThrow(() -> policy.assertEnterpriseMasterAccess(CustomerAction.DETAIL));
            verify(grantMapper, times(2)).selectEffective("USER", List.of(21L));
        }
    }

    @Test void onlyEnabledRoleGrantMustAuthorizeAndRevocationMustImmediatelyDeny() {
        SysRoleVo enabled = role(31L, "0");
        try (MockedStatic<LoginHelper> login = login(22L, false); MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.hasPermission("property:customer:list")).thenReturn(true);
            when(grantMapper.selectEffective("USER", List.of(22L))).thenReturn(List.of());
            when(roleService.selectRolesByUserId(22L)).thenReturn(List.of(enabled));
            when(grantMapper.selectEffective("ROLE", List.of(31L)))
                .thenReturn(List.of(grant("ROLE", 31L))).thenReturn(List.of());
            assertDoesNotThrow(() -> policy.assertEnterpriseMasterAccess(CustomerAction.LIST));
            assertThrows(ServiceException.class, () -> policy.assertEnterpriseMasterAccess(CustomerAction.LIST));
        }
    }

    @Test void userAndRoleGrantSourcesMustBeMerged() {
        try (MockedStatic<LoginHelper> login = login(26L, false); MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.hasPermission("property:customer:list")).thenReturn(true);
            when(grantMapper.selectEffective("USER", List.of(26L))).thenReturn(List.of(grant("USER", 26L)));
            when(roleService.selectRolesByUserId(26L)).thenReturn(List.of(role(36L, "0")));
            when(grantMapper.selectEffective("ROLE", List.of(36L))).thenReturn(List.of(grant("ROLE", 36L)));
            assertDoesNotThrow(() -> policy.assertEnterpriseMasterAccess(CustomerAction.LIST));
            verify(grantMapper).selectEffective("USER", List.of(26L));
            verify(grantMapper).selectEffective("ROLE", List.of(36L));
        }
    }

    @Test void disabledRoleAndManagementScopeMustNotExpandCustomerAccess() {
        try (MockedStatic<LoginHelper> login = login(23L, false); MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.hasPermission("property:customer:list")).thenReturn(true);
            when(grantMapper.selectEffective("USER", List.of(23L))).thenReturn(List.of());
            when(roleService.selectRolesByUserId(23L)).thenReturn(List.of(role(32L, "1")));
            assertThrows(ServiceException.class, () -> policy.assertEnterpriseMasterAccess(CustomerAction.LIST));
            verifyNoInteractions(projectScope);
        }
    }

    @Test void customerRelationScopeMustRemainIndependentFromEnterpriseGrant() {
        try (MockedStatic<LoginHelper> login = login(24L, false)) {
            doThrow(new ServiceException("denied")).when(projectScope).assertBusinessAccess(
                100L, PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.WRITE);
            assertThrows(ServiceException.class,
                () -> policy.assertProjectRelationAccess(100L, ProjectBusinessAction.WRITE));
            verifyNoInteractions(grantMapper);
        }
    }

    @Test void piiViewAndExportMustBeIndependentAndSuperAdminStillAudited() {
        try (MockedStatic<LoginHelper> login = login(25L, false); MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            when(menuService.selectMenuPermsByUserId(25L)).thenReturn(Set.of("property:customer:pii:view"));
            assertTrue(policy.canViewSensitive(CustomerSensitiveField.IDENTITY));
            assertFalse(policy.canExportSensitive());
            assertThrows(ServiceException.class, () -> policy.assertSensitiveAccess(1L,
                CustomerSensitiveAction.PII_EXPORT, CustomerSensitiveField.IDENTITY,
                "CUSTOMER_EXPORT", "trace-1", null));
            verify(auditService).recordRequired(25L, 1L, CustomerSensitiveAction.PII_EXPORT,
                CustomerSensitiveField.IDENTITY, "CUSTOMER_EXPORT", "DENY", "trace-1", null,
                "MISSING_SENSITIVE_PERMISSION");
        }

        reset(auditService);
        try (MockedStatic<LoginHelper> login = login(1L, true)) {
            assertDoesNotThrow(() -> policy.assertSensitiveAccess(2L, CustomerSensitiveAction.PII_VIEW,
                CustomerSensitiveField.CONTACT, "CUSTOMER_DETAIL", "trace-2", null));
            verify(auditService).recordRequired(1L, 2L, CustomerSensitiveAction.PII_VIEW,
                CustomerSensitiveField.CONTACT, "CUSTOMER_DETAIL", "ALLOW", "trace-2", null,
                "AUTHORIZED");
        }
    }

    @Test void sensitiveBatchExportMustAuditAllowDenyAndSuperAdmin() {
        try (MockedStatic<LoginHelper> login = login(27L, false); MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            when(menuService.selectMenuPermsByUserId(27L)).thenReturn(Set.of());
            assertThrows(ServiceException.class,
                () -> policy.assertSensitiveExportAccess(2, 1, "trace-deny"));
            verify(auditService).recordRequired(27L, 0L, CustomerSensitiveAction.PII_EXPORT,
                CustomerSensitiveField.CONTACT_AND_IDENTITY, "CUSTOMER_EXPORT", "DENY",
                "trace-deny", null, "MISSING_PERMISSION_ROWS_2_FILTERS_1");
        }

        reset(auditService);
        try (MockedStatic<LoginHelper> login = login(1L, true)) {
            assertDoesNotThrow(() -> policy.assertSensitiveExportAccess(3, 0, "trace-admin"));
            verify(auditService).recordRequired(1L, 0L, CustomerSensitiveAction.PII_EXPORT,
                CustomerSensitiveField.CONTACT_AND_IDENTITY, "CUSTOMER_EXPORT", "ALLOW",
                "trace-admin", null, "AUTHORIZED_ROWS_3_FILTERS_0");
        }
    }

    private static MockedStatic<LoginHelper> login(Long userId, boolean superAdmin) {
        MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class);
        login.when(LoginHelper::getUserId).thenReturn(userId);
        login.when(LoginHelper::isSuperAdmin).thenReturn(superAdmin);
        return login;
    }

    private static CustomerManagementGrant grant(String type, Long id) {
        CustomerManagementGrant grant = new CustomerManagementGrant();
        grant.setPrincipalType(type); grant.setPrincipalId(id); grant.setScopeType("ENTERPRISE"); grant.setStatus("0");
        return grant;
    }

    private static SysRoleVo role(Long id, String status) {
        SysRoleVo role = new SysRoleVo(); role.setRoleId(id); role.setStatus(status); return role;
    }
}
