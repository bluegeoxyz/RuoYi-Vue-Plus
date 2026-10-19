package org.dromara.property.customer.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.customer.domain.bo.CustomerManagementGrantBo;
import org.dromara.property.customer.domain.vo.CustomerManagementGrantVo;

import java.util.List;

/** Customer 企业主档授权管理。 */
public interface ICustomerManagementGrantService {
    PageResult<CustomerManagementGrantVo> queryPage(CustomerManagementGrantBo bo, PageQuery pageQuery);
    List<CustomerManagementGrantVo> queryHistory(CustomerManagementGrantBo bo);
    boolean grant(CustomerManagementGrantBo bo);
    boolean changeStatus(Long id, Integer version, String status);
    boolean revoke(Long id, Integer version);
}
