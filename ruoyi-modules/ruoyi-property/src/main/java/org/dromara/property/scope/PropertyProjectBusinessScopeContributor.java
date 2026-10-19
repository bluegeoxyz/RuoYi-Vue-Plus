package org.dromara.property.scope;

/** 可插拔的项目业务范围授权来源。 */
public interface PropertyProjectBusinessScopeContributor {
    boolean supports(PropertyBusinessDomain domain, ProjectBusinessAction action);
    PropertyProjectScope resolve(Long userId, PropertyBusinessDomain domain, ProjectBusinessAction action);
}
