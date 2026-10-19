package org.dromara.warehouse.inventory.transaction.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.inventory.transaction.domain.InventoryTransaction;
import org.dromara.warehouse.inventory.transaction.domain.bo.InventoryTransactionBo;
import org.dromara.warehouse.inventory.transaction.domain.vo.InventoryTransactionVo;

import java.util.Collection;
import java.util.List;

/** 库存流水数据层。 */
public interface InventoryTransactionMapper extends BaseMapperPlus<InventoryTransaction, InventoryTransactionVo> {
    IPage<InventoryTransactionVo> selectTransactionPage(Page<InventoryTransactionVo> page,
        @Param("bo") InventoryTransactionBo bo, @Param("warehouseIds") Collection<Long> warehouseIds);
    List<InventoryTransactionVo> selectTransactionList(@Param("bo") InventoryTransactionBo bo,
        @Param("warehouseIds") Collection<Long> warehouseIds);
    InventoryTransactionVo selectTransactionById(@Param("id") Long id);

    default boolean existsByWarehouseIds(Collection<Long> ids) { return !ids.isEmpty() && lambda().in(InventoryTransaction::getWarehouseId, ids).exists(); }
    default boolean existsByLocationIds(Collection<Long> ids) { return !ids.isEmpty() && lambda().in(InventoryTransaction::getLocationId, ids).exists(); }
    default boolean existsByMaterialIds(Collection<Long> ids) { return !ids.isEmpty() && lambda().in(InventoryTransaction::getMaterialId, ids).exists(); }
}
