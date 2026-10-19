package org.dromara.property.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.customer.domain.CustomerManagementGrant;
import org.dromara.property.customer.domain.CustomerManagementGrantAudit;
import org.dromara.property.customer.mapper.CustomerManagementGrantAuditMapper;
import org.dromara.property.customer.service.CustomerManagementGrantAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** 授权审计使用独立事务，授权失败或回滚后仍保留安全操作轨迹。 */
@Service
@RequiredArgsConstructor
public class CustomerManagementGrantAuditServiceImpl implements CustomerManagementGrantAuditService {
    private final CustomerManagementGrantAuditMapper auditMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(CustomerManagementGrant grant, String action, String beforeStatus,
                       String afterStatus, boolean success, String reasonCode) {
        CustomerManagementGrantAudit audit = new CustomerManagementGrantAudit();
        audit.setGrantId(grant.getId());
        audit.setOperatorId(LoginHelper.getUserId());
        audit.setAction(action);
        audit.setPrincipalType(grant.getPrincipalType());
        audit.setPrincipalId(grant.getPrincipalId());
        audit.setScopeType(grant.getScopeType());
        audit.setBeforeStatus(beforeStatus);
        audit.setAfterStatus(afterStatus);
        audit.setResult(success ? "0" : "1");
        audit.setTraceId(UUID.randomUUID().toString());
        audit.setRemark(reasonCode);
        if (auditMapper.insert(audit) <= 0) {
            throw new ServiceException("客户授权审计写入失败");
        }
    }
}
