package org.dromara.warehouse.inventory.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.transaction.domain.command.InventoryTransactionCommand;
import org.dromara.warehouse.inventory.transaction.service.IInventoryTransactionService;
import org.dromara.warehouse.support.QuantityPrecisionUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 仅供正式库存业务在既有事务中调用的余额变更入口。 */
@Service
@RequiredArgsConstructor
public class InventoryMutationService {
  private final InventoryMapper inventoryMapper;
  private final IInventoryService inventoryService;
  private final IInventoryTransactionService transactionService;

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
  public void inbound(
      Long warehouseId,
      Long locationId,
      Long materialId,
      BigDecimal quantity,
      Long sourceId,
      String sourceNo,
      LocalDateTime occurredAt,
      Long operatorId) {
    increase(
        warehouseId,
        locationId,
        materialId,
        quantity,
        sourceId,
        sourceNo,
        occurredAt,
        operatorId,
        "INBOUND",
        "INBOUND_ORDER",
        "入库");
  }

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
  public void transferIn(
      Long warehouseId,
      Long locationId,
      Long materialId,
      BigDecimal quantity,
      Long sourceId,
      String sourceNo,
      LocalDateTime occurredAt,
      Long operatorId) {
    increase(
        warehouseId,
        locationId,
        materialId,
        quantity,
        sourceId,
        sourceNo,
        occurredAt,
        operatorId,
        "TRANSFER_IN",
        "TRANSFER_ORDER",
        "调入");
  }

  private void increase(
      Long warehouseId,
      Long locationId,
      Long materialId,
      BigDecimal quantity,
      Long sourceId,
      String sourceNo,
      LocalDateTime occurredAt,
      Long operatorId,
      String transactionType,
      String sourceType,
      String action) {
    if (quantity == null || quantity.signum() <= 0 || QuantityPrecisionUtils.exceedsPrecision(quantity, 6)) {
      throw new ServiceException(action + "数量必须大于0且小数位不超过6位");
    }
    inventoryService.validateRelations(warehouseId, locationId, materialId);
    Inventory balance = inventoryMapper.selectBalance(warehouseId, locationId, materialId);
    BigDecimal before;
    BigDecimal locked;
    if (balance == null) {
      before = BigDecimal.ZERO;
      locked = BigDecimal.ZERO;
      balance = new Inventory();
      balance.setWarehouseId(warehouseId);
      balance.setLocationId(locationId);
      balance.setMaterialId(materialId);
      balance.setQuantity(quantity);
      balance.setLockedQuantity(BigDecimal.ZERO);
      balance.setVersion(0);
      try {
        inventoryMapper.insert(balance);
      } catch (DuplicateKeyException ex) {
        throw new ServiceException("库存余额已被其他业务并发创建，请重试完成" + action);
      }
    } else {
      before = balance.getQuantity();
      locked = balance.getLockedQuantity();
      if (inventoryMapper.incrementQuantity(balance.getId(), balance.getVersion(), quantity) != 1) {
        throw new ServiceException("库存余额已发生并发变化，请重试完成" + action);
      }
    }
    InventoryTransactionCommand command = new InventoryTransactionCommand();
    command.setTransactionType(transactionType);
    command.setWarehouseId(warehouseId);
    command.setLocationId(locationId);
    command.setMaterialId(materialId);
    command.setQuantityBefore(before);
    command.setQuantityChange(quantity);
    command.setQuantityAfter(before.add(quantity));
    command.setLockedBefore(locked);
    command.setLockedChange(BigDecimal.ZERO);
    command.setLockedAfter(locked);
    command.setSourceType(sourceType);
    command.setSourceId(sourceId);
    command.setSourceNo(sourceNo);
    command.setOccurredAt(occurredAt);
    command.setOperatorId(operatorId);
    transactionService.record(command);
  }

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
  public void outbound(
      Long warehouseId,
      Long locationId,
      Long materialId,
      BigDecimal quantity,
      Long sourceId,
      String sourceNo,
      LocalDateTime occurredAt,
      Long operatorId) {
    decrease(
        warehouseId,
        locationId,
        materialId,
        quantity,
        sourceId,
        sourceNo,
        occurredAt,
        operatorId,
        "OUTBOUND",
        "OUTBOUND_ORDER",
        "出库");
  }

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
  public void transferOut(
      Long warehouseId,
      Long locationId,
      Long materialId,
      BigDecimal quantity,
      Long sourceId,
      String sourceNo,
      LocalDateTime occurredAt,
      Long operatorId) {
    decrease(
        warehouseId,
        locationId,
        materialId,
        quantity,
        sourceId,
        sourceNo,
        occurredAt,
        operatorId,
        "TRANSFER_OUT",
        "TRANSFER_ORDER",
        "调出");
  }

