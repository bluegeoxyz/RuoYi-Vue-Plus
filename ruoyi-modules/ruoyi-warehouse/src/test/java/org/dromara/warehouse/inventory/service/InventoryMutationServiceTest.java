package org.dromara.warehouse.inventory.service;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.transaction.domain.command.InventoryTransactionCommand;
import org.dromara.warehouse.inventory.transaction.service.IInventoryTransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 原子库存增加与流水联动测试。 */
@ExtendWith(MockitoExtension.class)
class InventoryMutationServiceTest {
    @Mock InventoryMapper inventoryMapper; @Mock IInventoryService inventoryService;
    @Mock IInventoryTransactionService transactionService; @InjectMocks InventoryMutationService service;

    @Test void firstInboundCreatesBalanceAndTransaction() {
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(null);
        service.inbound(1L,2L,3L,new BigDecimal("100"),9L,"RK9",null,8L);
        ArgumentCaptor<Inventory> balance = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryMapper).insert(balance.capture());
        assertEquals(0, balance.getValue().getQuantity().compareTo(new BigDecimal("100")));
        assertEquals(BigDecimal.ZERO, balance.getValue().getLockedQuantity());
        ArgumentCaptor<InventoryTransactionCommand> transaction = ArgumentCaptor.forClass(InventoryTransactionCommand.class);
        verify(transactionService).record(transaction.capture());
        assertEquals("INBOUND_ORDER", transaction.getValue().getSourceType());
        assertEquals(0, transaction.getValue().getQuantityBefore().compareTo(BigDecimal.ZERO));
        assertEquals(0, transaction.getValue().getQuantityAfter().compareTo(new BigDecimal("100")));
    }

    @Test void existingBalanceUsesVersionCasAndKeepsLockedQuantity() {
        Inventory balance = new Inventory(); balance.setId(7L); balance.setQuantity(new BigDecimal("100"));
        balance.setLockedQuantity(new BigDecimal("20")); balance.setVersion(4);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(balance);
        when(inventoryMapper.incrementQuantity(7L,4,new BigDecimal("50"))).thenReturn(1);
        service.inbound(1L,2L,3L,new BigDecimal("50"),9L,"RK9",null,8L);
        ArgumentCaptor<InventoryTransactionCommand> transaction = ArgumentCaptor.forClass(InventoryTransactionCommand.class);
        verify(transactionService).record(transaction.capture());
        assertEquals(0, transaction.getValue().getQuantityAfter().compareTo(new BigDecimal("150")));
        assertEquals(0, transaction.getValue().getLockedAfter().compareTo(new BigDecimal("20")));
    }

    @Test void casConflictFailsWithoutWritingTransaction() {
        Inventory balance = new Inventory(); balance.setId(7L); balance.setQuantity(BigDecimal.TEN); balance.setLockedQuantity(BigDecimal.ZERO); balance.setVersion(1);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(balance);
        assertThrows(ServiceException.class, () -> service.inbound(1L,2L,3L,BigDecimal.ONE,9L,"RK9",null,8L));
        verify(transactionService, never()).record(any());
    }

    @Test void firstBalanceDuplicateFailsExplicitly() {
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(null);
        doThrow(new DuplicateKeyException("duplicate")).when(inventoryMapper).insert(any(Inventory.class));
        assertThrows(ServiceException.class, () -> service.inbound(1L,2L,3L,BigDecimal.ONE,9L,"RK9",null,8L));
        verify(transactionService, never()).record(any());
    }

    @Test void mutationRequiresOuterTransaction() throws Exception {
        Transactional annotation = InventoryMutationService.class.getMethod("inbound", Long.class, Long.class, Long.class,
            BigDecimal.class, Long.class, String.class, java.time.LocalDateTime.class, Long.class).getAnnotation(Transactional.class);
        assertEquals(Propagation.MANDATORY, annotation.propagation());
    }

    @Test void outboundUsesAvailableQuantityAndWritesNegativeTransaction() {
        Inventory balance = new Inventory(); balance.setId(7L); balance.setQuantity(new BigDecimal("10")); balance.setLockedQuantity(new BigDecimal("2")); balance.setVersion(3);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(balance);
        when(inventoryMapper.decrementAvailable(7L,3,new BigDecimal("8"))).thenReturn(1);
        service.outbound(1L,2L,3L,new BigDecimal("8"),9L,"CK9",null,8L);
        ArgumentCaptor<InventoryTransactionCommand> captor=ArgumentCaptor.forClass(InventoryTransactionCommand.class);verify(transactionService).record(captor.capture());
        assertEquals("OUTBOUND",captor.getValue().getTransactionType());assertEquals(new BigDecimal("-8"),captor.getValue().getQuantityChange());assertEquals(new BigDecimal("2"),captor.getValue().getQuantityAfter());assertEquals("OUTBOUND_ORDER",captor.getValue().getSourceType());
    }

    @Test void outboundRejectsMissingAndLockedInsufficientBalance() {
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(null);
        assertThrows(ServiceException.class,()->service.outbound(1L,2L,3L,BigDecimal.ONE,9L,"CK9",null,8L));
        Inventory balance=new Inventory();balance.setQuantity(BigDecimal.TEN);balance.setLockedQuantity(new BigDecimal("4"));balance.setVersion(1);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(balance);
        assertThrows(ServiceException.class,()->service.outbound(1L,2L,3L,new BigDecimal("7"),9L,"CK9",null,8L));
        verify(inventoryMapper,never()).decrementAvailable(anyLong(),anyInt(),any());
    }

    @Test void outboundCasFailureCannotOversell() {
        Inventory initial=new Inventory();initial.setId(7L);initial.setQuantity(BigDecimal.TEN);initial.setLockedQuantity(BigDecimal.ZERO);initial.setVersion(1);
        Inventory latest=new Inventory();latest.setQuantity(new BigDecimal("2"));latest.setLockedQuantity(BigDecimal.ZERO);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(initial,latest);
        when(inventoryMapper.decrementAvailable(7L,1,new BigDecimal("8"))).thenReturn(0);
        assertThrows(ServiceException.class,()->service.outbound(1L,2L,3L,new BigDecimal("8"),9L,"CK9",null,8L));
        verify(transactionService,never()).record(any());
    }

    @Test void transferOutWritesPairedSourceMetadata() {
        Inventory balance=new Inventory();balance.setId(7L);balance.setQuantity(BigDecimal.TEN);balance.setLockedQuantity(BigDecimal.ZERO);balance.setVersion(1);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(balance);when(inventoryMapper.decrementAvailable(7L,1,new BigDecimal("4"))).thenReturn(1);
        service.transferOut(1L,2L,3L,new BigDecimal("4"),9L,"DB9",null,8L);
        ArgumentCaptor<InventoryTransactionCommand> c=ArgumentCaptor.forClass(InventoryTransactionCommand.class);verify(transactionService).record(c.capture());
        assertEquals("TRANSFER_OUT",c.getValue().getTransactionType());assertEquals("TRANSFER_ORDER",c.getValue().getSourceType());assertEquals(new BigDecimal("-4"),c.getValue().getQuantityChange());
    }

    @Test void firstTransferInCreatesTargetWithZeroBefore() {
        when(inventoryMapper.selectBalance(4L,5L,3L)).thenReturn(null);
        service.transferIn(4L,5L,3L,new BigDecimal("4"),9L,"DB9",null,8L);
        ArgumentCaptor<InventoryTransactionCommand> c=ArgumentCaptor.forClass(InventoryTransactionCommand.class);verify(transactionService).record(c.capture());
        assertEquals("TRANSFER_IN",c.getValue().getTransactionType());assertEquals(BigDecimal.ZERO,c.getValue().getQuantityBefore());assertEquals(new BigDecimal("4"),c.getValue().getQuantityAfter());
    }

    @Test void stocktakeGainAdjustsBalanceAndWritesGainTransaction() {
        Inventory balance=new Inventory();balance.setId(7L);balance.setQuantity(BigDecimal.TEN);balance.setLockedQuantity(new BigDecimal("2"));balance.setVersion(3);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(balance);
        when(inventoryMapper.adjustForStocktake(7L,3,BigDecimal.TEN,new BigDecimal("12"))).thenReturn(1);
        service.adjustForStocktake(1L,2L,3L,BigDecimal.TEN,new BigDecimal("12"),9L,"PD9",null,8L);
        ArgumentCaptor<InventoryTransactionCommand> c=ArgumentCaptor.forClass(InventoryTransactionCommand.class);verify(transactionService).record(c.capture());
        assertEquals("STOCKTAKE_GAIN",c.getValue().getTransactionType());assertEquals(new BigDecimal("2"),c.getValue().getQuantityChange());assertEquals("STOCKTAKE_ORDER",c.getValue().getSourceType());
    }

    @Test void stocktakeLossCannotReduceBelowLockedQuantity() {
        Inventory balance=new Inventory();balance.setId(7L);balance.setQuantity(BigDecimal.TEN);balance.setLockedQuantity(new BigDecimal("6"));balance.setVersion(3);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(balance);
        assertThrows(ServiceException.class,()->service.adjustForStocktake(1L,2L,3L,BigDecimal.TEN,new BigDecimal("5"),9L,"PD9",null,8L));
        verify(inventoryMapper,never()).adjustForStocktake(anyLong(),anyInt(),any(),any());verify(transactionService,never()).record(any());
    }

    @Test void stocktakeZeroDifferenceDoesNotWriteTransaction() {
        Inventory balance=new Inventory();balance.setId(7L);balance.setQuantity(BigDecimal.TEN);balance.setLockedQuantity(BigDecimal.ZERO);balance.setVersion(3);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(balance);
        service.adjustForStocktake(1L,2L,3L,BigDecimal.TEN,BigDecimal.TEN,9L,"PD9",null,8L);
        verify(inventoryMapper,never()).adjustForStocktake(anyLong(),anyInt(),any(),any());verify(transactionService,never()).record(any());
    }

    @Test void firstStocktakeGainCreatesBalanceFromZero() {
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(null);
        service.adjustForStocktake(1L,2L,3L,BigDecimal.ZERO,new BigDecimal("3"),9L,"PD9",null,8L);
        ArgumentCaptor<Inventory> b=ArgumentCaptor.forClass(Inventory.class);verify(inventoryMapper).insert(b.capture());assertEquals(new BigDecimal("3"),b.getValue().getQuantity());
        ArgumentCaptor<InventoryTransactionCommand> c=ArgumentCaptor.forClass(InventoryTransactionCommand.class);verify(transactionService).record(c.capture());assertEquals("STOCKTAKE_GAIN",c.getValue().getTransactionType());
    }

    @Test void stocktakeRejectsChangedBookAndCasConflict() {
        Inventory changed=new Inventory();changed.setId(7L);changed.setQuantity(new BigDecimal("9"));changed.setLockedQuantity(BigDecimal.ZERO);changed.setVersion(3);
        when(inventoryMapper.selectBalance(1L,2L,3L)).thenReturn(changed);
        assertThrows(ServiceException.class,()->service.adjustForStocktake(1L,2L,3L,BigDecimal.TEN,new BigDecimal("11"),9L,"PD9",null,8L));
        changed.setQuantity(BigDecimal.TEN);
        assertThrows(ServiceException.class,()->service.adjustForStocktake(1L,2L,3L,BigDecimal.TEN,new BigDecimal("11"),9L,"PD9",null,8L));
        verify(transactionService,never()).record(any());
    }

    @Test void stocktakeMutationRequiresOuterTransaction() throws Exception {
        Transactional annotation=InventoryMutationService.class.getMethod("adjustForStocktake",Long.class,Long.class,Long.class,BigDecimal.class,BigDecimal.class,Long.class,String.class,java.time.LocalDateTime.class,Long.class).getAnnotation(Transactional.class);
        assertEquals(Propagation.MANDATORY,annotation.propagation());
    }
}
