package org.dromara.property.customer.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.SysMenu;
import org.dromara.system.domain.SysRole;
import org.dromara.system.domain.bo.SysRoleBo;
import org.dromara.system.mapper.SysMenuMapper;
import org.dromara.system.mapper.SysRoleMapper;
import org.dromara.system.service.impl.RoleDelegationGuardImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerRoleDelegationGuardTest {
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysMenuMapper menuMapper = mock(SysMenuMapper.class);
    private final RoleDelegationGuardImpl guard = new RoleDelegationGuardImpl(roleMapper, menuMapper);

    @Test void normalAdministratorCannotCreateRoleWithCustomerSensitivePermission() {
        SysRoleBo bo = new SysRoleBo(); bo.setRoleKey("customer_operator"); bo.setMenuIds(new Long[]{9001L});
        SysMenu piiMenu = new SysMenu(); piiMenu.setPerms("property:customer:pii:view");
        when(menuMapper.selectByIds(List.of(9001L))).thenReturn(List.of(piiMenu));
        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(false);
            assertThrows(ServiceException.class, () -> guard.assertCanChangeRoleDefinition(bo, false));
        }
    }

    @Test void normalAdministratorCannotDelegateRoleWithCustomerGrantPermission() {
        SysRole role = new SysRole(); role.setRoleId(88L); role.setRoleKey("customer_grant_admin");
        when(roleMapper.selectById(88L)).thenReturn(role);
        when(menuMapper.selectMenuPermsByRoleId(88L)).thenReturn(Set.of("property:customerGrant:add"));
        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(false);
            assertThrows(ServiceException.class, () -> guard.assertCanDelegateRole(88L));
        }
    }

    @Test void superAdminMayDelegateCustomerSensitiveRole() {
        SysRole role = new SysRole(); role.setRoleId(89L); role.setRoleKey("customer_pii_exporter");
        when(roleMapper.selectById(89L)).thenReturn(role);
        when(menuMapper.selectMenuPermsByRoleId(89L)).thenReturn(Set.of("property:customer:pii:export"));
        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(true);
            assertDoesNotThrow(() -> guard.assertCanDelegateRole(89L));
        }
    }
}
