package org.dromara.warehouse.material.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.warehouse.material.category.mapper.MaterialCategoryMapper;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.transaction.mapper.InventoryTransactionMapper;
import org.dromara.warehouse.material.category.service.IMaterialCategoryService;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.domain.bo.MaterialBo;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.unit.service.IUnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 物料归属、单位和唯一性规则测试。 */
@ExtendWith(MockitoExtension.class) @MockitoSettings(strictness = Strictness.LENIENT)
class MaterialServiceImplTest {
    @Mock MaterialMapper materialMapper; @Mock MaterialCategoryMapper categoryMapper; @Mock UnitMapper unitMapper;
    @Mock IMaterialCategoryService categoryService; @Mock IUnitService unitService; @Mock WarehouseAccessService accessService; @Mock DictService dictService; @Mock InventoryMapper inventoryMapper; @Mock InventoryTransactionMapper transactionMapper;
    @InjectMocks MaterialServiceImpl service;
    @BeforeEach void setUp() { when(dictService.getAllDictByDictType("sys_normal_disable")).thenReturn(Map.of("0", "正常", "1", "停用")); when(materialMapper.insert(any(Material.class))).thenAnswer(i -> { ((Material)i.getArgument(0)).setId(1L); return 1; }); }
    @Test void createMaterialSucceeds() { MaterialBo bo = bo(1L, "MAT-1"); assertTrue(service.insertByBo(bo)); assertEquals(1L, bo.getId()); verify(categoryService).requireEnabledCategory(10L, 1L); verify(unitService).requireEnabledUnit(20L); }
    @Test void duplicateCodeInSameCompanyFails() { when(materialMapper.countByCode(1L, "MAT-1", null)).thenReturn(1L); assertThrows(ServiceException.class, () -> service.insertByBo(bo(1L, "MAT-1"))); }
    @Test void sameCodeInDifferentCompanySucceeds() { assertTrue(service.insertByBo(bo(2L, "MAT-1"))); verify(materialMapper).countByCode(2L, "MAT-1", null); }
    @Test void crossCompanyCategoryIsDenied() { doThrow(new ServiceException("company mismatch")).when(categoryService).requireEnabledCategory(10L, 1L); assertThrows(ServiceException.class, () -> service.insertByBo(bo(1L, "MAT-1"))); }
    @Test void missingUnitIsDenied() { doThrow(new ServiceException("missing")).when(unitService).requireEnabledUnit(20L); assertThrows(ServiceException.class, () -> service.insertByBo(bo(1L, "MAT-1"))); }
    @Test void disabledUnitIsDenied() { doThrow(new ServiceException("disabled")).when(unitService).requireEnabledUnit(20L); assertThrows(ServiceException.class, () -> service.insertByBo(bo(1L, "MAT-1"))); }
    @Test void disabledCategoryIsDenied() { doThrow(new ServiceException("disabled")).when(categoryService).requireEnabledCategory(10L, 1L); assertThrows(ServiceException.class, () -> service.insertByBo(bo(1L, "MAT-1"))); }
    @Test void negativeSafetyStockIsDenied() { MaterialBo bo = bo(1L, "MAT-1"); bo.setSafetyStock(BigDecimal.valueOf(-1)); assertThrows(ServiceException.class, () -> service.insertByBo(bo)); }
    @Test void crossCompanyDetailIdorIsDenied() { Material entity = new Material(); entity.setCompanyId(2L); when(materialMapper.selectById(9L)).thenReturn(entity); doThrow(new ServiceException("denied")).when(accessService).assertMaterialCompanyAccess(2L); assertThrows(ServiceException.class, () -> service.queryById(9L)); }
    @Test void optionsUseAccessibleCompanyScope() { when(accessService.accessibleMaterialCompanyIds()).thenReturn(Set.of(1L)); service.selectOptions(null); verify(materialMapper).selectOptions(Set.of(1L), null); }
    @Test void materialWithInventoryCannotBeDeleted() { Material material = new Material(); material.setCompanyId(1L); when(materialMapper.selectById(9L)).thenReturn(material); when(inventoryMapper.existsByMaterialIds(any())).thenReturn(true); assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(9L))); verify(materialMapper, never()).deleteByIds(any(java.util.Collection.class)); }
    @Test void materialWithTransactionCannotBeDeleted() { Material material = new Material(); material.setCompanyId(1L); when(materialMapper.selectById(9L)).thenReturn(material); when(transactionMapper.existsByMaterialIds(any())).thenReturn(true); assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(9L))); verify(materialMapper, never()).deleteByIds(any(java.util.Collection.class)); }
    private MaterialBo bo(Long companyId, String code) { MaterialBo bo = new MaterialBo(); bo.setCompanyId(companyId); bo.setMaterialCode(code); bo.setMaterialName("LED灯"); bo.setCategoryId(10L); bo.setBaseUnitId(20L); bo.setSafetyStock(BigDecimal.ZERO); bo.setStatus("0"); return bo; }
}
