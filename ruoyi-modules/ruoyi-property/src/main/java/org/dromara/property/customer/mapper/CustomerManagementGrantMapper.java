package org.dromara.property.customer.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.customer.domain.CustomerManagementGrant;
import org.dromara.property.customer.domain.vo.CustomerManagementGrantVo;

import java.util.Collection;
import java.util.List;

/** Customer 企业授权 Mapper。 */
public interface CustomerManagementGrantMapper extends BaseMapperPlus<CustomerManagementGrant, CustomerManagementGrantVo> {
    default List<CustomerManagementGrant> selectEffective(String type, Collection<Long> principalIds) {
        return principalIds == null || principalIds.isEmpty() ? List.of() : lambda()
            .eq(CustomerManagementGrant::getPrincipalType, type)
            .in(CustomerManagementGrant::getPrincipalId, principalIds)
            .eq(CustomerManagementGrant::getScopeType, "ENTERPRISE")
            .eq(CustomerManagementGrant::getStatus, "0")
            .list();
    }

    default CustomerManagementGrant selectActiveForUpdate(String type, Long principalId) {
        return lambda().eq(CustomerManagementGrant::getPrincipalType, type)
            .eq(CustomerManagementGrant::getPrincipalId, principalId)
            .eq(CustomerManagementGrant::getScopeType, "ENTERPRISE")
            .last("FOR UPDATE").one();
    }
}
