package org.dromara.property.customer.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.customer.domain.Customer;
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
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.service.CustomerAccessPolicy;
import org.dromara.property.customer.service.CustomerAction;
import org.dromara.property.customer.service.CustomerCreateExecutor;
import org.dromara.property.customer.service.CustomerSensitiveAction;
import org.dromara.property.customer.service.CustomerSensitiveField;
import org.dromara.property.customer.service.ICustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Customer 企业主档安全查询实现。 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {
    private static final String SENSITIVE_PURPOSE = "CUSTOMER_DETAIL";
    private static final int MAX_EXPORT_ROWS = 5_000;

    private final CustomerMapper customerMapper;
    private final CustomerAccessPolicy accessPolicy;
    private final CustomerCreateExecutor createExecutor;

    @Override
    public PageResult<CustomerListVo> queryPage(CustomerQueryBo bo, PageQuery pageQuery) {
        accessPolicy.assertEnterpriseMasterAccess(CustomerAction.LIST);
        Page<Customer> page = customerMapper.selectPage(pageQuery.build(), buildQuery(bo));
        List<CustomerListVo> records = page.getRecords().stream().map(this::toListVo).toList();
        return PageResult.build(records, page.getTotal());
    }

    @Override
    public CustomerDetailVo queryById(Long id) {
        accessPolicy.assertCustomerVisible(id, CustomerAction.DETAIL);
        return toDetailVo(requireCustomer(id));
    }

    @Override
    public CustomerEditVo queryForEdit(Long id) {
        accessPolicy.assertCustomerVisible(id, CustomerAction.UPDATE);
        Customer customer = requireCustomer(id);
        CustomerEditVo vo = new CustomerEditVo();
        vo.setId(customer.getId());
        vo.setCustomerNo(customer.getCustomerNo());
        vo.setCustomerType(customer.getCustomerType());
        vo.setCustomerName(customer.getCustomerName());
        vo.setGender(customer.getGender());
        vo.setBirthDate(customer.getBirthDate());
        vo.setRemark(customer.getRemark());
        vo.setVersion(customer.getVersion());
        return vo;
    }

    @Override
    public CustomerSensitiveDetailVo querySensitiveById(Long id, String traceId) {
        accessPolicy.assertCustomerVisible(id, CustomerAction.DETAIL);
        Customer customer = requireCustomer(id);
        // Entity 此时已在服务端受控内存中解密；必须先可靠审计，之后才允许映射到出站 VO。
        accessPolicy.assertSensitiveAccess(id, CustomerSensitiveAction.PII_VIEW,
            CustomerSensitiveField.CONTACT_AND_IDENTITY, SENSITIVE_PURPOSE, traceId, null);
        return toSensitiveVo(customer);
    }

    @Override
    public List<CustomerMaskedExportVo> queryMaskedExport(CustomerQueryBo bo) {
        accessPolicy.assertEnterpriseMasterAccess(CustomerAction.EXPORT);
        LambdaQueryWrapper<Customer> query = buildQuery(bo);
        assertExportLimit(query);
        return customerMapper.selectList(query).stream().map(this::toMaskedExportVo).toList();
    }

    @Override
    public List<CustomerSensitiveExportVo> querySensitiveExport(CustomerQueryBo bo, String traceId) {
        accessPolicy.assertEnterpriseMasterAccess(CustomerAction.EXPORT);
        LambdaQueryWrapper<Customer> query = buildQuery(bo);
        int recordCount = assertExportLimit(query);
        // 仅 COUNT 查询先执行。权限和审计通过后才允许读取、解密并映射完整 PII。
        accessPolicy.assertSensitiveExportAccess(recordCount, countFilters(bo), traceId);
        try {
            return customerMapper.selectList(query).stream().map(this::toSensitiveExportVo).toList();
        } catch (RuntimeException e) {
            accessPolicy.recordSensitiveExportFailure(recordCount, traceId);
            throw e;
        }
    }

    @Override
    public void auditSensitiveExportFailure(int recordCount, String traceId) {
        accessPolicy.recordSensitiveExportFailure(recordCount, traceId);
    }

    private int assertExportLimit(LambdaQueryWrapper<Customer> query) {
        long count = customerMapper.selectCount(query);
        if (count > MAX_EXPORT_ROWS) {
            throw new ServiceException("导出记录超过5000条，请缩小查询范围后重试");
        }
        return (int) count;
    }

    private int countFilters(CustomerQueryBo bo) {
        int count = 0;
        if (StringUtils.isNotBlank(bo.getCustomerNo())) count++;
        if (StringUtils.isNotBlank(bo.getCustomerType())) count++;
        if (StringUtils.isNotBlank(bo.getCustomerName())) count++;
        if (StringUtils.isNotBlank(bo.getMobile())) count++;
        if (StringUtils.isNotBlank(bo.getStatus())) count++;
        if (StringUtils.isNotBlank(bo.getLifecycleStatus())) count++;
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerCreatedVo create(CustomerCreateBo bo) {
        accessPolicy.assertEnterpriseMasterAccess(CustomerAction.CREATE);
        Customer customer = createExecutor.insert(createExecutor.prepare(bo, true));
        return toCreatedVo(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(CustomerUpdateBo bo) {
        accessPolicy.assertCustomerVisible(bo.getId(), CustomerAction.UPDATE);
        Customer current = requireCustomer(bo.getId());
        assertVersion(current, bo.getVersion());
        Customer update = new Customer();
        update.setId(current.getId());
        update.setVersion(bo.getVersion());
        update.setCustomerName(bo.getCustomerName().trim());
        update.setGender(trimToNull(bo.getGender()));
        update.setMobile(trimToNull(bo.getMobile()));
        update.setPhone(trimToNull(bo.getPhone()));
        update.setEmail(trimToNull(bo.getEmail()));
        update.setBirthDate(bo.getBirthDate());
        update.setRemark(trimToNull(bo.getRemark()));
        if (customerMapper.updateById(update) != 1) {
            throw optimisticConflict();
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeLifecycle(CustomerLifecycleBo bo) {
        accessPolicy.assertCustomerVisible(bo.getId(), CustomerAction.STATUS);
        Customer current = requireLockedCustomer(bo.getId());
        assertVersion(current, bo.getVersion());
        if ("MERGED".equals(current.getLifecycleStatus())) {
            throw new ServiceException("已合并客户不能变更生命周期状态");
        }
        if ("CANCELLED".equals(current.getLifecycleStatus())
            && !"CANCELLED".equals(bo.getLifecycleStatus())) {
            throw new ServiceException("已注销客户不能重新启用或停用");
        }
        if (!"ACTIVE".equals(bo.getLifecycleStatus())
            && !customerMapper.selectEffectiveRelationIdsForUpdate(current.getId()).isEmpty()) {
            throw new ServiceException("客户存在待生效或有效房屋关系，不能停用或注销");
        }
        Customer update = new Customer();
        update.setId(current.getId());
        update.setVersion(bo.getVersion());
        update.setLifecycleStatus(bo.getLifecycleStatus());
        update.setStatus("ACTIVE".equals(bo.getLifecycleStatus()) ? "0" : "1");
        if (customerMapper.updateById(update) != 1) {
            throw optimisticConflict();
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id, Integer version) {
        accessPolicy.assertCustomerVisible(id, CustomerAction.DELETE);
        Customer current = requireLockedCustomer(id);
        assertVersion(current, version);
        // 包含已终止、停用以及逻辑删除的历史关系；任何历史都禁止删除主档。
        if (!customerMapper.selectAllRelationIdsForUpdate(id).isEmpty()) {
            throw new ServiceException("客户存在房屋关系历史，只能停用或注销，不能删除");
        }
        if (customerMapper.logicalDelete(id, version) != 1) {
            throw optimisticConflict();
        }
        return true;
    }

    private LambdaQueryWrapper<Customer> buildQuery(CustomerQueryBo bo) {
        return QueryBuilder.lambda(Customer.class)
            .likeIfText(Customer::getCustomerNo, bo.getCustomerNo())
            .eqIfText(Customer::getCustomerType, bo.getCustomerType())
            .likeIfText(Customer::getCustomerName, bo.getCustomerName())
            .eqIfText(Customer::getMobile, bo.getMobile())
            .eqIfText(Customer::getStatus, bo.getStatus())
            .eqIfText(Customer::getLifecycleStatus, bo.getLifecycleStatus())
            .orderByDesc(Customer::getCreateTime)
            .orderByDesc(Customer::getId)
            .build();
    }

    private Customer requireCustomer(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new ServiceException("客户档案不存在");
        }
        return customer;
    }

    private Customer requireLockedCustomer(Long id) {
        Customer customer = customerMapper.selectActiveByIdForUpdate(id);
        if (customer == null) {
            throw new ServiceException("客户档案不存在");
        }
        return customer;
    }

    private void assertVersion(Customer customer, Integer version) {
        if (version == null || !version.equals(customer.getVersion())) {
            throw optimisticConflict();
        }
    }

    private ServiceException optimisticConflict() {
        return new ServiceException("客户档案已被其他请求修改，请刷新后重试");
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private CustomerCreatedVo toCreatedVo(Customer customer) {
        CustomerCreatedVo vo = new CustomerCreatedVo();
        vo.setId(customer.getId());
        vo.setCustomerNo(customer.getCustomerNo());
        vo.setCustomerName(customer.getCustomerName());
        vo.setStatus(customer.getStatus());
        vo.setLifecycleStatus(customer.getLifecycleStatus());
        vo.setVersion(customer.getVersion());
        return vo;
    }

    private CustomerListVo toListVo(Customer customer) {
        CustomerListVo vo = new CustomerListVo();
        vo.setId(customer.getId());
        vo.setCustomerNo(customer.getCustomerNo());
        vo.setCustomerType(customer.getCustomerType());
        vo.setCustomerName(maskName(customer));
        vo.setMobile(maskMobile(customer.getMobile()));
        vo.setStatus(customer.getStatus());
        vo.setLifecycleStatus(customer.getLifecycleStatus());
        vo.setVersion(customer.getVersion());
        return vo;
    }

    private CustomerDetailVo toDetailVo(Customer customer) {
        CustomerDetailVo vo = new CustomerDetailVo();
        vo.setId(customer.getId());
        vo.setCustomerNo(customer.getCustomerNo());
        vo.setCustomerType(customer.getCustomerType());
        vo.setCustomerName(maskName(customer));
        vo.setGender(customer.getGender());
        vo.setMobile(maskMobile(customer.getMobile()));
        vo.setPhone(maskFixedPhone(customer.getPhone()));
        vo.setEmail(maskEmail(customer.getEmail()));
        vo.setIdType(customer.getIdType());
        vo.setIdNo(maskIdentity(customer.getIdNo()));
        vo.setStatus(customer.getStatus());
        vo.setLifecycleStatus(customer.getLifecycleStatus());
        return vo;
    }

    private CustomerSensitiveDetailVo toSensitiveVo(Customer customer) {
        CustomerSensitiveDetailVo vo = new CustomerSensitiveDetailVo();
        vo.setId(customer.getId());
        vo.setCustomerNo(customer.getCustomerNo());
        vo.setCustomerType(customer.getCustomerType());
        vo.setCustomerName(customer.getCustomerName());
        vo.setGender(customer.getGender());
        vo.setMobile(customer.getMobile());
        vo.setPhone(customer.getPhone());
        vo.setEmail(customer.getEmail());
        vo.setIdType(customer.getIdType());
        vo.setIdNo(customer.getIdNo());
        vo.setBirthDate(customer.getBirthDate());
        vo.setStatus(customer.getStatus());
        vo.setLifecycleStatus(customer.getLifecycleStatus());
        return vo;
    }

    private CustomerMaskedExportVo toMaskedExportVo(Customer customer) {
        CustomerMaskedExportVo vo = new CustomerMaskedExportVo();
        vo.setCustomerNo(customer.getCustomerNo());
        vo.setCustomerType(customer.getCustomerType());
        vo.setCustomerName(maskName(customer));
        vo.setGender(customer.getGender());
        vo.setMobile(maskMobile(customer.getMobile()));
        vo.setPhone(maskFixedPhone(customer.getPhone()));
        vo.setEmail(maskEmail(customer.getEmail()));
        vo.setIdType(customer.getIdType());
        vo.setIdNo(maskIdentity(customer.getIdNo()));
        vo.setStatus(customer.getStatus());
        vo.setLifecycleStatus(customer.getLifecycleStatus());
        return vo;
    }

    private CustomerSensitiveExportVo toSensitiveExportVo(Customer customer) {
        CustomerSensitiveExportVo vo = new CustomerSensitiveExportVo();
        vo.setCustomerNo(customer.getCustomerNo());
        vo.setCustomerType(customer.getCustomerType());
        vo.setCustomerName(customer.getCustomerName());
        vo.setGender(customer.getGender());
        vo.setMobile(customer.getMobile());
        vo.setPhone(customer.getPhone());
        vo.setEmail(customer.getEmail());
        vo.setIdType(customer.getIdType());
        vo.setIdNo(customer.getIdNo());
        vo.setBirthDate(customer.getBirthDate());
        vo.setStatus(customer.getStatus());
        vo.setLifecycleStatus(customer.getLifecycleStatus());
        return vo;
    }

    private String maskName(Customer customer) {
        if (StringUtils.isBlank(customer.getCustomerName()) || "COMPANY".equals(customer.getCustomerType())) {
            return customer.getCustomerName();
        }
        return DesensitizedUtil.chineseName(customer.getCustomerName());
    }

    private String maskMobile(String value) {
        return StringUtils.isBlank(value) ? value : DesensitizedUtil.mobilePhone(value);
    }

    private String maskFixedPhone(String value) {
        return StringUtils.isBlank(value) ? value : DesensitizedUtil.fixedPhone(value);
    }

    private String maskEmail(String value) {
        return StringUtils.isBlank(value) ? value : DesensitizedUtil.email(value);
    }

    private String maskIdentity(String value) {
        return StringUtils.isBlank(value) ? value : DesensitizedUtil.idCardNum(value, 3, 4);
    }
}
