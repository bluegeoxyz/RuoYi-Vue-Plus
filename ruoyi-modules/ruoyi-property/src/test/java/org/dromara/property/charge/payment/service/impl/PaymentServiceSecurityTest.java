package org.dromara.property.charge.payment.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.charge.payment.domain.*;
import org.dromara.property.charge.payment.domain.bo.PaymentCreateBo;
import org.dromara.property.charge.payment.domain.bo.PaymentQueryBo;
import org.dromara.property.charge.payment.domain.bo.PaymentReverseBo;
import org.dromara.property.charge.payment.mapper.*;
import org.dromara.property.charge.payment.service.PaymentAutoAllocationEngine;
import org.dromara.property.charge.receivable.domain.Receivable;
import org.dromara.property.charge.receivable.mapper.ReceivableMapper;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PaymentServiceSecurityTest {
 @Mock PaymentMapper paymentMapper; @Mock PaymentAllocationMapper allocationMapper; @Mock ReceivableMapper receivableMapper;
 @Mock PaymentReversalMapper reversalMapper;
 @Mock RoomMapper roomMapper; @Mock BuildingMapper buildingMapper; @Mock BuildingUnitMapper unitMapper;
 @Mock CustomerMapper customerMapper; @Mock ProjectMapper projectMapper; @Mock PropertyProjectScopeService scope; PaymentServiceImpl service;
 @BeforeEach void setUp(){service=new PaymentServiceImpl(paymentMapper,allocationMapper,reversalMapper,receivableMapper,roomMapper,buildingMapper,unitMapper,customerMapper,projectMapper,scope,new PaymentAutoAllocationEngine());}
 @Test void createUsesPersistedRoomProjectForWriteScope(){when(roomMapper.selectById(10L)).thenReturn(room(20L));doThrow(new ServiceException("deny")).when(scope).assertBusinessAccess(20L,PropertyBusinessDomain.FINANCE,ProjectBusinessAction.WRITE);assertThrows(ServiceException.class,()->service.create(bo(20L)));verify(receivableMapper,never()).selectPayableByRoomForUpdate(anyLong(),anyLong());}
 @Test void detailChecksPersistedProjectAgainstReadScope(){Payment p=new Payment();p.setId(1L);p.setProjectId(99L);when(paymentMapper.selectActiveById(1L)).thenReturn(p);doThrow(new ServiceException("deny")).when(scope).assertBusinessAccess(99L,PropertyBusinessDomain.FINANCE,ProjectBusinessAction.READ);assertThrows(ServiceException.class,()->service.queryById(1L));verify(allocationMapper,never()).selectList(any());}
 @Test void successfulCollectionUsesCustomerSnapshotAndUpdatesTwoReceivables(){when(roomMapper.selectById(10L)).thenReturn(room(20L));Customer c=new Customer();c.setId(30L);c.setCustomerName("李四");c.setCustomerType("OWNER");c.setStatus("0");c.setLifecycleStatus("ACTIVE");when(customerMapper.selectById(30L)).thenReturn(c);when(receivableMapper.selectPayableByRoomForUpdate(20L,10L)).thenReturn(List.of(receivable(2L,"2026-01-31"),receivable(1L,"2025-01-31")));when(paymentMapper.insert(any(Payment.class))).thenReturn(1);when(allocationMapper.insert(any(PaymentAllocation.class))).thenReturn(1);when(receivableMapper.updatePaymentState(anyLong(),any(),anyString())).thenReturn(1);when(paymentMapper.updateAllocatedAmount(anyLong(),any())).thenReturn(1);Payment saved=new Payment();saved.setProjectId(20L);when(paymentMapper.selectActiveById(anyLong())).thenReturn(saved);when(allocationMapper.selectList(any())).thenReturn(List.of());PaymentCreateBo b=bo(20L);b.setPayerCustomerId(30L);b.setPayerName("伪造姓名");b.setPaymentAmount(bd("3000"));b.setPaymentMethod("BANK_TRANSFER");service.create(b);ArgumentCaptor<Payment> cap=ArgumentCaptor.forClass(Payment.class);verify(paymentMapper).insert(cap.capture());assertEquals("李四",cap.getValue().getPayerNameSnapshot());assertEquals("OWNER",cap.getValue().getPayerTypeSnapshot());verify(receivableMapper).updatePaymentState(1L,bd("2400.00"),"PAID");verify(receivableMapper).updatePaymentState(2L,bd("600.00"),"PARTIAL");verify(paymentMapper).updateAllocatedAmount(anyLong(),eq(bd("3000.00")));}
 @Test void forgedRequestProjectIsRejectedBeforeMoneyChanges(){when(roomMapper.selectById(10L)).thenReturn(room(20L));assertThrows(ServiceException.class,()->service.create(bo(99L)));verifyNoInteractions(paymentMapper,allocationMapper,receivableMapper);}
 @Test void exportRequiresFinanceExportOnRequestedProject(){PaymentQueryBo query=new PaymentQueryBo();query.setProjectId(20L);doThrow(new ServiceException("deny")).when(scope).assertBusinessAccess(20L,PropertyBusinessDomain.FINANCE,ProjectBusinessAction.EXPORT);assertThrows(ServiceException.class,()->service.export(query));verify(paymentMapper,never()).selectList(any());}
 @Test void roomOptionsRequiresFinanceWriteOnRequestedProject(){doThrow(new ServiceException("deny")).when(scope).assertBusinessAccess(20L,PropertyBusinessDomain.FINANCE,ProjectBusinessAction.WRITE);assertThrows(ServiceException.class,()->service.roomOptions(20L));verify(paymentMapper,never()).selectCollectableRoomOptions(anyLong());}
 @Test void reverseUsesLockedPaymentProjectForWriteScope(){when(paymentMapper.selectActiveByIdForUpdate(1L)).thenReturn(payment(1L,99L,"VALID","100.00","100.00"));doThrow(new ServiceException("deny")).when(scope).assertBusinessAccess(99L,PropertyBusinessDomain.FINANCE,ProjectBusinessAction.WRITE);assertThrows(ServiceException.class,()->service.reverse(1L,reverseBo()));verify(allocationMapper,never()).selectByPaymentIdForUpdate(anyLong());}
 @Test void reverseRejectsAlreadyReversedPayment(){when(paymentMapper.selectActiveByIdForUpdate(1L)).thenReturn(payment(1L,20L,"REVERSED","100.00","100.00"));assertEquals("该收款已冲销，不能重复操作",assertThrows(ServiceException.class,()->service.reverse(1L,reverseBo())).getMessage());verifyNoInteractions(allocationMapper,receivableMapper,reversalMapper);}
 @Test void reverseTwoReceivablesAndPreservesPartialFromAnotherPayment(){Payment p=payment(1L,20L,"VALID","3000.00","3000.00");p.setRoomId(10L);when(paymentMapper.selectActiveByIdForUpdate(1L)).thenReturn(p);PaymentAllocation a1=allocation(11L,101L,"2400.00"),a2=allocation(12L,102L,"600.00");when(allocationMapper.selectByPaymentIdForUpdate(1L)).thenReturn(List.of(a1,a2));Receivable r1=receivableState(101L,"2400.00","2400.00","PAID"),r2=receivableState(102L,"2400.00","2400.00","PAID");when(receivableMapper.selectByIdsForUpdate(List.of(101L,102L))).thenReturn(List.of(r1,r2));when(receivableMapper.updatePaymentState(anyLong(),any(),anyString())).thenReturn(1);when(paymentMapper.markReversed(1L)).thenReturn(1);when(reversalMapper.insert(any(PaymentReversal.class))).thenReturn(1);var result=service.reverse(1L,reverseBo());assertEquals(2,result.getAffectedReceivableCount());verify(receivableMapper).updatePaymentState(101L,bd("0.00"),"UNPAID");verify(receivableMapper).updatePaymentState(102L,bd("1800.00"),"PARTIAL");verify(paymentMapper).markReversed(1L);verify(reversalMapper).insert(argThat((PaymentReversal r)->r.getPaymentId().equals(1L)&&r.getReversalAmount().compareTo(bd("3000.00"))==0));}
 @Test void reverseRejectsAllocationSumMismatchBeforeReceivableMutation(){when(paymentMapper.selectActiveByIdForUpdate(1L)).thenReturn(payment(1L,20L,"VALID","100.00","100.00"));when(allocationMapper.selectByPaymentIdForUpdate(1L)).thenReturn(List.of(allocation(11L,101L,"90.00")));assertTrue(assertThrows(ServiceException.class,()->service.reverse(1L,reverseBo())).getMessage().contains("金额不一致"));verify(receivableMapper,never()).selectByIdsForUpdate(any());}
 @Test void reverseRejectsCancelledOrInsufficientReceivable(){Payment p=payment(1L,20L,"VALID","100.00","100.00");p.setRoomId(10L);when(paymentMapper.selectActiveByIdForUpdate(1L)).thenReturn(p);when(allocationMapper.selectByPaymentIdForUpdate(1L)).thenReturn(List.of(allocation(11L,101L,"100.00")));Receivable cancelled=receivableState(101L,"100.00","100.00","CANCELLED");when(receivableMapper.selectByIdsForUpdate(List.of(101L))).thenReturn(List.of(cancelled));assertTrue(assertThrows(ServiceException.class,()->service.reverse(1L,reverseBo())).getMessage().contains("已作废"));cancelled.setPaymentStatus("PARTIAL");cancelled.setPaidAmount(bd("50.00"));assertTrue(assertThrows(ServiceException.class,()->service.reverse(1L,reverseBo())).getMessage().contains("小于核销金额"));verify(paymentMapper,never()).markReversed(anyLong());}
 private Room room(Long p){Room r=new Room();r.setId(10L);r.setProjectId(p);r.setRoomNo("101");return r;}
 private PaymentCreateBo bo(Long p){PaymentCreateBo b=new PaymentCreateBo();b.setProjectId(p);b.setRoomId(10L);b.setPayerName("李四");b.setPaymentAmount(BigDecimal.TEN);b.setPaymentMethod("CASH");b.setPaymentTime(LocalDateTime.now().minusMinutes(1));return b;}
 private Receivable receivable(long id,String due){Receivable r=new Receivable();r.setId(id);r.setReceivableNo("R"+id);r.setReceivableAmount(bd("2400.00"));r.setPaidAmount(bd("0.00"));r.setPaymentStatus("UNPAID");r.setDueDate(LocalDate.parse(due));r.setPeriodStartDate(LocalDate.parse(due).withDayOfMonth(1));return r;}
 private Payment payment(long id,long project,String status,String amount,String allocated){Payment p=new Payment();p.setId(id);p.setProjectId(project);p.setRoomId(10L);p.setPaymentNo("PAY"+id);p.setStatus(status);p.setPaymentAmount(bd(amount));p.setAllocatedAmount(bd(allocated));return p;}
 private PaymentAllocation allocation(long id,long receivable,String amount){PaymentAllocation a=new PaymentAllocation();a.setId(id);a.setProjectId(20L);a.setPaymentId(1L);a.setReceivableId(receivable);a.setAllocationAmount(bd(amount));return a;}
 private Receivable receivableState(long id,String amount,String paid,String status){Receivable r=new Receivable();r.setId(id);r.setProjectId(20L);r.setRoomId(10L);r.setReceivableNo("R"+id);r.setReceivableAmount(bd(amount));r.setPaidAmount(bd(paid));r.setPaymentStatus(status);return r;}
 private PaymentReverseBo reverseBo(){PaymentReverseBo b=new PaymentReverseBo();b.setReason("收款录错");return b;}
 private BigDecimal bd(String v){return new BigDecimal(v);}
}
