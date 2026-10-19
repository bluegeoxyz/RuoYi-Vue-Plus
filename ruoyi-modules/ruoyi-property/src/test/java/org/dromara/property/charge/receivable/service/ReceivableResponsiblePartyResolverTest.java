package org.dromara.property.charge.receivable.service;
import org.dromara.common.core.exception.ServiceException; import org.dromara.property.charge.receivable.mapper.*; import org.junit.jupiter.api.Test; import java.time.LocalDate; import java.util.List; import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
class ReceivableResponsiblePartyResolverTest { private final ReceivableGenerationMapper mapper=mock(ReceivableGenerationMapper.class); private final ReceivableResponsiblePartyResolver resolver=new ReceivableResponsiblePartyResolver(mapper);
 @Test void uniqueOwnerIsResolved(){ResponsibleCustomerSnapshot owner=owner(1L,"张三","PERSON");when(mapper.selectOwnersOnDate(7L,LocalDate.of(2026,5,1))).thenReturn(List.of(owner));assertSame(owner,resolver.resolve(7L,LocalDate.of(2026,5,1)));}
 @Test void missingOwnerFails(){when(mapper.selectOwnersOnDate(anyLong(),any())).thenReturn(List.of());assertEquals("收费周期开始日不存在有效业主",assertThrows(ServiceException.class,()->resolver.resolve(7L,LocalDate.now())).getMessage());}
 @Test void multipleOwnersFailRatherThanSelectingFirst(){when(mapper.selectOwnersOnDate(anyLong(),any())).thenReturn(List.of(owner(1L,"甲","PERSON"),owner(2L,"乙","PERSON")));assertEquals("收费周期开始日责任主体不唯一",assertThrows(ServiceException.class,()->resolver.resolve(7L,LocalDate.now())).getMessage());}
 private ResponsibleCustomerSnapshot owner(Long id,String name,String type){ResponsibleCustomerSnapshot x=new ResponsibleCustomerSnapshot();x.setCustomerId(id);x.setCustomerName(name);x.setCustomerType(type);return x;}
}
