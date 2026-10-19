package org.dromara.warehouse.context.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.warehouse.inbound.domain.vo.InboundOrderItemVo;
import org.dromara.warehouse.inbound.domain.vo.InboundOrderVo;
import org.dromara.warehouse.inbound.service.IInboundOrderService;
import org.dromara.warehouse.location.service.IWarehouseLocationService;
import org.dromara.warehouse.material.category.service.IMaterialCategoryService;
import org.dromara.warehouse.material.service.IMaterialService;
import org.dromara.warehouse.outbound.service.IOutboundOrderService;
import org.dromara.warehouse.outbound.domain.vo.OutboundOrderVo;
import org.dromara.warehouse.stocktake.domain.vo.StocktakeOrderVo;
import org.dromara.warehouse.stocktake.service.IStocktakeOrderService;
import org.dromara.warehouse.transfer.domain.vo.TransferOrderVo;
import org.dromara.warehouse.transfer.service.ITransferOrderService;
import org.dromara.warehouse.unit.service.IUnitService;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseVo;
import org.dromara.warehouse.warehouse.service.IWarehouseService;
import org.dromara.warehouse.zone.service.IWarehouseZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WarehouseEditInitializationServiceImplTest {

    private IWarehouseService warehouseService;
    private IInboundOrderService inboundService;
    private IOutboundOrderService outboundService;
    private ITransferOrderService transferService;
    private IStocktakeOrderService stocktakeService;
    private WarehouseEditInitializationServiceImpl service;

    @BeforeEach
    void setUp() {
        warehouseService = mock(IWarehouseService.class);
        inboundService = mock(IInboundOrderService.class);
        outboundService = mock(IOutboundOrderService.class);
        transferService = mock(ITransferOrderService.class);
        stocktakeService = mock(IStocktakeOrderService.class);
        service = new WarehouseEditInitializationServiceImpl(
            warehouseService,
            mock(IWarehouseZoneService.class),
            mock(IWarehouseLocationService.class),
            mock(IMaterialCategoryService.class),
            mock(IUnitService.class),
            mock(IMaterialService.class),
            inboundService,
            outboundService,
            transferService,
            stocktakeService);
    }

    @Test
    void baseDataDelegatesToExistingScopedReadAndReturnsOnlyEditFields() {
        WarehouseVo source = new WarehouseVo();
        source.setId(7L);
        source.setWarehouseName("项目仓");
        source.setCompanyName("不应返回的展示字段");
        when(warehouseService.queryById(7L)).thenReturn(source);

        var result = service.warehouse(7L);

        assertEquals(7L, result.getId());
        assertEquals("项目仓", result.getWarehouseName());
        verify(warehouseService).queryById(7L);
    }

    @Test
    void draftDocumentReturnsOnlyEditableHeaderAndItemFields() {
        InboundOrderVo source = new InboundOrderVo();
        source.setId(8L);
        source.setWarehouseId(9L);
        source.setStatus("DRAFT");
        InboundOrderItemVo item = new InboundOrderItemVo();
        item.setId(10L);
        item.setMaterialId(11L);
        item.setMaterialName("不应返回的展示字段");
        source.setItems(List.of(item));
        when(inboundService.queryById(8L)).thenReturn(source);

        var result = service.inbound(8L);

        assertEquals(9L, result.getWarehouseId());
        assertEquals(11L, result.getItems().getFirst().getMaterialId());
    }

    @Test
    void everyNonDraftDocumentCannotBeInitializedForEdit() {
        InboundOrderVo inbound = new InboundOrderVo();
        inbound.setStatus("CONFIRMED");
        when(inboundService.queryById(8L)).thenReturn(inbound);
        OutboundOrderVo outbound = new OutboundOrderVo();
        outbound.setStatus("COMPLETED");
        when(outboundService.queryById(9L)).thenReturn(outbound);
        TransferOrderVo transfer = new TransferOrderVo();
        transfer.setStatus("CONFIRMED");
        when(transferService.queryById(10L)).thenReturn(transfer);
        StocktakeOrderVo stocktake = new StocktakeOrderVo();
        stocktake.setStatus("COMPLETED");
        when(stocktakeService.queryById(11L)).thenReturn(stocktake);

        assertThrows(ServiceException.class, () -> service.inbound(8L));
        assertThrows(ServiceException.class, () -> service.outbound(9L));
        assertThrows(ServiceException.class, () -> service.transfer(10L));
        assertThrows(ServiceException.class, () -> service.stocktake(11L));
    }
}
