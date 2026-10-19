package org.dromara.property.customer.service;

/** Customer 敏感资料访问的强制持久化审计服务。 */
public interface CustomerSensitiveAccessAuditService {
    void recordRequired(Long operatorId, Long customerId, CustomerSensitiveAction action,
                        CustomerSensitiveField field, String purpose, String decision,
                        String traceId, Long projectId, String reasonCode);
}
