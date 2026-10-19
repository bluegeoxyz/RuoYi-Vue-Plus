package org.dromara.property.customer.service;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.domain.bo.CustomerCreateBo;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.security.CustomerFieldEncryptionService;
import org.dromara.property.customer.security.CustomerIdentityFingerprintService;
import org.dromara.property.customer.security.CustomerSecurityProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/** 单条新增与批量导入共用的 Customer 安全创建能力。 */
@Service
@RequiredArgsConstructor
public class CustomerCreateExecutor {
    private final CustomerMapper customerMapper;
    private final CustomerSecurityProperties securityProperties;
    private final ObjectProvider<CustomerIdentityFingerprintService> fingerprintServiceProvider;
    private final CustomerFieldEncryptionService fieldEncryptionService;

    public Customer prepare(CustomerCreateBo bo, boolean checkExistingIdentity) {
        CustomerIdentityFingerprintService fingerprintService = requireSecurity();
        String idType = trimToNull(bo.getIdType());
        String idNo = trimToNull(bo.getIdNo());
        if ((idType == null) != (idNo == null)) throw new ServiceException("证件类型和证件号码必须同时填写或同时为空");
        Customer customer = new Customer();
        long id = IdUtil.getSnowflakeNextId();
        customer.setId(id); customer.setCustomerNo("C" + id);
        customer.setCustomerType(bo.getCustomerType().trim().toUpperCase());
        customer.setCustomerName(bo.getCustomerName().trim());
        customer.setGender(trimToNull(bo.getGender())); customer.setMobile(trimToNull(bo.getMobile()));
        customer.setPhone(trimToNull(bo.getPhone())); customer.setEmail(trimToNull(bo.getEmail()));
        customer.setBirthDate(bo.getBirthDate()); customer.setRemark(trimToNull(bo.getRemark()));
        customer.setStatus("0"); customer.setLifecycleStatus("ACTIVE"); customer.setVersion(0); customer.setDelFlag("0");
        if (idType != null) {
            String normalizedType = fingerprintService.normalizeIdType(idType);
            String normalizedNo = fingerprintService.normalizeIdNo(idNo);
            customer.setIdType(normalizedType);
            customer.setIdNo(fieldEncryptionService.encryptIdNo(normalizedNo));
            customer.setIdentityFingerprint(fingerprintService.fingerprint(normalizedType, normalizedNo));
            customer.setPiiKeyVersion(securityProperties.getKeyVersion());
            if (checkExistingIdentity && customerMapper.countActiveIdentity(normalizedType, customer.getIdentityFingerprint()) > 0) {
                throw duplicateIdentity();
            }
        }
        return customer;
    }

    public Customer insert(Customer customer) {
        try { customerMapper.insert(customer); }
        catch (DuplicateKeyException e) { throw classifyDuplicate(e); }
        return customer;
    }

    private CustomerIdentityFingerprintService requireSecurity() {
        CustomerIdentityFingerprintService service = fingerprintServiceProvider.getIfAvailable();
        if (!securityProperties.isEnabled() || service == null || StringUtils.isBlank(securityProperties.getKeyVersion())) {
            throw new ServiceException("Customer 安全配置未启用或不完整，禁止写入客户档案");
        }
        return service;
    }

    private ServiceException duplicateIdentity() { return new ServiceException("相同证件身份的有效客户档案已存在"); }
    private ServiceException classifyDuplicate(DuplicateKeyException exception) {
        String message = duplicateMessage(exception).toLowerCase();
        if (message.contains("uk_pm_customer_active_identity")) return duplicateIdentity();
        if (message.contains("uk_pm_customer_no")) return new ServiceException("客户编号冲突，请重新提交");
        return new ServiceException("客户档案唯一性校验失败，请重新提交");
    }
    private String duplicateMessage(Throwable throwable) { StringBuilder value=new StringBuilder(); for(Throwable current=throwable;current!=null;current=current.getCause()) if(current.getMessage()!=null)value.append(' ').append(current.getMessage()); return value.toString(); }
    private String trimToNull(String value) { return StringUtils.isBlank(value) ? null : value.trim(); }
}
