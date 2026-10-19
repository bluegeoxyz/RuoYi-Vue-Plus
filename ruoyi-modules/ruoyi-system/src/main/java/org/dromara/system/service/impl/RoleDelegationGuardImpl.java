package org.dromara.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.SysRoleBo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.SysMenu;
import org.dromara.system.domain.SysRole;
import org.dromara.system.mapper.SysMenuMapper;
import org.dromara.system.mapper.SysRoleMapper;
import org.dromara.system.service.RoleDelegationGuard;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/** B1 的保守策略：菜单/数据权限变更及敏感角色委派只允许超级管理员。 */
@Service
@RequiredArgsConstructor
public class RoleDelegationGuardImpl implements RoleDelegationGuard {
    private static final Set<String> SENSITIVE_PERMISSIONS = Set.of(
        "system:role:add", "system:role:edit", "system:role:remove",
        "system:user:add", "system:user:edit", "system:user:remove",
        "property:projectAdminScope:list", "property:projectAdminScope:query",
        "property:projectAdminScope:add", "property:projectAdminScope:edit", "property:projectAdminScope:remove",
        "property:customer:pii:view", "property:customer:pii:export",
        "property:customerGrant:list", "property:customerGrant:query",
        "property:customerGrant:add", "property:customerGrant:edit", "property:customerGrant:remove");
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public void assertCanChangeRoleDefinition(SysRoleBo role, boolean permissionChange) {
        if (permissionChange || isSensitive(role) || (role.getRoleId() != null && isSensitive(roleMapper.selectById(role.getRoleId())))
            || containsSensitiveMenu(role.getMenuIds())) {
            requireSuperAdmin();
        }
    }

    @Override
    public void assertCanDelegateRoles(Collection<Long> roleIds) {
        if (roleIds == null) return;
        roleMapper.selectByIds(roleIds).forEach(this::assertCanDelegateRole);
    }

    @Override
    public void assertCanDelegateRole(Long roleId) {
        assertCanDelegateRole(roleMapper.selectById(roleId));
    }

    private void assertCanDelegateRole(SysRole role) {
        if (isSensitive(role)) requireSuperAdmin();
    }

    private boolean isSensitive(SysRoleBo role) {
        return role != null && (role.isSuperAdmin() || "admin".equalsIgnoreCase(role.getRoleKey()));
    }

    private boolean isSensitive(SysRole role) {
        return role != null && (SystemConstants.SUPER_ADMIN_ROLE_ID.equals(role.getRoleId())
            || "admin".equalsIgnoreCase(role.getRoleKey())
            || menuMapper.selectMenuPermsByRoleId(role.getRoleId()).stream().anyMatch(SENSITIVE_PERMISSIONS::contains));
    }

    private boolean containsSensitiveMenu(Long[] menuIds) {
        if (menuIds == null || menuIds.length == 0) return false;
        return menuMapper.selectByIds(java.util.List.of(menuIds)).stream()
            .map(SysMenu::getPerms).filter(java.util.Objects::nonNull).anyMatch(SENSITIVE_PERMISSIONS::contains);
    }

    private void requireSuperAdmin() {
        if (!LoginHelper.isSuperAdmin()) throw new ServiceException("仅超级管理员可委派或修改敏感角色权限");
    }
}
