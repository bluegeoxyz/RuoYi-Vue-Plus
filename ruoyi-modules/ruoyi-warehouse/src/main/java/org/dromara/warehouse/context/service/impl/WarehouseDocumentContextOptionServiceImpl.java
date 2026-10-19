package org.dromara.warehouse.context.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.warehouse.context.domain.vo.ContextCompanyOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentInventoryOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentLocationOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentMaterialOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentZoneOptionVo;
import org.dromara.warehouse.context.service.WarehouseDocumentContextOptionService;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationOptionVo;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.material.category.mapper.MaterialCategoryMapper;
import org.dromara.warehouse.material.domain.vo.MaterialOptionVo;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseOptionVo;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneOptionVo;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 仓库业务单据上下文选项实现；只负责能力与资源边界，不替代单据写操作校验。 */
@Service
@RequiredArgsConstructor
public class WarehouseDocumentContextOptionServiceImpl implements WarehouseDocumentContextOptionService {
    private final BusinessContextAccessService contextAccessService;
    private final WarehouseAccessService warehouseAccessService;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseZoneMapper zoneMapper;
    private final WarehouseLocationMapper locationMapper;
    private final MaterialCategoryMapper categoryMapper;
    private final MaterialMapper materialMapper;
    private final UnitMapper unitMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public List<ContextCompanyOptionVo> selectCompanies(BusinessContext context) {
        contextAccessService.requireAccess(context, ReferenceType.WAREHOUSE);
        Set<Long> companyIds = accessibleWarehouseOptions().stream()
            .map(WarehouseOptionVo::getCompanyId).collect(Collectors.toSet());
        if (companyIds.isEmpty()) return List.of();
        return warehouseAccessService.selectMaterialCompanyOptions().stream()
            .filter(company -> companyIds.contains(company.getDeptId()))
            .map(this::companyOption).toList();
    }

    @Override
    public List<DocumentWarehouseOptionVo> selectWarehouses(BusinessContext context) {
        contextAccessService.requireAccess(context, ReferenceType.WAREHOUSE);
        return accessibleWarehouseOptions().stream().map(this::warehouseOption).toList();
    }

    @Override
    public List<MaterialCategoryOptionVo> selectMaterialCategories(BusinessContext context, Long companyId) {
        contextAccessService.requireAccess(context, ReferenceType.MATERIAL_CATEGORY);
        Set<Long> companyIds = accessibleWarehouseOptions().stream()
            .map(WarehouseOptionVo::getCompanyId).collect(Collectors.toSet());
        if (companyId != null && !companyIds.contains(companyId)) {
            throw new ServiceException("无该公司仓库及物料分类访问权限");
        }
        if (companyIds.isEmpty()) return List.of();
        return buildCategoryTree(categoryMapper.selectOptions(companyIds, companyId));
    }

    @Override
    public List<DocumentZoneOptionVo> selectZones(BusinessContext context, Long warehouseId) {
        contextAccessService.requireAccess(context, ReferenceType.ZONE);
        warehouseAccessService.requireWarehouse(warehouseId);
        return zoneMapper.selectOptions(warehouseId).stream().map(this::zoneOption).toList();
    }

    @Override
    public List<DocumentLocationOptionVo> selectLocations(BusinessContext context, Long warehouseId, Long zoneId) {
        contextAccessService.requireAccess(context, ReferenceType.LOCATION);
        warehouseAccessService.requireWarehouse(warehouseId);
        Map<Long, WarehouseZoneOptionVo> zones = zoneMapper.selectOptions(warehouseId).stream()
            .collect(Collectors.toMap(WarehouseZoneOptionVo::getId, Function.identity()));
        if (zoneId != null && !zones.containsKey(zoneId)) {
            throw new ServiceException("库区不属于当前授权仓库或已停用");
        }
        if (zones.isEmpty()) return List.of();
        return locationMapper.selectOptions(warehouseId, zoneId).stream()
            .filter(location -> zones.containsKey(location.getZoneId()))
            .map(location -> locationOption(location, zones.get(location.getZoneId())))
            .toList();
    }

    @Override
    public List<DocumentMaterialOptionVo> selectMaterials(BusinessContext context, Long warehouseId) {
        contextAccessService.requireAccess(context, ReferenceType.MATERIAL);
        contextAccessService.requireAccess(context, ReferenceType.MEASUREMENT_UNIT);
        Warehouse warehouse = warehouseAccessService.requireWarehouse(warehouseId);
        warehouseAccessService.assertMaterialCompanyAccess(warehouse.getCompanyId());
        List<MaterialOptionVo> materials = materialMapper.selectOptions(Set.of(warehouse.getCompanyId()), warehouse.getCompanyId());
        Map<Long, UnitOptionVo> units = unitMapper.selectOptions().stream()
            .collect(Collectors.toMap(UnitOptionVo::getId, Function.identity()));
        return materials.stream().filter(material -> units.containsKey(material.getBaseUnitId()))
            .map(material -> materialOption(material, units.get(material.getBaseUnitId()))).toList();
    }

