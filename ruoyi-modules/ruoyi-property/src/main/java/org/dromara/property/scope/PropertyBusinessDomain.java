package org.dromara.property.scope;

/** 物业业务域。未实现授权来源的业务域默认拒绝。 */
public enum PropertyBusinessDomain {
    PROJECT_BASE, RESOURCE, CUSTOMER_RELATION, FINANCE, WAREHOUSE, OPERATIONS
}
