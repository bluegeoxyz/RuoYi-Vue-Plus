package org.dromara.warehouse.inbound.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.warehouse.inbound.domain.vo.InboundLocationOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundMaterialOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundWarehouseOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundZoneOptionVo;
import org.dromara.warehouse.inbound.service.IInboundContextOptionService;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationOptionVo;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.vo.MaterialOptionVo;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseOptionVo;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneOptionVo;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 入库制单业务上下文选项实现。 */
@Service
@RequiredArgsConstructor
public class InboundContextOptionServiceImpl implements IInboundContextOptionService {

    private static final BusinessContext CONTEXT = BusinessContext.INBOUND_MAINTAIN;

    private final BusinessContextAccessService contextAccessService;
    private final WarehouseAccessService warehouseAccessService;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseZoneMapper zoneMapper;
    private final WarehouseLocationMapper locationMapper;
    private final MaterialMapper materialMapper;
    private final UnitMapper unitMapper;

    @Override
    public List<InboundWarehouseOptionVo> selectWarehouses() {
        contextAccessService.requireAccess(CONTEXT, ReferenceType.WAREHOUSE);
        List<Long> warehouseIds = warehouseAccessService.accessibleWarehouseIds();
        if (warehouseIds.isEmpty()) return List.of();
        return warehouseMapper.selectOptions(warehouseIds).stream().map(this::warehouseOption).toList();
    }

    @Override
    public List<InboundZoneOptionVo> selectZones(Long warehouseId) {
        contextAccessService.requireAccess(CONTEXT, ReferenceType.ZONE);
        warehouseAccessService.requireWarehouse(warehouseId);
        return zoneMapper.selectOptions(warehouseId).stream().map(this::zoneOption).toList();
    }

    @Override
    public List<InboundLocationOptionVo> selectLocations(Long warehouseId, Long zoneId) {
        contextAccessService.requireAccess(CONTEXT, ReferenceType.LOCATION);
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
    public List<InboundMaterialOptionVo> selectMaterials(Long warehouseId) {
        contextAccessService.requireAccess(CONTEXT, ReferenceType.MATERIAL);
        contextAccessService.requireAccess(CONTEXT, ReferenceType.MEASUREMENT_UNIT);
        Warehouse warehouse = warehouseAccessService.requireWarehouse(warehouseId);
        warehouseAccessService.assertMaterialCompanyAccess(warehouse.getCompanyId());
        List<MaterialOptionVo> materials = materialMapper.selectOptions(
            Set.of(warehouse.getCompanyId()), warehouse.getCompanyId());
        Map<Long, UnitOptionVo> units = unitMapper.selectOptions().stream()
            .collect(Collectors.toMap(UnitOptionVo::getId, Function.identity()));
        return materials.stream()
            .filter(material -> units.containsKey(material.getBaseUnitId()))
            .map(material -> materialOption(material, units.get(material.getBaseUnitId())))
            .toList();
    }

    private InboundWarehouseOptionVo warehouseOption(WarehouseOptionVo source) {
        return new InboundWarehouseOptionVo(source.getId(), source.getCompanyId(), source.getProjectId(),
            source.getWarehouseScope(), source.getWarehouseCode(), source.getWarehouseName());
    }

    private InboundZoneOptionVo zoneOption(WarehouseZoneOptionVo source) {
        return new InboundZoneOptionVo(source.getId(), source.getWarehouseId(), source.getZoneCode(), source.getZoneName());
    }

    private InboundLocationOptionVo locationOption(WarehouseLocationOptionVo source, WarehouseZoneOptionVo zone) {
        return new InboundLocationOptionVo(source.getId(), source.getWarehouseId(), source.getZoneId(),
            zone.getZoneName(), source.getLocationCode(), source.getLocationName());
    }

    private InboundMaterialOptionVo materialOption(MaterialOptionVo source, UnitOptionVo unit) {
        return new InboundMaterialOptionVo(source.getId(), source.getCompanyId(), source.getMaterialCode(),
            source.getMaterialName(), source.getBaseUnitId(), source.getSpecification(), source.getModel(),
            unit.getUnitName(), unit.getSymbol(), unit.getPrecisionScale());
    }
}
