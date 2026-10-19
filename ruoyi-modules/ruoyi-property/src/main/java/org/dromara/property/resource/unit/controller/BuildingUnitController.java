package org.dromara.property.resource.unit.controller;

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
import org.dromara.property.resource.unit.domain.bo.BuildingUnitBo;
import org.dromara.property.resource.unit.domain.vo.BuildingUnitVo;
import org.dromara.property.resource.unit.service.IBuildingUnitService;
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

/**
 * 楼栋单元管理。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/resource/unit")
public class BuildingUnitController extends BaseController {
    private final IBuildingUnitService unitService;

    @SaCheckPermission("property:unit:list")
    @GetMapping("/list")
    public R<PageResult<BuildingUnitVo>> list(BuildingUnitBo bo, PageQuery pageQuery) {
        return R.ok(unitService.queryPage(bo, pageQuery));
    }

    @SaCheckPermission("property:unit:export")
    @Log(title = "楼栋单元", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BuildingUnitBo bo, HttpServletResponse response) {
        ExcelBuilder.of(unitService.queryExport(bo), BuildingUnitVo.class)
            .sheetName("楼栋单元").toResponse(response);
    }

    @SaCheckPermission("property:unit:query")
    @GetMapping("/{id}")
    public R<BuildingUnitVo> get(@PathVariable @NotNull Long id) {
        return R.ok(unitService.queryById(id));
    }

    @SaCheckPermission("property:unit:add")
    @Log(title = "楼栋单元", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BuildingUnitBo bo) {
        return toAjax(unitService.insert(bo));
    }

    @SaCheckPermission("property:unit:edit")
    @Log(title = "楼栋单元", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BuildingUnitBo bo) {
        return toAjax(unitService.update(bo));
    }

    @SaCheckPermission("property:unit:edit")
    @Log(title = "楼栋单元状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody BuildingUnitBo bo) {
        return toAjax(unitService.changeStatus(bo.getId(), bo.getStatus()));
    }

    @SaCheckPermission("property:unit:remove")
    @Log(title = "楼栋单元", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] ids) {
        return toAjax(unitService.delete(List.of(ids)));
    }

    @SaCheckPermission("property:unit:list")
    @GetMapping("/options")
    public R<List<BuildingUnitVo>> options(BuildingUnitBo bo) {
        return R.ok(unitService.options(bo));
    }
}
