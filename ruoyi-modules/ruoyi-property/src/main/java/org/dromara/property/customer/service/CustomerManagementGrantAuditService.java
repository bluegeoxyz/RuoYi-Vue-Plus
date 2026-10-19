package org.dromara.property.customer.service;

import org.dromara.property.customer.domain.CustomerManagementGrant;

/** Customer 授权变更持久化审计。 */
public interface CustomerManagementGrantAuditService {
    void record(CustomerManagementGrant grant, String action, String beforeStatus,
                String afterStatus, boolean success, String reasonCode);
}
