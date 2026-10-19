package org.dromara.property.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.property.customer.domain.CustomerSensitiveAccessAudit;
import org.dromara.property.customer.mapper.CustomerSensitiveAccessAuditMapper;
import org.dromara.property.customer.service.CustomerSensitiveAccessAuditService;
import org.dromara.property.customer.service.CustomerSensitiveAction;
import org.dromara.property.customer.service.CustomerSensitiveField;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

/** 敏感资料审计写入失败即拒绝继续返回明文。 */
@Service
@RequiredArgsConstructor
public class CustomerSensitiveAccessAuditServiceImpl implements CustomerSensitiveAccessAuditService {
    private static final Pattern SAFE_CODE = Pattern.compile("[A-Za-z0-9_:/.-]{1,64}");
    private final CustomerSensitiveAccessAuditMapper auditMapper;

    @Override
    public void recordRequired(Long operatorId, Long customerId, CustomerSensitiveAction action,
                               CustomerSensitiveField field, String purpose, String decision,
                               String traceId, Long projectId, String reasonCode) {
        if (operatorId == null || customerId == null || action == null || field == null
            || !SAFE_CODE.matcher(purpose == null ? "" : purpose).matches()
            || (!"ALLOW".equals(decision) && !"DENY".equals(decision) && !"FAILED".equals(decision))) {
            throw new ServiceException("敏感访问审计参数无效，已拒绝返回敏感资料");
        }
        CustomerSensitiveAccessAudit audit = new CustomerSensitiveAccessAudit();
        audit.setOperatorId(operatorId);
        audit.setCustomerId(customerId);
        audit.setAccessAction(action.name());
        audit.setAccessPurpose(purpose);
        audit.setFieldLevel(field.name());
        audit.setDecision(decision);
        audit.setTraceId(StringUtils.isBlank(traceId) ? UUID.randomUUID().toString() : safeTraceId(traceId));
        audit.setProjectId(projectId);
        audit.setRemark(safeReason(reasonCode));
        try {
            if (auditMapper.insert(audit) <= 0) {
                throw new ServiceException("敏感访问审计写入失败，已拒绝返回敏感资料");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("敏感访问审计写入失败，已拒绝返回敏感资料");
        }
    }

    private String safeTraceId(String traceId) {
        return SAFE_CODE.matcher(traceId).matches() ? traceId : UUID.randomUUID().toString();
    }

    private String safeReason(String reasonCode) {
        return StringUtils.isBlank(reasonCode) ? null
            : SAFE_CODE.matcher(reasonCode).matches() ? reasonCode : "REDACTED";
    }
}
