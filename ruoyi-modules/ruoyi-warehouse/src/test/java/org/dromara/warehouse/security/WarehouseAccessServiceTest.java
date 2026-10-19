package org.dromara.warehouse.security;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.api.ProjectAccessScope;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.system.service.ISysDeptService;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/** Warehouse 项目仓 IDOR 与超级管理员绕过测试。 */
@ExtendWith(MockitoExtension.class)
class WarehouseAccessServiceTest {
    @Mock WarehouseMapper warehouseMapper; @Mock ISysDeptService deptService; @Mock ProjectReferenceService projectReferenceService;
    @InjectMocks WarehouseAccessService service;

    @Test void projectAUserCannotAccessProjectBWarehouseByDetailId() {
        Warehouse warehouse = projectWarehouse(2L); when(warehouseMapper.selectById(9L)).thenReturn(warehouse);
        when(projectReferenceService.resolveWarehouseScope()).thenReturn(new ProjectAccessScope(ProjectAccessScope.Type.EXPLICIT_IDS, Set.of(1L)));
        try (MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(false);
            assertThrows(ServiceException.class, () -> service.requireWarehouse(9L));
        }
    }

    @Test void superAdminBypassesProjectScope() {
        Warehouse warehouse = projectWarehouse(2L); when(warehouseMapper.selectById(9L)).thenReturn(warehouse);
        try (MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(true);
            assertDoesNotThrow(() -> service.requireWarehouse(9L));
            verify(projectReferenceService, never()).resolveWarehouseScope();
        }
    }

    @Test void companyWarehouseRequiresDepartmentDataScope() {
        Warehouse warehouse = new Warehouse(); warehouse.setWarehouseScope("COMPANY"); warehouse.setCompanyId(100L);
        when(warehouseMapper.selectById(9L)).thenReturn(warehouse);
        doThrow(new ServiceException("无公司仓访问权限")).when(deptService).checkDeptDataScope(100L);
        try (MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(false);
            assertThrows(ServiceException.class, () -> service.requireWarehouse(9L));
        }
    }

    @Test void superAdminBypassesMaterialCompanyScope() {
        try (MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(true);
            assertDoesNotThrow(() -> service.assertMaterialCompanyAccess(999L));
            verifyNoInteractions(deptService, projectReferenceService);
        }
    }

    private Warehouse projectWarehouse(Long projectId) { Warehouse warehouse = new Warehouse(); warehouse.setWarehouseScope("PROJECT"); warehouse.setProjectId(projectId); return warehouse; }
}
