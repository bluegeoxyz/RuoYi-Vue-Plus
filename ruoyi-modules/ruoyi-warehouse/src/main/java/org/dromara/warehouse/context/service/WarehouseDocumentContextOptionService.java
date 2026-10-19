package org.dromara.warehouse.context.service;

import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.ContextCompanyOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentInventoryOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentLocationOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentMaterialOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentZoneOptionVo;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;

import java.util.List;

/** 出库、调拨和盘点共用的受控引用数据查询。业务场景只能由服务端 Controller 固定传入。 */
public interface WarehouseDocumentContextOptionService {
    List<ContextCompanyOptionVo> selectCompanies(BusinessContext context);
    List<DocumentWarehouseOptionVo> selectWarehouses(BusinessContext context);
    List<MaterialCategoryOptionVo> selectMaterialCategories(BusinessContext context, Long companyId);
    List<DocumentZoneOptionVo> selectZones(BusinessContext context, Long warehouseId);
    List<DocumentLocationOptionVo> selectLocations(BusinessContext context, Long warehouseId, Long zoneId);
    List<DocumentMaterialOptionVo> selectMaterials(BusinessContext context, Long warehouseId);
    DocumentInventoryOptionVo selectInventory(BusinessContext context, Long warehouseId, Long locationId, Long materialId);
}
