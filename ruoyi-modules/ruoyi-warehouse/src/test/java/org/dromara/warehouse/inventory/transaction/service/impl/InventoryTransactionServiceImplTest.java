package org.dromara.warehouse.inventory.transaction.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.warehouse.inventory.service.IInventoryService;
import org.dromara.warehouse.inventory.transaction.domain.InventoryTransaction;
import org.dromara.warehouse.inventory.transaction.domain.command.InventoryTransactionCommand;
import org.dromara.warehouse.inventory.transaction.mapper.InventoryTransactionMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 库存流水不可变性、数量关系和数据权限测试。 */
@ExtendWith(MockitoExtension.class)
class InventoryTransactionServiceImplTest {
    @Mock InventoryTransactionMapper transactionMapper;
    @Mock IInventoryService inventoryService;
    @Mock WarehouseAccessService accessService;
    @Mock ProjectReferenceService projectReferenceService;
    @InjectMocks InventoryTransactionServiceImpl service;

    @Test void recordRequiresExistingBusinessTransaction() throws Exception {
        Transactional annotation = InventoryTransactionServiceImpl.class
            .getMethod("record", InventoryTransactionCommand.class).getAnnotation(Transactional.class);
        assertNotNull(annotation);
        assertEquals(Propagation.MANDATORY, annotation.propagation());
    }

    @Test void quantityChangeCannotBeZero() {
        assertThrows(ServiceException.class, () -> service.validate(command("INBOUND", "1", "0", "1")));
    }

    @Test void quantitiesCannotBecomeNegative() {
        assertThrows(ServiceException.class, () -> service.validate(command("OUTBOUND", "1", "-2", "-1")));
    }

    @Test void quantityEquationMustBalance() {
        assertThrows(ServiceException.class, () -> service.validate(command("INBOUND", "1", "2", "4")));
    }

    @Test void positiveTypesAcceptOnlyPositiveChanges() {
        for (String type : List.of("INBOUND", "TRANSFER_IN", "STOCKTAKE_GAIN")) {
            assertDoesNotThrow(() -> service.validate(command(type, "1", "2", "3")));
            assertThrows(ServiceException.class, () -> service.validate(command(type, "3", "-1", "2")));
        }
    }

    @Test void negativeTypesAcceptOnlyNegativeChanges() {
        for (String type : List.of("OUTBOUND", "TRANSFER_OUT", "STOCKTAKE_LOSS")) {
            assertDoesNotThrow(() -> service.validate(command(type, "3", "-1", "2")));
            assertThrows(ServiceException.class, () -> service.validate(command(type, "1", "1", "2")));
        }
    }

    @Test void phaseOneRejectsLockedQuantityChanges() {
        InventoryTransactionCommand command = command("INBOUND", "1", "2", "3");
        command.setLockedChange(BigDecimal.ONE);
        command.setLockedAfter(BigDecimal.ONE);
        assertThrows(ServiceException.class, () -> service.validate(command));
    }

    @Test void detailChecksWarehouseAccessBeforeReturningJoinedData() {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setWarehouseId(2L);
        when(transactionMapper.selectById(9L)).thenReturn(transaction);
        doThrow(new ServiceException("denied")).when(accessService).requireWarehouse(2L);
        assertThrows(ServiceException.class, () -> service.queryById(9L));
        verify(transactionMapper, never()).selectTransactionById(anyLong());
    }

    private InventoryTransactionCommand command(String type, String before, String change, String after) {
        InventoryTransactionCommand command = new InventoryTransactionCommand();
        command.setTransactionType(type);
        command.setWarehouseId(1L);
        command.setLocationId(2L);
        command.setMaterialId(3L);
        command.setQuantityBefore(new BigDecimal(before));
        command.setQuantityChange(new BigDecimal(change));
        command.setQuantityAfter(new BigDecimal(after));
        command.setOperatorId(88L);
        return command;
    }
}
