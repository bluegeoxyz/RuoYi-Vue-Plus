package org.dromara.warehouse.context.service;

import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;

/** 仓库模块编辑初始化服务。 */
public interface WarehouseEditInitializationService {

    WarehouseEditInitializationVo.WarehouseEdit warehouse(Long id);

    WarehouseEditInitializationVo.ZoneEdit zone(Long id);

    WarehouseEditInitializationVo.LocationEdit location(Long id);

    WarehouseEditInitializationVo.MaterialCategoryEdit materialCategory(Long id);

    WarehouseEditInitializationVo.UnitEdit unit(Long id);

    WarehouseEditInitializationVo.MaterialEdit material(Long id);

    WarehouseEditInitializationVo.InboundEdit inbound(Long id);

    WarehouseEditInitializationVo.OutboundEdit outbound(Long id);

    WarehouseEditInitializationVo.TransferEdit transfer(Long id);

    WarehouseEditInitializationVo.StocktakeEdit stocktake(Long id);
}
