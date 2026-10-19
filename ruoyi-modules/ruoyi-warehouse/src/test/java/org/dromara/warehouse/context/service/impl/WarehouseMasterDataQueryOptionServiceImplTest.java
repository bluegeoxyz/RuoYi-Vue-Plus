package org.dromara.warehouse.context.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.warehouse.context.service.WarehouseDocumentContextOptionService;
import org.dromara.warehouse.material.category.mapper.MaterialCategoryMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseMasterDataQueryOptionServiceImplTest {

    @Mock BusinessContextAccessService contextAccessService;
    @Mock WarehouseAccessService warehouseAccessService;
    @Mock WarehouseDocumentContextOptionService documentOptionService;
    @Mock MaterialCategoryMapper categoryMapper;
    @Mock UnitMapper unitMapper;
    @InjectMocks WarehouseMasterDataQueryOptionServiceImpl service;

    @Test
    void warehouseAndZoneQueriesDelegateToScopedDocumentService() {
        service.selectWarehouses(BusinessContext.WAREHOUSE_ZONE_QUERY);
        service.selectZones(BusinessContext.WAREHOUSE_LOCATION_QUERY, 10L);

        verify(documentOptionService).selectWarehouses(BusinessContext.WAREHOUSE_ZONE_QUERY);
        verify(documentOptionService).selectZones(BusinessContext.WAREHOUSE_LOCATION_QUERY, 10L);
    }

    @Test
    void companyOptionsUseMaterialCompanyScope() {
        SysDeptVo company = new SysDeptVo();
        company.setDeptId(100L);
        company.setDeptName("测试公司");
        when(warehouseAccessService.selectMaterialCompanyOptions()).thenReturn(List.of(company));

        var result = service.selectCompanies(BusinessContext.WAREHOUSE_MATERIAL_QUERY);

        assertEquals(1, result.size());
        assertEquals(100L, result.getFirst().deptId());
        verify(contextAccessService).requireAccess(BusinessContext.WAREHOUSE_MATERIAL_QUERY, ReferenceType.COMPANY);
    }

    @Test
    void categoryQueryRejectsCompanyOutsideMaterialScope() {
        when(warehouseAccessService.accessibleMaterialCompanyIds()).thenReturn(Set.of(100L));

        assertThrows(ServiceException.class, () -> service.selectMaterialCategories(
            BusinessContext.WAREHOUSE_MATERIAL_QUERY, 101L));

        verify(contextAccessService).requireAccess(
            BusinessContext.WAREHOUSE_MATERIAL_QUERY, ReferenceType.MATERIAL_CATEGORY);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void unitQueryRequiresMaterialBusinessContext() {
        service.selectUnits(BusinessContext.WAREHOUSE_MATERIAL_QUERY);

        verify(contextAccessService).requireAccess(
            BusinessContext.WAREHOUSE_MATERIAL_QUERY, ReferenceType.MEASUREMENT_UNIT);
        verify(unitMapper).selectOptions();
    }
}
