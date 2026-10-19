package org.dromara.warehouse.material.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;
import org.dromara.warehouse.context.service.WarehouseEditInitializationService;
import org.dromara.warehouse.material.domain.bo.MaterialBo;
import org.dromara.warehouse.material.domain.vo.MaterialOptionVo;
import org.dromara.warehouse.material.domain.vo.MaterialVo;
import org.dromara.warehouse.material.service.IMaterialService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 物料管理。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/material")
public class MaterialController extends BaseController {
    private final IMaterialService materialService;
    private final WarehouseEditInitializationService editInitializationService;

    @SaCheckPermission("warehouse:material:list")
    @GetMapping("/list")
    public R<PageResult<MaterialVo>> list(MaterialBo bo, PageQuery pageQuery) {
        return R.ok(materialService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("warehouse:material:query")
    @GetMapping("/{id}")
    public R<MaterialVo> getInfo(@NotNull(message = "物料ID不能为空") @PathVariable Long id) {
        return R.ok(materialService.queryById(id));
    }

    @SaCheckPermission("warehouse:material:edit")
    @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.MaterialEdit> editContext(
            @NotNull(message = "物料ID不能为空") @PathVariable Long id) {
        return R.ok(editInitializationService.material(id));
    }

    @SaCheckPermission("warehouse:material:add")
    @Log(title = "物料", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MaterialBo bo) {
        return toAjax(materialService.insertByBo(bo));
    }

    @SaCheckPermission("warehouse:material:edit")
    @Log(title = "物料", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MaterialBo bo) {
        return toAjax(materialService.updateByBo(bo));
    }

    @SaCheckPermission("warehouse:material:edit")
    @Log(title = "物料状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody MaterialBo bo) {
        return toAjax(materialService.changeStatus(bo.getId(), bo.getStatus()));
    }

    @SaCheckPermission("warehouse:material:remove")
    @Log(title = "物料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "物料ID不能为空") @PathVariable Long[] ids) {
        return toAjax(materialService.deleteWithValidByIds(List.of(ids)));
    }

    @SaCheckPermission("warehouse:material:list")
    @GetMapping("/options")
    public R<List<MaterialOptionVo>> options(@RequestParam(required = false) Long companyId) {
        return R.ok(materialService.selectOptions(companyId));
    }

    @SaCheckPermission("warehouse:material:list")
    @GetMapping("/company-options")
    public R<List<SysDeptVo>> companyOptions() {
        return R.ok(materialService.selectCompanyOptions());
    }
}
