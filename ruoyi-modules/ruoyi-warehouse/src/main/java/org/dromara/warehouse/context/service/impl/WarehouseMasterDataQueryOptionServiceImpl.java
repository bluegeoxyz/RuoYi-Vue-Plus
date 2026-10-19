package org.dromara.warehouse.context.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.warehouse.context.domain.vo.ContextCompanyOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentZoneOptionVo;
import org.dromara.warehouse.context.service.WarehouseDocumentContextOptionService;
import org.dromara.warehouse.context.service.WarehouseMasterDataQueryOptionService;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.material.category.mapper.MaterialCategoryMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 仓库基础资料只读筛选项实现。 */
@Service
@RequiredArgsConstructor
public class WarehouseMasterDataQueryOptionServiceImpl implements WarehouseMasterDataQueryOptionService {

    private final BusinessContextAccessService contextAccessService;
    private final WarehouseAccessService warehouseAccessService;
    private final WarehouseDocumentContextOptionService documentOptionService;
    private final MaterialCategoryMapper categoryMapper;
    private final UnitMapper unitMapper;

    @Override
    public List<DocumentWarehouseOptionVo> selectWarehouses(BusinessContext context) {
        return documentOptionService.selectWarehouses(context);
    }

    @Override
    public List<DocumentZoneOptionVo> selectZones(BusinessContext context, Long warehouseId) {
        return documentOptionService.selectZones(context, warehouseId);
    }

    @Override
    public List<ContextCompanyOptionVo> selectCompanies(BusinessContext context) {
        contextAccessService.requireAccess(context, ReferenceType.COMPANY);
        return warehouseAccessService.selectMaterialCompanyOptions().stream()
            .map(this::companyOption)
            .toList();
    }

    @Override
    public List<MaterialCategoryOptionVo> selectMaterialCategories(BusinessContext context, Long companyId) {
        contextAccessService.requireAccess(context, ReferenceType.MATERIAL_CATEGORY);
        Set<Long> companyIds = warehouseAccessService.accessibleMaterialCompanyIds();
        if (companyId != null && !companyIds.contains(companyId)) {
            throw new ServiceException("无该公司物料分类访问权限");
        }
        if (companyIds.isEmpty()) return List.of();
        return buildCategoryTree(categoryMapper.selectOptions(companyIds, companyId));
    }

    @Override
    public List<UnitOptionVo> selectUnits(BusinessContext context) {
        contextAccessService.requireAccess(context, ReferenceType.MEASUREMENT_UNIT);
        return unitMapper.selectOptions();
    }

    private ContextCompanyOptionVo companyOption(SysDeptVo source) {
        return new ContextCompanyOptionVo(source.getDeptId(), source.getDeptName());
    }

    private List<MaterialCategoryOptionVo> buildCategoryTree(List<MaterialCategoryOptionVo> rows) {
        Map<Long, List<MaterialCategoryOptionVo>> children = rows.stream()
            .collect(Collectors.groupingBy(MaterialCategoryOptionVo::getParentId));
        rows.forEach(row -> row.setChildren(children.get(row.getId())));
        Set<Long> ids = rows.stream().map(MaterialCategoryOptionVo::getId).collect(Collectors.toSet());
        return rows.stream().filter(row -> row.getParentId() == 0L || !ids.contains(row.getParentId())).toList();
    }
}
