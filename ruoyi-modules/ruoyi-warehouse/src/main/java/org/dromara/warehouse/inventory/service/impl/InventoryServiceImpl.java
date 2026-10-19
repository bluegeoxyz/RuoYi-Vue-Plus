package org.dromara.warehouse.inventory.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.domain.bo.InventoryBo;
import org.dromara.warehouse.inventory.domain.vo.InventorySummaryVo;
import org.dromara.warehouse.inventory.domain.vo.InventoryVo;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.service.IInventoryService;
import org.dromara.warehouse.support.QuantityPrecisionUtils;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/** 库存余额服务实现。 */
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {
    private final InventoryMapper inventoryMapper;
    private final WarehouseLocationMapper locationMapper;
    private final MaterialMapper materialMapper;
    private final WarehouseAccessService accessService;

    @Override
    public InventoryVo queryById(Long id) {
        Inventory inventory = inventoryMapper.selectById(id);
        if (inventory == null) throw new ServiceException("库存余额不存在");
        accessService.requireWarehouse(inventory.getWarehouseId());
        InventoryBo bo = new InventoryBo(); bo.setWarehouseId(inventory.getWarehouseId()); bo.setLocationId(inventory.getLocationId());
        return queryList(bo).stream().filter(item -> id.equals(item.getId())).findFirst()
            .orElseThrow(() -> new ServiceException("库存余额不存在"));
    }

    @Override
    public PageResult<InventoryVo> queryPageList(InventoryBo bo, PageQuery pageQuery) {
        List<Long> ids = accessService.accessibleWarehouseIds();
        if (ids.isEmpty()) return PageResult.build(List.of(), 0L);
        IPage<InventoryVo> page = inventoryMapper.selectInventoryPage(pageQuery.build(), bo, ids);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<InventoryVo> queryList(InventoryBo bo) {
        List<Long> ids = accessService.accessibleWarehouseIds();
        return ids.isEmpty() ? List.of() : inventoryMapper.selectInventoryList(bo, ids);
    }

    @Override
    public PageResult<InventorySummaryVo> querySummaryPage(InventoryBo bo, PageQuery pageQuery) {
        List<Long> ids = accessService.accessibleWarehouseIds();
        if (ids.isEmpty()) return PageResult.build(List.of(), 0L);
        IPage<InventorySummaryVo> page = inventoryMapper.selectSummaryPage(pageQuery.build(), bo, ids);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public Inventory queryBalance(Long warehouseId, Long locationId, Long materialId) {
        accessService.requireWarehouse(warehouseId);
        return inventoryMapper.lambda().eq(Inventory::getWarehouseId, warehouseId)
            .eq(Inventory::getLocationId, locationId).eq(Inventory::getMaterialId, materialId).one();
    }

    @Override
    public void validateRelations(Long warehouseId, Long locationId, Long materialId) {
        Warehouse warehouse = accessService.requireWarehouse(warehouseId);
        WarehouseLocation location = locationMapper.selectById(locationId);
        if (location == null) throw new ServiceException("库位不存在");
        if (!warehouseId.equals(location.getWarehouseId())) throw new ServiceException("库位与仓库归属不一致");
        Material material = materialMapper.selectById(materialId);
        if (material == null) throw new ServiceException("物料不存在");
        if (!warehouse.getCompanyId().equals(material.getCompanyId())) throw new ServiceException("物料与仓库所属公司不一致");
    }

    @Override
    public void validateBalance(Inventory inventory) {
        if (inventory.getQuantity() == null || inventory.getQuantity().signum() < 0) {
            throw new ServiceException("库存数量不能为负数");
        }
        if (inventory.getLockedQuantity() == null || inventory.getLockedQuantity().signum() < 0) {
            throw new ServiceException("锁定数量不能为负数");
        }
        if (inventory.getLockedQuantity().compareTo(inventory.getQuantity()) > 0) {
            throw new ServiceException("锁定数量不能大于库存数量");
        }
        if (QuantityPrecisionUtils.exceedsPrecision(inventory.getQuantity(), 6)
            || QuantityPrecisionUtils.exceedsPrecision(inventory.getLockedQuantity(), 6)) {
            throw new ServiceException("库存数量小数位不能超过6位");
        }
        validateRelations(inventory.getWarehouseId(), inventory.getLocationId(), inventory.getMaterialId());
    }

    static BigDecimal available(BigDecimal quantity, BigDecimal lockedQuantity) {
        return quantity.subtract(lockedQuantity);
    }
}
