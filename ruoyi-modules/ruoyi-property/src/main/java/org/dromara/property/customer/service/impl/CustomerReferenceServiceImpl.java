package org.dromara.property.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.service.CustomerReferenceService;
import org.dromara.property.customer.service.CustomerReferenceSnapshot;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerReferenceServiceImpl implements CustomerReferenceService {
    private final CustomerMapper customerMapper;

    @Override
    public CustomerReferenceSnapshot requireActiveForRelation(Long customerId) {
        return snapshot(customerMapper.selectById(customerId));
    }

    @Override
    public CustomerReferenceSnapshot lockAndRequireActiveForRelation(Long customerId) {
        return snapshot(customerMapper.selectActiveByIdForUpdate(customerId));
    }

    @Override
    public CustomerReferenceSnapshot lockForExistingRelation(Long customerId) {
        Customer customer = customerMapper.selectActiveByIdForUpdate(customerId);
        if (customer == null) {
            throw new ServiceException("客户不存在或已删除");
        }
        return new CustomerReferenceSnapshot(customer.getId(), customer.getCustomerType(), customer.getLifecycleStatus());
    }

    @Override
    public boolean hasAnyRoomRelation(Long customerId) {
        return customerMapper.existsAnyRoomRelation(customerId);
    }

    private CustomerReferenceSnapshot snapshot(Customer customer) {
        if (customer == null || !"ACTIVE".equals(customer.getLifecycleStatus()) || !"0".equals(customer.getStatus())) {
            throw new ServiceException("客户不存在、已停用或不可建立房屋关系");
        }
        return new CustomerReferenceSnapshot(customer.getId(), customer.getCustomerType(), customer.getLifecycleStatus());
    }
}
