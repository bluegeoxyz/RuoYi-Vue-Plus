package org.dromara.warehouse.inventory.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.domain.bo.InventoryBo;
import org.dromara.warehouse.inventory.domain.vo.InventorySummaryVo;
import org.dromara.warehouse.inventory.domain.vo.InventoryVo;

import java.util.List;

/** 只读库存查询与库存关系校验服务。 */
public interface IInventoryService {
    InventoryVo queryById(Long id);
    PageResult<InventoryVo> queryPageList(InventoryBo bo, PageQuery pageQuery);
    List<InventoryVo> queryList(InventoryBo bo);
    PageResult<InventorySummaryVo> querySummaryPage(InventoryBo bo, PageQuery pageQuery);
    Inventory queryBalance(Long warehouseId, Long locationId, Long materialId);
    void validateRelations(Long warehouseId, Long locationId, Long materialId);
    void validateBalance(Inventory inventory);
}