  private void decrease(
      Long warehouseId,
      Long locationId,
      Long materialId,
      BigDecimal quantity,
      Long sourceId,
      String sourceNo,
      LocalDateTime occurredAt,
      Long operatorId,
      String transactionType,
      String sourceType,
      String action) {
    if (quantity == null || quantity.signum() <= 0 || QuantityPrecisionUtils.exceedsPrecision(quantity, 6)) {
      throw new ServiceException(action + "数量必须大于0且小数位不超过6位");
    }
    inventoryService.validateRelations(warehouseId, locationId, materialId);
    Inventory balance = inventoryMapper.selectBalance(warehouseId, locationId, materialId);
    if (balance == null) throw new ServiceException("目标物料在该库位没有库存余额");
    BigDecimal available = balance.getQuantity().subtract(balance.getLockedQuantity());
    if (available.compareTo(quantity) < 0) {
      throw new ServiceException(
          "可用库存不足：需要 " + quantity.toPlainString() + "，可用 " + available.toPlainString());
    }
    if (inventoryMapper.decrementAvailable(balance.getId(), balance.getVersion(), quantity) != 1) {
      Inventory latest = inventoryMapper.selectBalance(warehouseId, locationId, materialId);
      BigDecimal latestAvailable =
          latest == null
              ? BigDecimal.ZERO
              : latest.getQuantity().subtract(latest.getLockedQuantity());
      if (latestAvailable.compareTo(quantity) < 0) {
        throw new ServiceException(
            "可用库存不足：需要 " + quantity.toPlainString() + "，可用 " + latestAvailable.toPlainString());
      }
      throw new ServiceException("库存余额已发生并发变化，请重试完成出库");
    }
    InventoryTransactionCommand command = new InventoryTransactionCommand();
    command.setTransactionType(transactionType);
    command.setWarehouseId(warehouseId);
    command.setLocationId(locationId);
    command.setMaterialId(materialId);
    command.setQuantityBefore(balance.getQuantity());
    command.setQuantityChange(quantity.negate());
    command.setQuantityAfter(balance.getQuantity().subtract(quantity));
    command.setLockedBefore(balance.getLockedQuantity());
    command.setLockedChange(BigDecimal.ZERO);
    command.setLockedAfter(balance.getLockedQuantity());
    command.setSourceType(sourceType);
    command.setSourceId(sourceId);
    command.setSourceNo(sourceNo);
    command.setOccurredAt(occurredAt);
    command.setOperatorId(operatorId);
    transactionService.record(command);
  }

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
  public void adjustForStocktake(
      Long warehouseId,
      Long locationId,
      Long materialId,
      BigDecimal expectedBook,
      BigDecimal actual,
      Long sourceId,
      String sourceNo,
      LocalDateTime occurredAt,
      Long operatorId) {
    if (expectedBook == null
        || expectedBook.signum() < 0
        || actual == null
        || actual.signum() < 0
        || QuantityPrecisionUtils.exceedsPrecision(expectedBook, 6)
        || QuantityPrecisionUtils.exceedsPrecision(actual, 6)) throw new ServiceException("盘点账面和实际数量必须为非负数且小数位不超过6位");
    inventoryService.validateRelations(warehouseId, locationId, materialId);
    Inventory balance = inventoryMapper.selectBalance(warehouseId, locationId, materialId);
    BigDecimal difference = actual.subtract(expectedBook);
    BigDecimal locked = BigDecimal.ZERO;
    if (balance == null) {
      if (expectedBook.signum() != 0) throw new ServiceException("盘点期间库存已发生变化，请重新盘点");
      if (difference.signum() == 0) return;
      Inventory created = new Inventory();
      created.setWarehouseId(warehouseId);
      created.setLocationId(locationId);
      created.setMaterialId(materialId);
      created.setQuantity(actual);
      created.setLockedQuantity(BigDecimal.ZERO);
      created.setVersion(0);
      try {
        inventoryMapper.insert(created);
      } catch (DuplicateKeyException ex) {
        throw new ServiceException("盘点期间库存已被其他业务创建，请重新盘点");
      }
    } else {
      locked = balance.getLockedQuantity();
      if (balance.getQuantity().compareTo(expectedBook) != 0)
        throw new ServiceException("盘点期间库存已发生变化，请重新盘点");
      if (actual.compareTo(locked) < 0) throw new ServiceException("盘点实际数量不能小于锁定数量");
      if (difference.signum() == 0) return;
      if (inventoryMapper.adjustForStocktake(
              balance.getId(), balance.getVersion(), expectedBook, actual)
          != 1) throw new ServiceException("盘点库存发生并发变化，请重新盘点");
    }
    InventoryTransactionCommand command = new InventoryTransactionCommand();
    command.setTransactionType(difference.signum() > 0 ? "STOCKTAKE_GAIN" : "STOCKTAKE_LOSS");
    command.setWarehouseId(warehouseId);
    command.setLocationId(locationId);
    command.setMaterialId(materialId);
    command.setQuantityBefore(expectedBook);
    command.setQuantityChange(difference);
    command.setQuantityAfter(actual);
    command.setLockedBefore(locked);
    command.setLockedChange(BigDecimal.ZERO);
    command.setLockedAfter(locked);
    command.setSourceType("STOCKTAKE_ORDER");
    command.setSourceId(sourceId);
    command.setSourceNo(sourceNo);
    command.setOccurredAt(occurredAt);
    command.setOperatorId(operatorId);
    transactionService.record(command);
  }
}
