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
import org.dromara.property.project.domain.bo.ProjectMemberBo;
import org.dromara.property.project.domain.vo.ProjectMemberExportVo;
import org.dromara.property.project.domain.vo.ProjectMemberOptionVo;
import org.dromara.property.project.domain.vo.ProjectMemberVo;
import org.dromara.property.project.service.IProjectMemberService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 物业项目成员管理。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/property/project/member")
public class ProjectMemberController extends BaseController {

    private final IProjectMemberService memberService;

    @SaCheckPermission("property:projectMember:list")
    @GetMapping("/list")
    public R<PageResult<ProjectMemberVo>> list(ProjectMemberBo bo, PageQuery pageQuery) {
        return R.ok(memberService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("property:projectMember:export")
    @Log(title = "物业项目成员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ProjectMemberBo bo, HttpServletResponse response) {
        List<ProjectMemberExportVo> list = memberService.queryExportList(bo);
        ExcelBuilder.of(list, ProjectMemberExportVo.class).sheetName("项目成员").toResponse(response);
    }

    @SaCheckPermission("property:projectMember:query")
    @GetMapping("/{id}")
    public R<ProjectMemberVo> getInfo(@NotNull(message = "成员关系ID不能为空") @PathVariable Long id) {
        return R.ok(memberService.queryById(id));
    }

    @SaCheckPermission("property:projectMember:add")
    @Log(title = "物业项目成员", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ProjectMemberBo bo) {
        return toAjax(memberService.addMember(bo));
    }

    @SaCheckPermission("property:projectMember:edit")
    @Log(title = "物业项目成员", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ProjectMemberBo bo) {
        return toAjax(memberService.updateMember(bo));
    }

    @SaCheckPermission("property:projectMember:remove")
    @Log(title = "移出物业项目", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/end")
    public R<Void> end(@NotNull(message = "成员关系ID不能为空") @PathVariable Long id,
                       @RequestBody(required = false) ProjectMemberBo bo) {
        return toAjax(memberService.endMember(id, bo == null ? null : bo.getEndDate()));
    }

    @SaCheckPermission("property:projectMember:edit")
    @Log(title = "设置项目负责人", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/set-manager")
    public R<Void> setManager(@NotNull(message = "成员关系ID不能为空") @PathVariable Long id) {
        return toAjax(memberService.setManager(id));
    }

    @SaCheckPermission("property:projectMember:edit")
    @Log(title = "取消项目负责人", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/cancel-manager")
    public R<Void> cancelManager(@NotNull(message = "成员关系ID不能为空") @PathVariable Long id) {
        return toAjax(memberService.cancelManager(id));
    }

    @SaCheckPermission("property:projectMember:remove")
    @Log(title = "删除错误项目成员关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "成员关系ID不能为空") @PathVariable Long[] ids) {
        return toAjax(memberService.deleteWrongMembers(List.of(ids)));
    }

    @SaCheckPermission("property:projectMember:list")
    @GetMapping("/options")
    public R<List<ProjectMemberOptionVo>> options(@RequestParam(required = false) Long projectId) {
        return R.ok(memberService.selectOptions(projectId));
    }
}
