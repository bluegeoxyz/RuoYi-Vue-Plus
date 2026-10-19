package org.dromara.property.customer.importer.service.impl;

import jakarta.validation.Validation;
import org.dromara.property.dict.PropertyDictionaryService;
import org.dromara.property.dict.PropertyDictTypes;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.importer.domain.CustomerImportRow;
import org.dromara.property.customer.service.CustomerAccessPolicy;
import org.dromara.property.customer.service.CustomerAction;
import org.dromara.property.customer.service.CustomerCreateExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerImportServiceImplTest {
    private final CustomerAccessPolicy policy=mock(CustomerAccessPolicy.class);
    private final CustomerCreateExecutor executor=mock(CustomerCreateExecutor.class);
    private final TransactionTemplate transaction=mock(TransactionTemplate.class);
    private final PropertyDictionaryService dictionaryService=mock(PropertyDictionaryService.class);
    private final CustomerImportServiceImpl service=new CustomerImportServiceImpl(policy,executor,
        Validation.buildDefaultValidatorFactory().getValidator(),dictionaryService,transaction,List.of());

    CustomerImportServiceImplTest(){when(dictionaryService.resolveInput(anyString(),anyString())).thenAnswer(invocation->invocation.getArgument(1));}

    @Test void invalidRowCollectsErrorsAndNeverWrites(){CustomerImportRow row=new CustomerImportRow();row.setCustomerType("PERSON");var result=service.importRows(List.of(row));assertEquals(0,result.getSuccessRows());assertEquals(1,result.getFailedRows());assertFalse(result.getErrors().isEmpty());verify(executor,never()).insert(any());}
    @Test void overLimitStopsBeforeSecurityPreparation(){List<CustomerImportRow> rows=new ArrayList<>();for(int i=0;i<2001;i++)rows.add(valid(i));var result=service.importRows(rows);assertEquals(2001,result.getFailedRows());verify(executor,never()).prepare(any(),anyBoolean());}
    @Test void normalizedIdentityDuplicateIsReportedWithoutIdentityValue(){Customer fingerprinted=new Customer();fingerprinted.setIdType("ID_CARD");fingerprinted.setIdentityFingerprint(new byte[32]);when(executor.prepare(any(),eq(true))).thenReturn(fingerprinted);var result=service.importRows(List.of(valid(1),valid(2)));assertEquals(2,result.getFailedRows());assertTrue(result.getErrors().stream().anyMatch(e->e.getMessage().contains("与第 2 行重复")));assertTrue(result.getErrors().stream().noneMatch(e->e.getMessage().contains("T14C-ID")));}
    @Test void permissionIsCheckedBeforeRows(){doThrow(new org.dromara.common.core.exception.ServiceException("denied")).when(policy).assertEnterpriseMasterAccess(CustomerAction.IMPORT);assertThrows(org.dromara.common.core.exception.ServiceException.class,()->service.importRows(List.of(valid(1))));verifyNoInteractions(executor);}
    @Test void chineseCustomerTypeIsConvertedToStableCode(){CustomerImportRow row=valid(1);row.setCustomerType("个人");when(dictionaryService.resolveInput(PropertyDictTypes.CUSTOMER_TYPE,"个人")).thenReturn("PERSON");when(executor.prepare(any(),eq(true))).thenReturn(new Customer());var result=service.importRows(List.of(row));assertEquals(1,result.getSuccessRows());var captor=org.mockito.ArgumentCaptor.forClass(org.dromara.property.customer.domain.bo.CustomerCreateBo.class);verify(executor).prepare(captor.capture(),eq(true));assertEquals("PERSON",captor.getValue().getCustomerType());}
    @Test void invalidChineseCustomerTypeRejectsWholeImport(){CustomerImportRow row=valid(1);row.setCustomerType("住户");when(dictionaryService.resolveInput(PropertyDictTypes.CUSTOMER_TYPE,"住户")).thenReturn(null);var result=service.importRows(List.of(row));assertEquals(0,result.getSuccessRows());assertTrue(result.getErrors().stream().anyMatch(e->e.getMessage().equals("客户类型“住户”不是有效值")));verify(executor,never()).insert(any());}
    private CustomerImportRow valid(int i){CustomerImportRow r=new CustomerImportRow();r.setCustomerType("PERSON");r.setCustomerName("Test"+i);r.setIdType("ID_CARD");r.setIdNo("T14C-ID-"+i);return r;}
}
