package org.dromara.warehouse.material.category.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;
import org.dromara.warehouse.context.service.WarehouseEditInitializationService;
import org.dromara.warehouse.material.category.domain.bo.MaterialCategoryBo;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryVo;
import org.dromara.warehouse.material.category.service.IMaterialCategoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 物料分类管理。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/materialCategory")
public class MaterialCategoryController extends BaseController {
    private final IMaterialCategoryService categoryService;
    private final WarehouseEditInitializationService editInitializationService;

    @SaCheckPermission("warehouse:materialCategory:list") @GetMapping("/list")
    public R<List<MaterialCategoryVo>> list(MaterialCategoryBo bo) { return R.ok(categoryService.queryTree(bo)); }

    @SaCheckPermission("warehouse:materialCategory:query") @GetMapping("/{id}")
    public R<MaterialCategoryVo> getInfo(@NotNull(message = "分类ID不能为空") @PathVariable Long id) { return R.ok(categoryService.queryById(id)); }

    @SaCheckPermission("warehouse:materialCategory:edit") @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.MaterialCategoryEdit> editContext(@NotNull(message = "分类ID不能为空") @PathVariable Long id) { return R.ok(editInitializationService.materialCategory(id)); }

    @SaCheckPermission("warehouse:materialCategory:add") @Log(title = "物料分类", businessType = BusinessType.INSERT) @RepeatSubmit @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MaterialCategoryBo bo) { return toAjax(categoryService.insertByBo(bo)); }

    @SaCheckPermission("warehouse:materialCategory:edit") @Log(title = "物料分类", businessType = BusinessType.UPDATE) @RepeatSubmit @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MaterialCategoryBo bo) { return toAjax(categoryService.updateByBo(bo)); }

    @SaCheckPermission("warehouse:materialCategory:edit") @Log(title = "物料分类状态", businessType = BusinessType.UPDATE) @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody MaterialCategoryBo bo) { return toAjax(categoryService.changeStatus(bo.getId(), bo.getStatus())); }

    @SaCheckPermission("warehouse:materialCategory:remove") @Log(title = "物料分类", businessType = BusinessType.DELETE) @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "分类ID不能为空") @PathVariable Long[] ids) { return toAjax(categoryService.deleteWithValidByIds(List.of(ids))); }

    @SaCheckPermission("warehouse:materialCategory:list") @GetMapping("/options")
    public R<List<MaterialCategoryOptionVo>> options(@RequestParam(required = false) Long companyId) { return R.ok(categoryService.selectOptions(companyId)); }
}
