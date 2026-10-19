package org.dromara.property.resource.building.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
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
import org.dromara.common.web.core.BaseController;
import org.dromara.property.resource.building.domain.bo.BuildingBo;
import org.dromara.property.resource.building.domain.vo.BuildingVo;
import org.dromara.property.resource.building.domain.vo.BuildingExportVo;
import org.dromara.property.resource.building.service.IBuildingService;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/resource/building")
public class BuildingController extends BaseController {
    private final IBuildingService buildingService;

    @SaCheckPermission("property:building:list")
    @GetMapping("/list")
    public R<PageResult<BuildingVo>> list(BuildingBo bo, PageQuery pageQuery) {
        return R.ok(buildingService.queryPage(bo, pageQuery));
    }

    @SaCheckPermission("property:building:export")
    @Log(title = "楼栋", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BuildingBo bo, HttpServletResponse response) {
        ExcelBuilder.of(BeanUtil.copyToList(buildingService.queryExport(bo), BuildingExportVo.class), BuildingExportVo.class)
            .sheetName("楼栋").toResponse(response);
    }

    @SaCheckPermission("property:building:query")
    @GetMapping("/{id}")
    public R<BuildingVo> get(@PathVariable @NotNull Long id) {
        return R.ok(buildingService.queryById(id));
    }

    @SaCheckPermission("property:building:add")
    @Log(title = "楼栋", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BuildingBo bo) {
        return toAjax(buildingService.insert(bo));
    }

    @SaCheckPermission("property:building:edit")
    @Log(title = "楼栋", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BuildingBo bo) {
        return toAjax(buildingService.update(bo));
    }

    @SaCheckPermission("property:building:edit")
    @Log(title = "楼栋状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> status(@RequestBody BuildingBo bo) {
        return toAjax(buildingService.changeStatus(bo.getId(), bo.getStatus()));
    }

    @SaCheckPermission("property:building:remove")
    @Log(title = "楼栋", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] ids) {
        return toAjax(buildingService.delete(List.of(ids)));
    }

    @SaCheckPermission("property:building:list")
    @GetMapping("/options")
    public R<List<BuildingVo>> options(BuildingBo bo) {
        return R.ok(buildingService.options(bo));
    }

    @SaCheckPermission("property:building:list")
    @GetMapping("/project-options")
    public R<List<ProjectOptionVo>> projectOptions() {
        return R.ok(buildingService.projectOptions());
    }
}
