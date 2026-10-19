package org.dromara.warehouse.context.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;
import org.dromara.warehouse.context.service.WarehouseEditInitializationService;
import org.dromara.warehouse.inbound.domain.vo.InboundOrderVo;
import org.dromara.warehouse.inbound.service.IInboundOrderService;
import org.dromara.warehouse.location.service.IWarehouseLocationService;
import org.dromara.warehouse.material.category.service.IMaterialCategoryService;
import org.dromara.warehouse.material.service.IMaterialService;
import org.dromara.warehouse.outbound.domain.vo.OutboundOrderVo;
import org.dromara.warehouse.outbound.service.IOutboundOrderService;
import org.dromara.warehouse.stocktake.domain.vo.StocktakeOrderVo;
import org.dromara.warehouse.stocktake.service.IStocktakeOrderService;
import org.dromara.warehouse.transfer.domain.vo.TransferOrderVo;
import org.dromara.warehouse.transfer.service.ITransferOrderService;
import org.dromara.warehouse.unit.service.IUnitService;
import org.dromara.warehouse.warehouse.service.IWarehouseService;
import org.dromara.warehouse.zone.service.IWarehouseZoneService;
import org.springframework.stereotype.Service;

/** 仓库模块编辑初始化实现。 */
@Service
@RequiredArgsConstructor
public class WarehouseEditInitializationServiceImpl implements WarehouseEditInitializationService {

    private final IWarehouseService warehouseService;
    private final IWarehouseZoneService zoneService;
    private final IWarehouseLocationService locationService;
    private final IMaterialCategoryService materialCategoryService;
    private final IUnitService unitService;
    private final IMaterialService materialService;
    private final IInboundOrderService inboundService;
    private final IOutboundOrderService outboundService;
    private final ITransferOrderService transferService;
    private final IStocktakeOrderService stocktakeService;

    @Override
    public WarehouseEditInitializationVo.WarehouseEdit warehouse(Long id) {
        return BeanUtil.copyProperties(warehouseService.queryById(id), WarehouseEditInitializationVo.WarehouseEdit.class);
    }

    @Override
    public WarehouseEditInitializationVo.ZoneEdit zone(Long id) {
        return BeanUtil.copyProperties(zoneService.queryById(id), WarehouseEditInitializationVo.ZoneEdit.class);
    }

    @Override
    public WarehouseEditInitializationVo.LocationEdit location(Long id) {
        return BeanUtil.copyProperties(locationService.queryById(id), WarehouseEditInitializationVo.LocationEdit.class);
    }

    @Override
    public WarehouseEditInitializationVo.MaterialCategoryEdit materialCategory(Long id) {
        return BeanUtil.copyProperties(materialCategoryService.queryById(id),
            WarehouseEditInitializationVo.MaterialCategoryEdit.class);
    }

    @Override
    public WarehouseEditInitializationVo.UnitEdit unit(Long id) {
        return BeanUtil.copyProperties(unitService.queryById(id), WarehouseEditInitializationVo.UnitEdit.class);
    }

    @Override
    public WarehouseEditInitializationVo.MaterialEdit material(Long id) {
        return BeanUtil.copyProperties(materialService.queryById(id), WarehouseEditInitializationVo.MaterialEdit.class);
    }

    @Override
    public WarehouseEditInitializationVo.InboundEdit inbound(Long id) {
        InboundOrderVo source = inboundService.queryById(id);
        requireDraft(source.getStatus(), "入库单");
        WarehouseEditInitializationVo.InboundEdit target = BeanUtil.copyProperties(source,
            WarehouseEditInitializationVo.InboundEdit.class);
        target.setItems(source.getItems().stream().map(item -> BeanUtil.copyProperties(item,
            WarehouseEditInitializationVo.InboundItemEdit.class)).toList());
        return target;
    }

    @Override
    public WarehouseEditInitializationVo.OutboundEdit outbound(Long id) {
        OutboundOrderVo source = outboundService.queryById(id);
        requireDraft(source.getStatus(), "出库单");
        WarehouseEditInitializationVo.OutboundEdit target = BeanUtil.copyProperties(source,
            WarehouseEditInitializationVo.OutboundEdit.class);
        target.setItems(source.getItems().stream().map(item -> BeanUtil.copyProperties(item,
            WarehouseEditInitializationVo.OutboundItemEdit.class)).toList());
        return target;
    }

    @Override
    public WarehouseEditInitializationVo.TransferEdit transfer(Long id) {
        TransferOrderVo source = transferService.queryById(id);
        requireDraft(source.getStatus(), "调拨单");
        WarehouseEditInitializationVo.TransferEdit target = BeanUtil.copyProperties(source,
            WarehouseEditInitializationVo.TransferEdit.class);
        target.setItems(source.getItems().stream().map(item -> BeanUtil.copyProperties(item,
            WarehouseEditInitializationVo.TransferItemEdit.class)).toList());
        return target;
    }

    @Override
    public WarehouseEditInitializationVo.StocktakeEdit stocktake(Long id) {
        StocktakeOrderVo source = stocktakeService.queryById(id);
        requireDraft(source.getStatus(), "盘点单");
        WarehouseEditInitializationVo.StocktakeEdit target = BeanUtil.copyProperties(source,
            WarehouseEditInitializationVo.StocktakeEdit.class);
        target.setItems(source.getItems().stream().map(item -> BeanUtil.copyProperties(item,
            WarehouseEditInitializationVo.StocktakeItemEdit.class)).toList());
        return target;
    }

    private void requireDraft(String status, String documentName) {
        if (!"DRAFT".equals(status)) {
            throw new ServiceException("仅草稿" + documentName + "允许编辑");
        }
    }
}
