package org.dromara.property.customer.service;

import org.dromara.property.scope.ProjectBusinessAction;

/** Customer 企业主档与项目关系权限的统一服务边界。 */
public interface CustomerAccessPolicy {
    CustomerAccessDecision resolve(CustomerAction action);
    void assertEnterpriseMasterAccess(CustomerAction action);
    void assertCustomerVisible(Long customerId, CustomerAction action);
    void assertProjectRelationAccess(Long projectId, ProjectBusinessAction action);
    boolean canViewSensitive(CustomerSensitiveField field);
    boolean canExportSensitive();

    /** 校验敏感能力并强制记录允许或拒绝审计。 */
    void assertSensitiveAccess(Long customerId, CustomerSensitiveAction action,
                               CustomerSensitiveField field, String purpose,
                               String traceId, Long projectId);

    /** 校验敏感导出权限并记录不含查询值的批次审计。 */
    void assertSensitiveExportAccess(int recordCount, int filterCount, String traceId);

    /** 记录文件生成阶段的终止失败，不包含任何敏感字段值。 */
    void recordSensitiveExportFailure(int recordCount, String traceId);
}
