package org.dromara.warehouse.unit.controller;

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
import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;
import org.dromara.warehouse.context.service.WarehouseEditInitializationService;
import org.dromara.warehouse.unit.domain.bo.UnitBo;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.dromara.warehouse.unit.domain.vo.UnitVo;
import org.dromara.warehouse.unit.service.IUnitService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 计量单位管理。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/unit")
public class UnitController extends BaseController {
    private final IUnitService unitService;
    private final WarehouseEditInitializationService editInitializationService;

    @SaCheckPermission("warehouse:unit:list")
    @GetMapping("/list")
    public R<PageResult<UnitVo>> list(UnitBo bo, PageQuery pageQuery) {
        return R.ok(unitService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("warehouse:unit:query")
    @GetMapping("/{id}")
    public R<UnitVo> getInfo(@NotNull(message = "单位ID不能为空") @PathVariable Long id) {
        return R.ok(unitService.queryById(id));
    }

    @SaCheckPermission("warehouse:unit:edit")
    @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.UnitEdit> editContext(
            @NotNull(message = "单位ID不能为空") @PathVariable Long id) {
        return R.ok(editInitializationService.unit(id));
    }

    @SaCheckPermission("warehouse:unit:add")
    @Log(title = "计量单位", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UnitBo bo) {
        return toAjax(unitService.insertByBo(bo));
    }

    @SaCheckPermission("warehouse:unit:edit")
    @Log(title = "计量单位", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UnitBo bo) {
        return toAjax(unitService.updateByBo(bo));
    }

    @SaCheckPermission("warehouse:unit:edit")
    @Log(title = "计量单位状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody UnitBo bo) {
        return toAjax(unitService.changeStatus(bo.getId(), bo.getStatus()));
    }

    @SaCheckPermission("warehouse:unit:remove")
    @Log(title = "计量单位", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "单位ID不能为空") @PathVariable Long[] ids) {
        return toAjax(unitService.deleteWithValidByIds(List.of(ids)));
    }

    @SaCheckPermission("warehouse:unit:list")
    @GetMapping("/options")
    public R<List<UnitOptionVo>> options() {
        return R.ok(unitService.selectOptions());
    }
}
