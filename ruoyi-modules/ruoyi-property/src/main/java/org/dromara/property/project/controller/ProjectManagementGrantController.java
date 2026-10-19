package org.dromara.property.project.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.domain.bo.ProjectManagementGrantBo;
import org.dromara.property.project.domain.vo.ProjectManagementGrantVo;
import org.dromara.property.project.service.IProjectManagementGrantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/property/project/admin-scope")
public class ProjectManagementGrantController {
    private final IProjectManagementGrantService grantService;

    @SaCheckPermission("property:projectAdminScope:list")
    @GetMapping("/list")
    public R<PageResult<ProjectManagementGrantVo>> list(ProjectManagementGrantBo bo, PageQuery page) {
        return R.ok(grantService.queryPage(bo, page));
    }

    @SaCheckPermission("property:projectAdminScope:query")
    @GetMapping("/history")
    public R<List<ProjectManagementGrantVo>> history(ProjectManagementGrantBo bo) {
        return R.ok(grantService.queryHistory(bo));
    }

    @SaCheckPermission("property:projectAdminScope:add")
    @Log(title = "项目管理授权", businessType = BusinessType.GRANT)
    @PostMapping
    public R<Void> add(@RequestBody ProjectManagementGrantBo bo) {
        grantService.grant(bo);
        return R.ok();
    }

    @SaCheckPermission("property:projectAdminScope:edit")
    @Log(title = "项目管理授权", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public R<Void> status(@PathVariable Long id, @RequestBody ProjectManagementGrantBo bo) {
        grantService.changeStatus(id, bo.getVersion(), bo.getStatus());
        return R.ok();
    }

    @SaCheckPermission("property:projectAdminScope:remove")
    @Log(title = "项目管理授权", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> revoke(@PathVariable Long id, @RequestParam Integer version) {
        grantService.revoke(id, version);
        return R.ok();
    }
}
