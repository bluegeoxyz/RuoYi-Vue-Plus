package org.dromara.warehouse.inventory.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.domain.bo.InventoryBo;
import org.dromara.warehouse.inventory.domain.vo.InventorySummaryVo;
import org.dromara.warehouse.inventory.domain.vo.InventoryVo;

import java.util.Collection;
import java.util.List;

/** 库存余额数据层。 */
public interface InventoryMapper extends BaseMapperPlus<Inventory, InventoryVo> {
    @Select("SELECT * FROM wm_inventory WHERE id = #{id} FOR UPDATE")
    Inventory selectByIdForUpdate(@Param("id") Long id);

    default Inventory selectBalance(Long warehouseId, Long locationId, Long materialId) {
        return lambda().eq(Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getLocationId, locationId)
                .eq(Inventory::getMaterialId, materialId)
                .one();
    }

    @Update(
            "UPDATE wm_inventory SET quantity = quantity + #{change}, version = version + 1, update_time = NOW() WHERE id = #{id} AND version = #{version}")
    int incrementQuantity(
            @Param("id") Long id,
            @Param("version") Integer version,
            @Param("change") java.math.BigDecimal change);

    @Update(
            "UPDATE wm_inventory SET quantity = quantity - #{change}, version = version + 1, update_time = NOW() WHERE id = #{id} AND version = #{version} AND quantity - locked_quantity >= #{change}")
    int decrementAvailable(
            @Param("id") Long id,
            @Param("version") Integer version,
            @Param("change") java.math.BigDecimal change);

    @Update(
            "UPDATE wm_inventory SET quantity = #{actual}, version = version + 1, update_time = NOW() WHERE id = #{id} AND version = #{version} AND quantity = #{book} AND locked_quantity <= #{actual}")
    int adjustForStocktake(
            @Param("id") Long id,
            @Param("version") Integer version,
            @Param("book") java.math.BigDecimal book,
            @Param("actual") java.math.BigDecimal actual);

    IPage<InventoryVo> selectInventoryPage(
            Page<InventoryVo> page,
            @Param("bo") InventoryBo bo,
            @Param("warehouseIds") Collection<Long> warehouseIds);

    List<InventoryVo> selectInventoryList(
            @Param("bo") InventoryBo bo, @Param("warehouseIds") Collection<Long> warehouseIds);

    IPage<InventorySummaryVo> selectSummaryPage(
            Page<InventorySummaryVo> page,
            @Param("bo") InventoryBo bo,
            @Param("warehouseIds") Collection<Long> warehouseIds);

    default boolean existsByWarehouseIds(Collection<Long> ids) {
        return !ids.isEmpty() && lambda().in(Inventory::getWarehouseId, ids).exists();
    }

    default boolean existsByLocationIds(Collection<Long> ids) {
        return !ids.isEmpty() && lambda().in(Inventory::getLocationId, ids).exists();
    }

    default boolean existsByMaterialIds(Collection<Long> ids) {
        return !ids.isEmpty() && lambda().in(Inventory::getMaterialId, ids).exists();
    }
}
