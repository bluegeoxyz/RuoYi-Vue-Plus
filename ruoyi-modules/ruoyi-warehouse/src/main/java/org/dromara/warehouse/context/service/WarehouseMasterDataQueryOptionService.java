package org.dromara.warehouse.context.service;

import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.ContextCompanyOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentZoneOptionVo;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;

import java.util.List;

/** 仓库基础资料只读页面的受控筛选项查询。 */
public interface WarehouseMasterDataQueryOptionService {

    List<DocumentWarehouseOptionVo> selectWarehouses(BusinessContext context);

    List<DocumentZoneOptionVo> selectZones(BusinessContext context, Long warehouseId);

    List<ContextCompanyOptionVo> selectCompanies(BusinessContext context);

    List<MaterialCategoryOptionVo> selectMaterialCategories(BusinessContext context, Long companyId);

    List<UnitOptionVo> selectUnits(BusinessContext context);
}