    @Override
    public DocumentInventoryOptionVo selectInventory(BusinessContext context, Long warehouseId,
                                                      Long locationId, Long materialId) {
        contextAccessService.requireAccess(context, ReferenceType.INVENTORY);
        Warehouse warehouse = warehouseAccessService.requireWarehouse(warehouseId);
        requireLocation(warehouse, locationId);
        requireMaterial(warehouse, materialId);
        Inventory inventory = inventoryMapper.selectBalance(warehouseId, locationId, materialId);
        BigDecimal quantity = inventory == null || inventory.getQuantity() == null ? BigDecimal.ZERO : inventory.getQuantity();
        BigDecimal locked = inventory == null || inventory.getLockedQuantity() == null ? BigDecimal.ZERO : inventory.getLockedQuantity();
        return new DocumentInventoryOptionVo(warehouseId, locationId, materialId, quantity, locked, quantity.subtract(locked));
    }

    private void requireLocation(Warehouse warehouse, Long locationId) {
        WarehouseLocation location = locationMapper.selectById(locationId);
        if (location == null || !warehouse.getId().equals(location.getWarehouseId())
            || !SystemConstants.NORMAL.equals(location.getStatus())) {
            throw new ServiceException("库位无效、已停用或不属于当前授权仓库");
        }
        WarehouseZone zone = zoneMapper.selectById(location.getZoneId());
        if (zone == null || !warehouse.getId().equals(zone.getWarehouseId())
            || !SystemConstants.NORMAL.equals(zone.getStatus())) {
            throw new ServiceException("库位所属库区无效或已停用");
        }
    }

    private void requireMaterial(Warehouse warehouse, Long materialId) {
        warehouseAccessService.assertMaterialCompanyAccess(warehouse.getCompanyId());
        Material material = materialMapper.selectById(materialId);
        if (material == null || !warehouse.getCompanyId().equals(material.getCompanyId())
            || !SystemConstants.NORMAL.equals(material.getStatus())) {
            throw new ServiceException("物料无效、已停用或与仓库公司不一致");
        }
    }

    private DocumentWarehouseOptionVo warehouseOption(WarehouseOptionVo source) {
        return new DocumentWarehouseOptionVo(source.getId(), source.getCompanyId(), source.getProjectId(),
            source.getWarehouseScope(), source.getWarehouseCode(), source.getWarehouseName());
    }

    private ContextCompanyOptionVo companyOption(SysDeptVo source) {
        return new ContextCompanyOptionVo(source.getDeptId(), source.getDeptName());
    }

    private List<WarehouseOptionVo> accessibleWarehouseOptions() {
        List<Long> warehouseIds = warehouseAccessService.accessibleWarehouseIds();
        return warehouseIds.isEmpty() ? List.of() : warehouseMapper.selectOptions(warehouseIds);
    }

    private List<MaterialCategoryOptionVo> buildCategoryTree(List<MaterialCategoryOptionVo> rows) {
        Map<Long, List<MaterialCategoryOptionVo>> children = rows.stream()
            .collect(Collectors.groupingBy(MaterialCategoryOptionVo::getParentId));
        rows.forEach(row -> row.setChildren(children.get(row.getId())));
        Set<Long> ids = rows.stream().map(MaterialCategoryOptionVo::getId).collect(Collectors.toSet());
        return rows.stream().filter(row -> row.getParentId() == 0L || !ids.contains(row.getParentId())).toList();
    }

    private DocumentZoneOptionVo zoneOption(WarehouseZoneOptionVo source) {
        return new DocumentZoneOptionVo(source.getId(), source.getWarehouseId(), source.getZoneCode(), source.getZoneName());
    }

    private DocumentLocationOptionVo locationOption(WarehouseLocationOptionVo source, WarehouseZoneOptionVo zone) {
        return new DocumentLocationOptionVo(source.getId(), source.getWarehouseId(), source.getZoneId(),
            zone.getZoneName(), source.getLocationCode(), source.getLocationName());
    }

    private DocumentMaterialOptionVo materialOption(MaterialOptionVo source, UnitOptionVo unit) {
        return new DocumentMaterialOptionVo(source.getId(), source.getCompanyId(), source.getMaterialCode(),
            source.getMaterialName(), source.getBaseUnitId(), source.getSpecification(), source.getModel(),
            unit.getUnitName(), unit.getSymbol(), unit.getPrecisionScale());
    }
}
