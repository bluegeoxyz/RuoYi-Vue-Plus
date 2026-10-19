package org.dromara.property.project.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.property.project.domain.bo.ProjectBo;
import org.dromara.property.project.domain.vo.ProjectExportVo;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.project.domain.vo.ProjectVo;
import org.dromara.property.project.service.IProjectService;
import org.dromara.property.project.service.ProjectManagementApplicationService;
import org.dromara.property.project.domain.bo.CreateManagedProjectCommand;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 物业项目管理。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/property/project")
public class ProjectController extends BaseController {

    private final IProjectService projectService;
    private final ProjectManagementApplicationService projectManagementApplicationService;

    @SaCheckPermission("property:project:list")
    @GetMapping("/list")
    public R<PageResult<ProjectVo>> list(ProjectBo bo, PageQuery pageQuery) {
        return R.ok(projectService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("property:project:export")
    @Log(title = "物业项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ProjectBo bo, HttpServletResponse response) {
        List<ProjectExportVo> list = projectService.queryExportList(bo);
        ExcelBuilder.of(list, ProjectExportVo.class).sheetName("物业项目").toResponse(response);
    }

    @SaCheckPermission("property:project:query")
    @GetMapping("/{id}")
    public R<ProjectVo> getInfo(@NotNull(message = "项目ID不能为空") @PathVariable Long id) {
        return R.ok(projectService.queryById(id));
    }

    @SaCheckPermission("property:project:add")
    @Log(title = "物业项目", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ProjectBo bo) {
        return toAjax(projectService.insertByBo(bo));
    }

    @SaCheckPermission("property:project:add")
    @Log(title = "物业项目初始化", businessType = BusinessType.INSERT)
    @PostMapping("/initialize")
    public R<Void> initialize(@RequestBody CreateManagedProjectCommand command) {
        return toAjax(projectManagementApplicationService.createManagedProject(command));
    }

    @SaCheckPermission("property:project:edit")
    @Log(title = "物业项目", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ProjectBo bo) {
        return toAjax(projectService.updateByBo(bo));
    }

    @SaCheckPermission("property:project:edit")
    @Log(title = "物业项目状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody ProjectBo bo) {
        if (bo.getId() == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        return toAjax(projectService.changeStatus(bo.getId(), bo.getStatus()));
    }

    @SaCheckPermission("property:project:remove")
    @Log(title = "物业项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "项目ID不能为空") @PathVariable Long[] ids) {
        return toAjax(projectService.deleteWithValidByIds(List.of(ids)));
    }

    @SaCheckPermission("property:project:list")
    @GetMapping("/options")
    public R<List<ProjectOptionVo>> options() {
        return R.ok(projectService.selectOptions());
    }

    /** 业务页面使用的项目选择项，仅返回当前有效成员项目。 */
    @SaCheckPermission("property:project:list")
    @GetMapping("/business-options")
    public R<List<ProjectOptionVo>> businessOptions() {
        return R.ok(projectService.selectBusinessOptions());
    }
}
