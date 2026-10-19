package org.dromara.property.customer.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.domain.bo.CustomerCreateBo;
import org.dromara.property.customer.domain.bo.CustomerLifecycleBo;
import org.dromara.property.customer.domain.bo.CustomerUpdateBo;
import org.dromara.property.customer.domain.vo.CustomerDetailVo;
import org.dromara.property.customer.domain.vo.CustomerSensitiveDetailVo;
import org.dromara.property.customer.domain.bo.CustomerQueryBo;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.security.CustomerIdentityFingerprintService;
import org.dromara.property.customer.security.CustomerSecurityProperties;
import org.dromara.property.customer.service.CustomerAccessPolicy;
import org.dromara.property.customer.service.CustomerAction;
import org.dromara.property.customer.service.CustomerCreateExecutor;
import org.dromara.property.customer.service.CustomerSensitiveAction;
import org.dromara.property.customer.service.CustomerSensitiveField;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.dromara.property.customer.security.CustomerFieldEncryptionService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {
    private final CustomerMapper mapper = mock(CustomerMapper.class);
    private final CustomerAccessPolicy policy = mock(CustomerAccessPolicy.class);
    private final CustomerSecurityProperties securityProperties = new CustomerSecurityProperties();
    private final CustomerFieldEncryptionService fieldEncryptionService = mock(CustomerFieldEncryptionService.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<CustomerIdentityFingerprintService> fingerprintProvider = mock(ObjectProvider.class);
    private final CustomerCreateExecutor createExecutor = new CustomerCreateExecutor(mapper, securityProperties,
        fingerprintProvider, fieldEncryptionService);
    private final CustomerServiceImpl service = new CustomerServiceImpl(mapper, policy, createExecutor);

    @Test
    void createRequiresGrantBeforeSecurityOrDatabaseAccess() {
        CustomerCreateBo bo = createBo();
        doThrow(new ServiceException("denied")).when(policy).assertEnterpriseMasterAccess(CustomerAction.CREATE);

        assertThrows(ServiceException.class, () -> service.create(bo));

        verifyNoInteractions(mapper, fingerprintProvider);
    }

    @Test
    void createComputesFingerprintAndReturnsNoIdentityPlaintext() {
        securityProperties.setEnabled(true);
        securityProperties.setKeyVersion("test-v1");
        CustomerIdentityFingerprintService fingerprint = mock(CustomerIdentityFingerprintService.class);
        when(fingerprintProvider.getIfAvailable()).thenReturn(fingerprint);
        when(fingerprint.normalizeIdType("id_card")).thenReturn("ID_CARD");
        when(fingerprint.normalizeIdNo("110101199001011234")).thenReturn("110101199001011234");
        when(fingerprint.fingerprint("ID_CARD", "110101199001011234")).thenReturn(new byte[32]);
        when(fieldEncryptionService.encryptIdNo("110101199001011234")).thenReturn("ENC_ciphertext");
        when(mapper.countActiveIdentity(eq("ID_CARD"), any(byte[].class))).thenReturn(0L);
        when(mapper.insert(any(Customer.class))).thenReturn(1);

        var result = service.create(createBo());

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(mapper).insert(captor.capture());
        Customer inserted = captor.getValue();
        assertEquals("ID_CARD", inserted.getIdType());
        assertNotEquals("110101199001011234", inserted.getIdNo());
        assertTrue(inserted.getIdNo().startsWith("ENC_"));
        assertArrayEquals(new byte[32], inserted.getIdentityFingerprint());
        assertEquals("test-v1", inserted.getPiiKeyVersion());
        assertFalse(result.toString().contains("110101199001011234"));
    }

    @Test
    void encryptionFailureStopsMapperWrite() {
        securityProperties.setEnabled(true);
        securityProperties.setKeyVersion("test-v1");
        CustomerIdentityFingerprintService fingerprint = mock(CustomerIdentityFingerprintService.class);
        when(fingerprintProvider.getIfAvailable()).thenReturn(fingerprint);
        when(fingerprint.normalizeIdType(anyString())).thenReturn("ID_CARD");
        when(fingerprint.normalizeIdNo(anyString())).thenReturn("110101199001011234");
        when(fieldEncryptionService.encryptIdNo(anyString())).thenThrow(new IllegalStateException("encryption failed"));

        assertThrows(IllegalStateException.class, () -> service.create(createBo()));

        verify(mapper, never()).insert(any(Customer.class));
    }

    @Test
    void customerNumberConflictIsNotReportedAsIdentityDuplicate() {
        securityProperties.setEnabled(true);
        securityProperties.setKeyVersion("test-v1");
        CustomerIdentityFingerprintService fingerprint = mock(CustomerIdentityFingerprintService.class);
        when(fingerprintProvider.getIfAvailable()).thenReturn(fingerprint);
        when(fingerprint.normalizeIdType(anyString())).thenReturn("ID_CARD");
        when(fingerprint.normalizeIdNo(anyString())).thenReturn("110101199001011234");
        when(fingerprint.fingerprint(anyString(), anyString())).thenReturn(new byte[32]);
        when(fieldEncryptionService.encryptIdNo(anyString())).thenReturn("ENC_ciphertext");
        when(mapper.insert(any(Customer.class))).thenThrow(new org.springframework.dao.DuplicateKeyException(
            "Duplicate entry for key 'uk_pm_customer_no'"));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.create(createBo()));

        assertEquals("客户编号冲突，请重新提交", exception.getMessage());
    }

    @Test
    void ordinaryUpdateUsesOptimisticVersionAndCannotWriteIdentityFields() {
        Customer current = customer();
        current.setVersion(3);
        when(mapper.selectById(10L)).thenReturn(current);
        when(mapper.updateById(any(Customer.class))).thenReturn(1);
        CustomerUpdateBo bo = new CustomerUpdateBo();
        bo.setId(10L);
        bo.setVersion(3);
        bo.setCustomerName("李四");

        assertTrue(service.update(bo));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(mapper).updateById(captor.capture());
        assertNull(captor.getValue().getIdType());
        assertNull(captor.getValue().getIdNo());
        assertNull(captor.getValue().getIdentityFingerprint());
        assertEquals(3, captor.getValue().getVersion());
    }

    @Test
    void lifecycleKeepsStatusConsistentAndCancelledIsTerminal() {
        Customer current = customer();
        current.setVersion(1);
        when(mapper.selectActiveByIdForUpdate(10L)).thenReturn(current);
        when(mapper.updateById(any(Customer.class))).thenReturn(1);
        CustomerLifecycleBo bo = new CustomerLifecycleBo();
        bo.setId(10L);
        bo.setVersion(1);
        bo.setLifecycleStatus("INACTIVE");

        assertTrue(service.changeLifecycle(bo));
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(mapper).updateById(captor.capture());
        assertEquals("1", captor.getValue().getStatus());
        assertEquals("INACTIVE", captor.getValue().getLifecycleStatus());

        current.setLifecycleStatus("CANCELLED");
        bo.setLifecycleStatus("ACTIVE");
        assertThrows(ServiceException.class, () -> service.changeLifecycle(bo));
    }

    @Test
    void anyHistoricalRelationPreventsLogicalDelete() {
        Customer current = customer();
        current.setVersion(2);
        when(mapper.selectActiveByIdForUpdate(10L)).thenReturn(current);
        when(mapper.selectAllRelationIdsForUpdate(10L)).thenReturn(java.util.List.of(99L));

        assertThrows(ServiceException.class, () -> service.delete(10L, 2));

        verify(mapper, never()).logicalDelete(anyLong(), anyInt());
    }

    @Test
    void defaultDetailReturnsOnlyMaskedPiiAndNoSecurityMetadata() {
        Customer customer = customer();
        when(mapper.selectById(10L)).thenReturn(customer);

        CustomerDetailVo result = service.queryById(10L);

        assertNotEquals(customer.getCustomerName(), result.getCustomerName());
        assertEquals("138****8000", result.getMobile());
        assertNotEquals(customer.getIdNo(), result.getIdNo());
        assertFalse(result.getIdNo().contains("110101199001011234"));
        verify(policy).assertCustomerVisible(10L, CustomerAction.DETAIL);
    }

    @Test
    void sensitiveDetailAuditsBeforeReturningPlaintext() {
        Customer customer = customer();
        when(mapper.selectById(10L)).thenReturn(customer);

        CustomerSensitiveDetailVo result = service.querySensitiveById(10L, "trace-1");

        assertEquals("110101199001011234", result.getIdNo());
        assertEquals("13800138000", result.getMobile());
        InOrder order = inOrder(policy, mapper);
        order.verify(policy).assertCustomerVisible(10L, CustomerAction.DETAIL);
        order.verify(mapper).selectById(10L);
        order.verify(policy).assertSensitiveAccess(10L, CustomerSensitiveAction.PII_VIEW,
            CustomerSensitiveField.CONTACT_AND_IDENTITY, "CUSTOMER_DETAIL", "trace-1", null);
    }

    @Test
    void auditFailurePreventsSensitiveResponse() {
        when(mapper.selectById(10L)).thenReturn(customer());
        doThrow(new ServiceException("audit failed")).when(policy).assertSensitiveAccess(
            10L, CustomerSensitiveAction.PII_VIEW, CustomerSensitiveField.CONTACT_AND_IDENTITY,
            "CUSTOMER_DETAIL", "trace-2", null);

        assertThrows(ServiceException.class, () -> service.querySensitiveById(10L, "trace-2"));
    }

    @Test
    void maskedExportUsesExportGrantAndNeverMapsPlainPii() {
        when(mapper.selectCount(any())).thenReturn(1L);
        when(mapper.selectList(any())).thenReturn(java.util.List.of(customer()));

        var rows = service.queryMaskedExport(new CustomerQueryBo());

        assertEquals(1, rows.size());
        assertEquals("138****8000", rows.getFirst().getMobile());
        assertNotEquals("110101199001011234", rows.getFirst().getIdNo());
        verify(policy).assertEnterpriseMasterAccess(CustomerAction.EXPORT);
    }

    @Test
    void sensitiveExportAuditsBeforePlainPiiMappingAndAuditFailureStopsIt() {
        when(mapper.selectCount(any())).thenReturn(1L);
        when(mapper.selectList(any())).thenReturn(java.util.List.of(customer()));
        CustomerQueryBo query = new CustomerQueryBo();
        query.setStatus("0");

        var rows = service.querySensitiveExport(query, "trace-export");

        assertEquals("110101199001011234", rows.getFirst().getIdNo());
        verify(policy).assertSensitiveExportAccess(1, 1, "trace-export");

        doThrow(new ServiceException("audit failed")).when(policy)
            .assertSensitiveExportAccess(1, 1, "trace-failed");
        assertThrows(ServiceException.class, () -> service.querySensitiveExport(query, "trace-failed"));
        verify(mapper, times(1)).selectList(any());
    }

    @Test
    void exportLimitIsCheckedBeforeRowsAreLoaded() {
        when(mapper.selectCount(any())).thenReturn(5_001L);

        assertThrows(ServiceException.class, () -> service.queryMaskedExport(new CustomerQueryBo()));

        verify(mapper, never()).selectList(any());
    }

    @Test
    void databaseFailureAfterAuditRecordsTerminalFailure() {
        when(mapper.selectCount(any())).thenReturn(1L);
        when(mapper.selectList(any())).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class,
            () -> service.querySensitiveExport(new CustomerQueryBo(), "trace-db-failed"));

        verify(policy).assertSensitiveExportAccess(1, 0, "trace-db-failed");
        verify(policy).recordSensitiveExportFailure(1, "trace-db-failed");
    }

    @Test
    void authorizationIsCheckedBeforeEncryptedEntityIsQueried() {
        doThrow(new ServiceException("denied")).when(policy).assertCustomerVisible(10L, CustomerAction.DETAIL);

        assertThrows(ServiceException.class, () -> service.queryById(10L));
        verifyNoInteractions(mapper);
    }

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId(10L);
        customer.setCustomerNo("C001");
        customer.setCustomerType("PERSON");
        customer.setCustomerName("张三");
        customer.setMobile("13800138000");
        customer.setPhone("01012345678");
        customer.setEmail("customer@example.com");
        customer.setIdType("ID_CARD");
        customer.setIdNo("110101199001011234");
        customer.setStatus("0");
        customer.setLifecycleStatus("ACTIVE");
        customer.setIdentityFingerprint(new byte[32]);
        customer.setPiiKeyVersion("test-v1");
        return customer;
    }

    private CustomerCreateBo createBo() {
        CustomerCreateBo bo = new CustomerCreateBo();
        bo.setCustomerType("PERSON");
        bo.setCustomerName("张三");
        bo.setIdType("id_card");
        bo.setIdNo("110101199001011234");
        return bo;
    }
}
