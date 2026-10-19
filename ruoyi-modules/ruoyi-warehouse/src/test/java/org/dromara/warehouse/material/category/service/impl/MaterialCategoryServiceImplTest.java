package org.dromara.warehouse.material.category.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.warehouse.material.category.domain.MaterialCategory;
import org.dromara.warehouse.material.category.domain.bo.MaterialCategoryBo;
import org.dromara.warehouse.material.category.mapper.MaterialCategoryMapper;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 物料分类树与删除规则测试。 */
@ExtendWith(MockitoExtension.class) @MockitoSettings(strictness = Strictness.LENIENT)
class MaterialCategoryServiceImplTest {
    @Mock MaterialCategoryMapper categoryMapper; @Mock MaterialMapper materialMapper; @Mock WarehouseAccessService accessService; @Mock DictService dictService;
    @InjectMocks MaterialCategoryServiceImpl service;
    @BeforeEach void setUp() { when(dictService.getAllDictByDictType("sys_normal_disable")).thenReturn(Map.of("0", "正常", "1", "停用")); when(categoryMapper.insert(any(MaterialCategory.class))).thenAnswer(i -> { ((MaterialCategory)i.getArgument(0)).setId(1L); return 1; }); }
    @Test void createRootCategorySucceeds() { MaterialCategoryBo bo = bo(0L, "ENG"); assertTrue(service.insertByBo(bo)); assertEquals(1L, bo.getId()); }
    @Test void createChildCategorySucceeds() { when(categoryMapper.selectById(10L)).thenReturn(category(10L, 1L, 0L, "0", "0")); MaterialCategoryBo bo = bo(10L, "ELEC"); assertTrue(service.insertByBo(bo)); }
    @Test void duplicateCategoryCodeFails() { when(categoryMapper.countByCode(1L, "ENG", null)).thenReturn(1L); assertThrows(ServiceException.class, () -> service.insertByBo(bo(0L, "ENG"))); }
    @Test void categoryWithChildCannotBeDeleted() { when(categoryMapper.selectById(1L)).thenReturn(category(1L, 1L, 0L, "0", "0")); when(categoryMapper.existsChild(any())).thenReturn(true); assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L))); }
    @Test void categoryWithMaterialCannotBeDeleted() { when(categoryMapper.selectById(1L)).thenReturn(category(1L, 1L, 0L, "0", "0")); when(materialMapper.existsByCategoryIds(any())).thenReturn(true); assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L))); }
    @Test void crossCompanyCategoryDetailIsDenied() { when(categoryMapper.selectById(1L)).thenReturn(category(1L, 2L, 0L, "0", "0")); doThrow(new ServiceException("denied")).when(accessService).assertMaterialCompanyAccess(2L); assertThrows(ServiceException.class, () -> service.queryById(1L)); }
    private MaterialCategoryBo bo(Long parentId, String code) { MaterialCategoryBo bo = new MaterialCategoryBo(); bo.setCompanyId(1L); bo.setParentId(parentId); bo.setCategoryCode(code); bo.setCategoryName(code); bo.setStatus("0"); return bo; }
    private MaterialCategory category(Long id, Long companyId, Long parentId, String ancestors, String status) { MaterialCategory c = new MaterialCategory(); c.setId(id); c.setCompanyId(companyId); c.setParentId(parentId); c.setAncestors(ancestors); c.setStatus(status); return c; }
}
