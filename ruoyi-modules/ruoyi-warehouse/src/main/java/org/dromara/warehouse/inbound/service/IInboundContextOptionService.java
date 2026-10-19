package org.dromara.warehouse.inbound.service;

import org.dromara.warehouse.inbound.domain.vo.InboundLocationOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundMaterialOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundWarehouseOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundZoneOptionVo;

import java.util.List;

/** 入库制单业务上下文选项。 */
public interface IInboundContextOptionService {

    List<InboundWarehouseOptionVo> selectWarehouses();

    List<InboundZoneOptionVo> selectZones(Long warehouseId);

    List<InboundLocationOptionVo> selectLocations(Long warehouseId, Long zoneId);

    List<InboundMaterialOptionVo> selectMaterials(Long warehouseId);
}
