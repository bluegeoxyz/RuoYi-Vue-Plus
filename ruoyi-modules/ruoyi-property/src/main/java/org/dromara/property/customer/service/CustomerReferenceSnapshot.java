package org.dromara.property.customer.service;

/** 建立项目住户关系所需的最小 Customer 引用快照。 */
public record CustomerReferenceSnapshot(Long customerId, String customerType, String lifecycleStatus) {
}
