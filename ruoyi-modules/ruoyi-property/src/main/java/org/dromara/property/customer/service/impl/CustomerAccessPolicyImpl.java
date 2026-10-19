package org.dromara.property.customer.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.customer.domain.CustomerManagementGrant;
import org.dromara.property.customer.mapper.CustomerManagementGrantMapper;
import org.dromara.property.customer.service.*;
import org.dromara.property.scope.*;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 实时读取数据库授权，不使用 Customer 正向权限缓存。 */
@Service
@RequiredArgsConstructor
public class CustomerAccessPolicyImpl implements CustomerAccessPolicy {
    private final CustomerManagementGrantMapper grantMapper;
    private final ISysRoleService roleService;
    private final ISysMenuService menuService;
    private final PropertyProjectScopeService projectScopeService;
    private final CustomerSensitiveAccessAuditService sensitiveAuditService;

    @Override
    public CustomerAccessDecision resolve(CustomerAction action) {
        boolean enterprise = hasEnterpriseGrant() && hasPermission(action.permission());
        PropertyProjectScope relationScope = projectScopeService.resolveBusinessScope(
            PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.READ);
        Set<CustomerSensitiveField> visible = hasSensitivePermission(CustomerSensitiveAction.PII_VIEW)
            ? Set.of(CustomerSensitiveField.values()) : Set.of();
        return new CustomerAccessDecision(enterprise, relationScope, visible,
            hasSensitivePermission(CustomerSensitiveAction.PII_EXPORT));
    }

    @Override
    public void assertEnterpriseMasterAccess(CustomerAction action) {
        if (!hasPermission(action.permission())) {
            throw new ServiceException("无客户主档功能权限");
        }
        if (!hasEnterpriseGrant()) {
            throw new ServiceException("无企业客户主档授权");
        }
    }

    @Override
    public void assertCustomerVisible(Long customerId, CustomerAction action) {
        if (customerId == null || customerId <= 0) {
            throw new ServiceException("客户ID无效");
        }
        assertEnterpriseMasterAccess(action);
    }

    @Override
    public void assertProjectRelationAccess(Long projectId, ProjectBusinessAction action) {
        projectScopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.CUSTOMER_RELATION, action);
    }

    @Override
    public boolean canViewSensitive(CustomerSensitiveField field) {
        return field != null && hasSensitivePermission(CustomerSensitiveAction.PII_VIEW);
    }

    @Override
    public boolean canExportSensitive() {
        return hasSensitivePermission(CustomerSensitiveAction.PII_EXPORT);
    }

    @Override
    public void assertSensitiveAccess(Long customerId, CustomerSensitiveAction action,
                                      CustomerSensitiveField field, String purpose,
                                      String traceId, Long projectId) {
        boolean allowed = customerId != null && customerId > 0 && field != null
            && hasSensitivePermission(action);
        sensitiveAuditService.recordRequired(LoginHelper.getUserId(), customerId, action, field,
            purpose, allowed ? "ALLOW" : "DENY", traceId, projectId,
            allowed ? "AUTHORIZED" : "MISSING_SENSITIVE_PERMISSION");
        if (!allowed) {
            throw new ServiceException("无客户敏感资料访问权限");
        }
    }

    @Override
    public void assertSensitiveExportAccess(int recordCount, int filterCount, String traceId) {
        boolean allowed = hasSensitivePermission(CustomerSensitiveAction.PII_EXPORT);
        sensitiveAuditService.recordRequired(LoginHelper.getUserId(), 0L,
            CustomerSensitiveAction.PII_EXPORT, CustomerSensitiveField.CONTACT_AND_IDENTITY,
            "CUSTOMER_EXPORT", allowed ? "ALLOW" : "DENY", traceId, null,
            exportReason(recordCount, filterCount, allowed ? "AUTHORIZED" : "MISSING_PERMISSION"));
        if (!allowed) {
            throw new ServiceException("无客户敏感资料导出权限");
        }
    }

    @Override
    public void recordSensitiveExportFailure(int recordCount, String traceId) {
        sensitiveAuditService.recordRequired(LoginHelper.getUserId(), 0L,
            CustomerSensitiveAction.PII_EXPORT, CustomerSensitiveField.CONTACT_AND_IDENTITY,
            "CUSTOMER_EXPORT", "FAILED", traceId, null,
            exportReason(recordCount, 0, "FILE_GENERATION_FAILED"));
    }

    private String exportReason(int recordCount, int filterCount, String result) {
        return result + "_ROWS_" + Math.max(recordCount, 0) + "_FILTERS_" + Math.max(filterCount, 0);
    }

    private boolean hasEnterpriseGrant() {
        if (LoginHelper.isSuperAdmin()) {
            return true;
        }
        Long userId = LoginHelper.getUserId();
        List<CustomerManagementGrant> grants = new ArrayList<>(
            grantMapper.selectEffective("USER", List.of(userId)));
        List<Long> enabledRoleIds = roleService.selectRolesByUserId(userId).stream()
            .filter(role -> "0".equals(role.getStatus()))
            .map(role -> role.getRoleId()).toList();
        grants.addAll(grantMapper.selectEffective("ROLE", enabledRoleIds));
        return !grants.isEmpty();
    }

    private boolean hasPermission(String permission) {
        if (LoginHelper.isSuperAdmin()) return true;
        if (CustomerAction.IMPORT.permission().equals(permission)) {
            return menuService.selectMenuPermsByUserId(LoginHelper.getUserId()).contains(permission);
        }
        return StpUtil.hasPermission(permission);
    }

    private boolean hasSensitivePermission(CustomerSensitiveAction action) {
        // 敏感权限必须读取数据库当前角色状态与菜单授权，不能依赖登录时权限快照。
        return LoginHelper.isSuperAdmin()
            || menuService.selectMenuPermsByUserId(LoginHelper.getUserId()).contains(action.permission());
    }
}
