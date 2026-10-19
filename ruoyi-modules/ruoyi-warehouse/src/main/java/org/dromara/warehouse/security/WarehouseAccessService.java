package org.dromara.warehouse.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.api.ProjectAccessScope;
import org.dromara.property.api.ProjectReferenceDTO;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.system.domain.bo.SysDeptBo;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Warehouse 公司与项目范围的统一校验边界。 */
@Service
@RequiredArgsConstructor
public class WarehouseAccessService {

    private final WarehouseMapper warehouseMapper;
    private final ISysDeptService deptService;
    private final ProjectReferenceService projectReferenceService;

    public Warehouse requireWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) throw new ServiceException("仓库不存在");
        assertAccess(warehouse);
        return warehouse;
    }

    public Warehouse requireEnabledWarehouse(Long warehouseId) {
        Warehouse warehouse = requireWarehouse(warehouseId);
        if (!SystemConstants.NORMAL.equals(warehouse.getStatus())) {
            throw new ServiceException("仓库已停用，不允许新增下级资料");
        }
        return warehouse;
    }

    public void assertAccess(Warehouse warehouse) {
        if (LoginHelper.isSuperAdmin()) return;
        if ("COMPANY".equals(warehouse.getWarehouseScope())) {
            deptService.checkDeptDataScope(warehouse.getCompanyId());
            return;
        }
        ProjectAccessScope scope = projectReferenceService.resolveWarehouseScope();
        if (!scope.contains(warehouse.getProjectId())) throw new ServiceException("无项目仓库访问权限");
    }

    public void validateCompany(Long companyId) {
        deptService.checkDeptDataScope(companyId);
        SysDeptVo company = deptService.selectDeptById(companyId);
        if (company == null) throw new ServiceException("所属公司/组织不存在");
        if (!SystemConstants.NORMAL.equals(company.getStatus())) throw new ServiceException("所属公司/组织已停用");
    }

    public ProjectReferenceDTO validateProject(Long projectId) {
        ProjectAccessScope scope = projectReferenceService.resolveWarehouseScope();
        if (!scope.contains(projectId)) throw new ServiceException("无项目仓库访问权限");
        ProjectReferenceDTO project = projectReferenceService.queryById(projectId);
        if (project == null) throw new ServiceException("所属项目不存在");
        if (!SystemConstants.NORMAL.equals(project.status())) throw new ServiceException("所属项目已停用");
        return project;
    }

    public void applyScope(LambdaQueryWrapper<Warehouse> wrapper) {
        if (LoginHelper.isSuperAdmin()) return;
        Set<Long> companyIds = accessibleCompanyIds();
        ProjectAccessScope projectScope = projectReferenceService.resolveWarehouseScope();
        boolean hasCompany = !companyIds.isEmpty();
        boolean hasProjects = projectScope.type() != ProjectAccessScope.Type.NONE;
        if (!hasCompany && !hasProjects) {
            wrapper.eq(Warehouse::getId, -1L);
            return;
        }
        wrapper.and(scoped -> {
            if (hasCompany) {
                scoped.nested(company -> company.eq(Warehouse::getWarehouseScope, "COMPANY")
                    .in(Warehouse::getCompanyId, companyIds));
            }
            if (hasProjects) {
                if (hasCompany) scoped.or();
                scoped.nested(project -> {
                    project.eq(Warehouse::getWarehouseScope, "PROJECT");
                    if (projectScope.type() == ProjectAccessScope.Type.EXPLICIT_IDS) {
                        project.in(Warehouse::getProjectId, projectScope.projectIds());
                    }
                });
            }
        });
    }

    public List<Long> accessibleWarehouseIds() {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        applyScope(wrapper);
        return warehouseMapper.selectList(wrapper.select(Warehouse::getId)).stream().map(Warehouse::getId).toList();
    }

    /** 返回当前用户可使用物料主数据的公司范围。 */
    public Set<Long> accessibleMaterialCompanyIds() {
        Set<Long> companyIds = new LinkedHashSet<>(accessibleDeptCompanyIds());
        if (LoginHelper.isSuperAdmin()) return companyIds;
        ProjectAccessScope projectScope = projectReferenceService.resolveWarehouseScope();
        if (projectScope.type() != ProjectAccessScope.Type.NONE) {
            companyIds.addAll(warehouseMapper.lambda()
                .select(Warehouse::getCompanyId)
                .eq(Warehouse::getWarehouseScope, "PROJECT")
                .in(projectScope.type() == ProjectAccessScope.Type.EXPLICIT_IDS,
                    Warehouse::getProjectId, projectScope.projectIds())
                .list().stream().map(Warehouse::getCompanyId).collect(Collectors.toSet()));
        }
        return companyIds;
    }

    /** 校验当前用户是否可以访问指定公司的物料主数据。 */
    public void assertMaterialCompanyAccess(Long companyId) {
        if (!LoginHelper.isSuperAdmin() && !accessibleMaterialCompanyIds().contains(companyId)) {
            throw new ServiceException("无该公司物料主数据访问权限");
        }
    }

    /** 校验物料公司存在、启用且在当前用户范围内。 */
    public void validateMaterialCompany(Long companyId) {
        assertMaterialCompanyAccess(companyId);
        SysDeptVo company = deptService.selectDeptById(companyId);
        if (company == null) throw new ServiceException("所属公司/组织不存在");
        if (!SystemConstants.NORMAL.equals(company.getStatus())) throw new ServiceException("所属公司/组织已停用");
    }

    /** 返回当前用户可选择的启用物料公司。 */
    public List<SysDeptVo> selectMaterialCompanyOptions() {
        return accessibleMaterialCompanyIds().stream().map(deptService::selectDeptById)
            .filter(java.util.Objects::nonNull)
            .filter(company -> SystemConstants.NORMAL.equals(company.getStatus()))
            .sorted(java.util.Comparator.comparing(SysDeptVo::getOrderNum).thenComparing(SysDeptVo::getDeptId))
            .toList();
    }

    private Set<Long> accessibleCompanyIds() {
        return accessibleDeptCompanyIds();
    }

    private Set<Long> accessibleDeptCompanyIds() {
        SysDeptBo query = new SysDeptBo();
        return deptService.selectDeptList(query).stream().map(SysDeptVo::getDeptId).collect(Collectors.toSet());
    }
}
