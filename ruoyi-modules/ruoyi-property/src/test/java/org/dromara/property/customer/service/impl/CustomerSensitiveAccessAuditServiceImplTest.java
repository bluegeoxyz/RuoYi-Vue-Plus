package org.dromara.property.customer.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.customer.domain.CustomerSensitiveAccessAudit;
import org.dromara.property.customer.mapper.CustomerSensitiveAccessAuditMapper;
import org.dromara.property.customer.service.CustomerSensitiveAction;
import org.dromara.property.customer.service.CustomerSensitiveField;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerSensitiveAccessAuditServiceImplTest {
    private final CustomerSensitiveAccessAuditMapper mapper = mock(CustomerSensitiveAccessAuditMapper.class);
    private final CustomerSensitiveAccessAuditServiceImpl service = new CustomerSensitiveAccessAuditServiceImpl(mapper);

    @Test void auditMustContainMetadataOnly() {
        when(mapper.insert(any(CustomerSensitiveAccessAudit.class))).thenReturn(1);
        service.recordRequired(10L, 20L, CustomerSensitiveAction.PII_VIEW,
            CustomerSensitiveField.IDENTITY, "CUSTOMER_DETAIL", "ALLOW", "trace-1", 30L, "AUTHORIZED");
        ArgumentCaptor<CustomerSensitiveAccessAudit> captor = ArgumentCaptor.forClass(CustomerSensitiveAccessAudit.class);
        verify(mapper).insert(captor.capture());
        String serializedMetadata = captor.getValue().toString();
        assertFalse(serializedMetadata.contains("320101"));
        assertFalse(serializedMetadata.contains("1380000"));
        assertEquals("IDENTITY", captor.getValue().getFieldLevel());
        assertEquals("ALLOW", captor.getValue().getDecision());
    }

    @Test void failedAuditMustFailClosedAndUnsafeReasonMustBeRedacted() {
        when(mapper.insert(any(CustomerSensitiveAccessAudit.class))).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.recordRequired(10L, 20L,
            CustomerSensitiveAction.PII_EXPORT, CustomerSensitiveField.CONTACT_AND_IDENTITY,
            "CUSTOMER_EXPORT", "ALLOW", "trace-2", null, "sensitive value 13800000000"));
    }

    @Test void terminalExportFailureCanBeRecordedWithoutSensitiveValues() {
        when(mapper.insert(any(CustomerSensitiveAccessAudit.class))).thenReturn(1);

        service.recordRequired(10L, 0L, CustomerSensitiveAction.PII_EXPORT,
            CustomerSensitiveField.CONTACT_AND_IDENTITY, "CUSTOMER_EXPORT", "FAILED",
            "trace-failed", null, "FILE_GENERATION_FAILED_ROWS_2_FILTERS_0");

        ArgumentCaptor<CustomerSensitiveAccessAudit> captor = ArgumentCaptor.forClass(CustomerSensitiveAccessAudit.class);
        verify(mapper).insert(captor.capture());
        assertEquals("FAILED", captor.getValue().getDecision());
        assertEquals(0L, captor.getValue().getCustomerId());
        assertFalse(captor.getValue().toString().contains("13800000000"));
    }
}
