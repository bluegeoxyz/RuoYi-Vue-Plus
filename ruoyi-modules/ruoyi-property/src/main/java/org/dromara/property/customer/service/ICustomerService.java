package org.dromara.property.customer.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.customer.domain.bo.CustomerQueryBo;
import org.dromara.property.customer.domain.bo.CustomerCreateBo;
import org.dromara.property.customer.domain.bo.CustomerLifecycleBo;
import org.dromara.property.customer.domain.bo.CustomerUpdateBo;
import org.dromara.property.customer.domain.vo.CustomerCreatedVo;
import org.dromara.property.customer.domain.vo.CustomerDetailVo;
import org.dromara.property.customer.domain.vo.CustomerEditVo;
import org.dromara.property.customer.domain.vo.CustomerListVo;
import org.dromara.property.customer.domain.vo.CustomerMaskedExportVo;
import org.dromara.property.customer.domain.vo.CustomerSensitiveExportVo;
import org.dromara.property.customer.domain.vo.CustomerSensitiveDetailVo;

import java.util.List;

/** Customer 企业主档只读查询服务。 */
public interface ICustomerService {
    PageResult<CustomerListVo> queryPage(CustomerQueryBo bo, PageQuery pageQuery);
    CustomerDetailVo queryById(Long id);
    CustomerEditVo queryForEdit(Long id);
    CustomerSensitiveDetailVo querySensitiveById(Long id, String traceId);
    List<CustomerMaskedExportVo> queryMaskedExport(CustomerQueryBo bo);
    List<CustomerSensitiveExportVo> querySensitiveExport(CustomerQueryBo bo, String traceId);
    void auditSensitiveExportFailure(int recordCount, String traceId);
    CustomerCreatedVo create(CustomerCreateBo bo);
    boolean update(CustomerUpdateBo bo);
    boolean changeLifecycle(CustomerLifecycleBo bo);
    boolean delete(Long id, Integer version);
}
