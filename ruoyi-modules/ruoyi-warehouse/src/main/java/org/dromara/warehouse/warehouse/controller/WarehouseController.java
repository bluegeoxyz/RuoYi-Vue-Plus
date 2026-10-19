package org.dromara.warehouse.warehouse.controller;

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
import org.dromara.property.api.ProjectReferenceDTO;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;
import org.dromara.warehouse.context.service.WarehouseEditInitializationService;
import org.dromara.warehouse.warehouse.domain.bo.WarehouseBo;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseOptionVo;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseVo;
import org.dromara.warehouse.warehouse.service.IWarehouseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 仓库管理。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/warehouse")
public class WarehouseController extends BaseController {
    private final IWarehouseService warehouseService;
    private final WarehouseEditInitializationService editInitializationService;

    @SaCheckPermission("warehouse:warehouse:list")
    @GetMapping("/list")
    public R<PageResult<WarehouseVo>> list(WarehouseBo bo, PageQuery pageQuery) { return R.ok(warehouseService.queryPageList(bo, pageQuery)); }

    @SaCheckPermission("warehouse:warehouse:query")
    @GetMapping("/{id}")
    public R<WarehouseVo> getInfo(@NotNull(message = "仓库ID不能为空") @PathVariable Long id) { return R.ok(warehouseService.queryById(id)); }

    @SaCheckPermission("warehouse:warehouse:edit")
    @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.WarehouseEdit> editContext(
        @NotNull(message = "仓库ID不能为空") @PathVariable Long id) {
        return R.ok(editInitializationService.warehouse(id));
    }

    @SaCheckPermission("warehouse:warehouse:add")
    @Log(title = "仓库", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WarehouseBo bo) { return toAjax(warehouseService.insertByBo(bo)); }

    @SaCheckPermission("warehouse:warehouse:edit")
    @Log(title = "仓库", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody WarehouseBo bo) { return toAjax(warehouseService.updateByBo(bo)); }

    @SaCheckPermission("warehouse:warehouse:edit")
    @Log(title = "仓库状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody WarehouseBo bo) { return toAjax(warehouseService.changeStatus(bo.getId(), bo.getStatus())); }

    @SaCheckPermission("warehouse:warehouse:remove")
    @Log(title = "仓库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "仓库ID不能为空") @PathVariable Long[] ids) { return toAjax(warehouseService.deleteWithValidByIds(List.of(ids))); }

    @SaCheckPermission("warehouse:warehouse:list")
    @GetMapping("/options")
    public R<List<WarehouseOptionVo>> options() { return R.ok(warehouseService.selectOptions()); }

    @SaCheckPermission("warehouse:warehouse:list")
    @GetMapping("/company-options")
    public R<List<SysDeptVo>> companyOptions() { return R.ok(warehouseService.selectCompanyOptions()); }

    @SaCheckPermission("warehouse:warehouse:list")
    @GetMapping("/project-options")
    public R<List<ProjectReferenceDTO>> projectOptions() { return R.ok(warehouseService.selectProjectOptions()); }
}
