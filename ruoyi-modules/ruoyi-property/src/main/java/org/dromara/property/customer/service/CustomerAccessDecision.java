package org.dromara.property.customer.service;

import org.dromara.property.scope.PropertyProjectScope;

import java.util.Set;

/** 当前请求的企业 Customer、项目关系及敏感能力决策。 */
public record CustomerAccessDecision(
    boolean enterpriseWide,
    PropertyProjectScope relationProjectScope,
    Set<CustomerSensitiveField> visibleSensitiveFields,
    boolean sensitiveExportAllowed) {
}
