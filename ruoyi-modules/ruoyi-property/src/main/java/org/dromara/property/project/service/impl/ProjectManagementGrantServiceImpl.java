package org.dromara.property.project.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.property.project.domain.ProjectManagementGrant;
import org.dromara.property.project.domain.ProjectManagementGrantAudit;
import org.dromara.property.project.domain.bo.ProjectManagementGrantBo;
import org.dromara.property.project.domain.vo.ProjectManagementGrantVo;
import org.dromara.property.project.mapper.ProjectManagementGrantMapper;
import org.dromara.property.project.mapper.ProjectManagementGrantAuditMapper;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.project.service.IProjectManagementGrantService;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.dromara.system.event.OnlineUserCleanEvent;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 企业项目管理范围授权；B2 首期仅平台超级管理员可维护。
 */
@Service
@RequiredArgsConstructor
public class ProjectManagementGrantServiceImpl implements IProjectManagementGrantService {
    private final ProjectManagementGrantMapper grantMapper;
    private final ProjectMapper projectMapper;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ProjectManagementGrantAuditMapper auditMapper;

    @Override
    public PageResult<ProjectManagementGrantVo> queryPage(ProjectManagementGrantBo bo, PageQuery pageQuery) {
        assertSuperAdmin();
        Page<ProjectManagementGrantVo> page = grantMapper.selectVoPage(pageQuery.build(), QueryBuilder.lambda(ProjectManagementGrant.class)
            .eqIfText(ProjectManagementGrant::getPrincipalType, bo.getPrincipalType()).eqIfPresent(ProjectManagementGrant::getPrincipalId, bo.getPrincipalId())
            .eqIfText(ProjectManagementGrant::getScopeType, bo.getScopeType()).eqIfPresent(ProjectManagementGrant::getProjectId, bo.getProjectId()).build());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<ProjectManagementGrantVo> queryHistory(ProjectManagementGrantBo bo) {
        assertSuperAdmin();
        return grantMapper.selectVoList(QueryBuilder.lambda(ProjectManagementGrant.class)
            .eqIfText(ProjectManagementGrant::getPrincipalType, bo.getPrincipalType()).eqIfPresent(ProjectManagementGrant::getPrincipalId, bo.getPrincipalId()).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grant(ProjectManagementGrantBo bo) {
        assertSuperAdmin();
        normalizeAndValidate(bo);
        ProjectManagementGrant existing = grantMapper.selectActiveForUpdate(bo.getPrincipalType(), bo.getPrincipalId(), bo.getScopeType(), bo.getProjectId());
        if (existing != null) {
            if ("0".equals(existing.getStatus())) return true;
            String before = existing.getStatus();
            existing.setStatus("0");
            boolean result = grantMapper.updateById(existing) > 0;
            audit(existing, "ENABLE", before, "0", result);
            publishSubjectCleanup(existing, result);
            return result;
        }
        ProjectManagementGrant entity = new ProjectManagementGrant();
        entity.setPrincipalType(bo.getPrincipalType());
        entity.setPrincipalId(bo.getPrincipalId());
        entity.setScopeType(bo.getScopeType());
        entity.setProjectId(bo.getProjectId());
        entity.setStatus("0");
        try {
            boolean result = grantMapper.insert(entity) > 0;
            audit(entity, "GRANT", null, "0", result);
            publishSubjectCleanup(entity, result);
            return result;
        } catch (DuplicateKeyException e) {
            throw new ServiceException("相同有效管理授权已存在，请重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, Integer version, String status) {
        assertSuperAdmin();
        if (!"0".equals(status) && !"1".equals(status)) throw new ServiceException("授权状态无效");
        ProjectManagementGrant grant = grantMapper.selectById(id);
        if (grant == null) throw new ServiceException("授权不存在");
        boolean result = grantMapper.lambda().set(ProjectManagementGrant::getStatus, status).setSql("version = version + 1").eq(ProjectManagementGrant::getId, id).eq(ProjectManagementGrant::getVersion, version).updateCount() > 0;
        audit(grant, "0".equals(status) ? "ENABLE" : "DISABLE", grant.getStatus(), status, result);
        publishSubjectCleanup(grant, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revoke(Long id, Integer version) {
        assertSuperAdmin();
        ProjectManagementGrant grant = grantMapper.selectById(id);
        if (grant == null) throw new ServiceException("授权不存在");
        boolean result = grantMapper.lambda().set(ProjectManagementGrant::getDelFlag, "1").setSql("version = version + 1").eq(ProjectManagementGrant::getId, id).eq(ProjectManagementGrant::getVersion, version).updateCount() > 0;
        audit(grant, "REVOKE", grant.getStatus(), null, result);
        publishSubjectCleanup(grant, result);
        return result;
    }

    private void normalizeAndValidate(ProjectManagementGrantBo bo) {
        if (!"USER".equals(bo.getPrincipalType()) && !"ROLE".equals(bo.getPrincipalType()))
            throw new ServiceException("授权主体类型无效");
        if (!"ALL".equals(bo.getScopeType()) && !"PROJECT".equals(bo.getScopeType()))
            throw new ServiceException("授权范围类型无效");
        if ("ALL".equals(bo.getScopeType())) bo.setProjectId(0L);
        else if (bo.getProjectId() == null || bo.getProjectId() <= 0 || projectMapper.selectById(bo.getProjectId()) == null)
            throw new ServiceException("项目不存在");
        if ("USER".equals(bo.getPrincipalType()) && userService.selectUserById(bo.getPrincipalId()) == null)
            throw new ServiceException("系统用户不存在");
        if ("ROLE".equals(bo.getPrincipalType()) && roleService.selectRoleById(bo.getPrincipalId()) == null)
            throw new ServiceException("系统角色不存在");
    }

    private void assertSuperAdmin() {
        if (!LoginHelper.isSuperAdmin()) throw new ServiceException("仅超级管理员可维护项目管理授权");
    }

    private void publishSubjectCleanup(ProjectManagementGrant grant, boolean result) {
        if (!result) return;
        if ("ROLE".equals(grant.getPrincipalType()))
            SpringUtils.context().publishEvent(OnlineUserCleanEvent.byRole(grant.getPrincipalId()));
        else SpringUtils.context().publishEvent(OnlineUserCleanEvent.byUsers(List.of(grant.getPrincipalId())));
    }

    private void audit(ProjectManagementGrant grant, String action, String before, String after, boolean result) {
        ProjectManagementGrantAudit audit = new ProjectManagementGrantAudit();
        audit.setGrantId(grant.getId());
        audit.setOperatorId(LoginHelper.getUserId());
        audit.setAction(action);
        audit.setPrincipalType(grant.getPrincipalType());
        audit.setPrincipalId(grant.getPrincipalId());
        audit.setScopeType(grant.getScopeType());
        audit.setProjectId(grant.getProjectId());
        audit.setBeforeStatus(before);
        audit.setAfterStatus(after);
        audit.setResult(result ? "0" : "1");
        auditMapper.insert(audit);
    }
}
