package org.dromara.property.customer.service;

/** Customer 主档向房屋关系域提供的受控引用边界。 */
public interface CustomerReferenceService {
    CustomerReferenceSnapshot requireActiveForRelation(Long customerId);
    CustomerReferenceSnapshot lockAndRequireActiveForRelation(Long customerId);
    CustomerReferenceSnapshot lockForExistingRelation(Long customerId);
    boolean hasAnyRoomRelation(Long customerId);
}
