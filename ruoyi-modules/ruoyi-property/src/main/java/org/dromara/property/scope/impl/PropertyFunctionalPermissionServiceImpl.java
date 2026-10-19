package org.dromara.property.scope.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.scope.PropertyFunctionalPermissionService;
import org.dromara.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

/** Database-current functional permission check; no positive authorization cache. */
@Service
@RequiredArgsConstructor
public class PropertyFunctionalPermissionServiceImpl implements PropertyFunctionalPermissionService {

    private final ISysMenuService menuService;

    @Override
    public void assertCurrent(String permission) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        if (!menuService.selectMenuPermsByUserId(LoginHelper.getUserId()).contains(permission)) {
            throw new ServiceException("功能权限已失效");
        }
    }
}
