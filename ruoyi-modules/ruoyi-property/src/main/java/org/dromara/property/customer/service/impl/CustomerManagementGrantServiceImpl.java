package org.dromara.property.customer.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.customer.domain.CustomerManagementGrant;
import org.dromara.property.customer.domain.bo.CustomerManagementGrantBo;
import org.dromara.property.customer.domain.vo.CustomerManagementGrantVo;
import org.dromara.property.customer.mapper.CustomerManagementGrantMapper;
import org.dromara.property.customer.service.CustomerManagementGrantAuditService;
import org.dromara.property.customer.service.ICustomerManagementGrantService;
import org.dromara.system.event.OnlineUserCleanEvent;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 企业 Customer 主档授权，仅 SuperAdmin 可维护。 */
@Service
@RequiredArgsConstructor
public class CustomerManagementGrantServiceImpl implements ICustomerManagementGrantService {
    private final CustomerManagementGrantMapper grantMapper;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final CustomerManagementGrantAuditService auditService;

    @Override
    public PageResult<CustomerManagementGrantVo> queryPage(CustomerManagementGrantBo bo, PageQuery pageQuery) {
        assertSuperAdmin();
        Page<CustomerManagementGrantVo> page = grantMapper.selectVoPage(pageQuery.build(),
            QueryBuilder.lambda(CustomerManagementGrant.class)
                .eqIfText(CustomerManagementGrant::getPrincipalType, bo.getPrincipalType())
                .eqIfPresent(CustomerManagementGrant::getPrincipalId, bo.getPrincipalId())
                .eq(CustomerManagementGrant::getScopeType, "ENTERPRISE").build());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<CustomerManagementGrantVo> queryHistory(CustomerManagementGrantBo bo) {
        assertSuperAdmin();
        return grantMapper.selectVoList(QueryBuilder.lambda(CustomerManagementGrant.class)
            .eqIfText(CustomerManagementGrant::getPrincipalType, bo.getPrincipalType())
            .eqIfPresent(CustomerManagementGrant::getPrincipalId, bo.getPrincipalId())
            .eq(CustomerManagementGrant::getScopeType, "ENTERPRISE").build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grant(CustomerManagementGrantBo bo) {
        assertSuperAdmin();
        normalizeAndValidate(bo);
        CustomerManagementGrant existing = grantMapper.selectActiveForUpdate(bo.getPrincipalType(), bo.getPrincipalId());
        if (existing != null) {
            if ("0".equals(existing.getStatus())) {
                return true;
            }
            String before = existing.getStatus();
            existing.setStatus("0");
            boolean result = grantMapper.updateById(existing) > 0;
            publishSubjectCleanup(existing, result);
            auditService.record(existing, "ENABLE", before, "0", result, result ? "SUCCESS" : "UPDATE_CONFLICT");
            return result;
        }
        CustomerManagementGrant entity = new CustomerManagementGrant();
        entity.setPrincipalType(bo.getPrincipalType());
        entity.setPrincipalId(bo.getPrincipalId());
        entity.setScopeType("ENTERPRISE");
        entity.setStatus("0");
        try {
            boolean result = grantMapper.insert(entity) > 0;
            publishSubjectCleanup(entity, result);
            auditService.record(entity, "GRANT", null, "0", result, result ? "SUCCESS" : "INSERT_FAILED");
            return result;
        } catch (DuplicateKeyException e) {
            auditService.record(entity, "GRANT", null, null, false, "DUPLICATE_ACTIVE_GRANT");
            throw new ServiceException("相同有效客户主档授权已存在，请重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, Integer version, String status) {
        assertSuperAdmin();
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("授权状态无效");
        }
        CustomerManagementGrant grant = requireGrant(id);
        boolean result = version != null && grantMapper.lambda().set(CustomerManagementGrant::getStatus, status)
            .setSql("version = version + 1").eq(CustomerManagementGrant::getId, id)
            .eq(CustomerManagementGrant::getVersion, version).updateCount() > 0;
        publishSubjectCleanup(grant, result);
        auditService.record(grant, "0".equals(status) ? "ENABLE" : "DISABLE",
            grant.getStatus(), status, result, result ? "SUCCESS" : "VERSION_CONFLICT");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revoke(Long id, Integer version) {
        assertSuperAdmin();
        CustomerManagementGrant grant = requireGrant(id);
        boolean result = version != null && grantMapper.lambda().set(CustomerManagementGrant::getDelFlag, "1")
            .setSql("version = version + 1").eq(CustomerManagementGrant::getId, id)
            .eq(CustomerManagementGrant::getVersion, version).updateCount() > 0;
        publishSubjectCleanup(grant, result);
        auditService.record(grant, "REVOKE", grant.getStatus(), null, result,
            result ? "SUCCESS" : "VERSION_CONFLICT");
        return result;
    }

    private CustomerManagementGrant requireGrant(Long id) {
        CustomerManagementGrant grant = grantMapper.selectById(id);
        if (grant == null) {
            throw new ServiceException("客户主档授权不存在");
        }
        return grant;
    }

    private void normalizeAndValidate(CustomerManagementGrantBo bo) {
        bo.setScopeType("ENTERPRISE");
        if (bo.getPrincipalId() == null || bo.getPrincipalId() <= 0
            || (!"USER".equals(bo.getPrincipalType()) && !"ROLE".equals(bo.getPrincipalType()))) {
            throw new ServiceException("授权主体无效");
        }
        if ("USER".equals(bo.getPrincipalType()) && userService.selectUserById(bo.getPrincipalId()) == null) {
            throw new ServiceException("系统用户不存在");
        }
        if ("ROLE".equals(bo.getPrincipalType()) && roleService.selectRoleById(bo.getPrincipalId()) == null) {
            throw new ServiceException("系统角色不存在");
        }
    }

    private void assertSuperAdmin() {
        if (!LoginHelper.isSuperAdmin()) {
            throw new ServiceException("仅超级管理员可维护客户主档授权");
        }
    }

    private void publishSubjectCleanup(CustomerManagementGrant grant, boolean result) {
        if (!result) return;
        if ("ROLE".equals(grant.getPrincipalType())) {
            SpringUtils.context().publishEvent(OnlineUserCleanEvent.byRole(grant.getPrincipalId()));
        } else {
            SpringUtils.context().publishEvent(OnlineUserCleanEvent.byUsers(List.of(grant.getPrincipalId())));
        }
    }
}
