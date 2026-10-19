package org.dromara.system.service;

import org.dromara.system.domain.bo.SysRoleBo;

import java.util.Collection;

/** 高风险角色委派的服务级安全边界。 */
public interface RoleDelegationGuard {
    void assertCanChangeRoleDefinition(SysRoleBo role, boolean permissionChange);
    void assertCanDelegateRoles(Collection<Long> roleIds);
    void assertCanDelegateRole(Long roleId);
}
