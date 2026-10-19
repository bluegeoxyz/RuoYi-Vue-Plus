package org.dromara.property.customer.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.customer.domain.bo.CustomerManagementGrantBo;
import org.dromara.property.customer.mapper.CustomerManagementGrantMapper;
import org.dromara.property.customer.service.CustomerManagementGrantAuditService;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomerManagementGrantServiceImplTest {
    private final CustomerManagementGrantMapper mapper = mock(CustomerManagementGrantMapper.class);
    private final ISysUserService userService = mock(ISysUserService.class);
    private final ISysRoleService roleService = mock(ISysRoleService.class);
    private final CustomerManagementGrantAuditService auditService = mock(CustomerManagementGrantAuditService.class);
    private final CustomerManagementGrantServiceImpl service =
        new CustomerManagementGrantServiceImpl(mapper, userService, roleService, auditService);

    @Test void nonSuperAdminCannotCreateCustomerGrant() {
        CustomerManagementGrantBo bo = new CustomerManagementGrantBo();
        bo.setPrincipalType("USER");
        bo.setPrincipalId(100L);
        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(false);
            assertThrows(ServiceException.class, () -> service.grant(bo));
        }
        verifyNoInteractions(mapper, userService, roleService, auditService);
    }
}
