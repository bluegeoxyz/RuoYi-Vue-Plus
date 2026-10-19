package org.dromara.warehouse.stocktake.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;
import org.dromara.warehouse.context.service.WarehouseEditInitializationService;
import org.dromara.warehouse.stocktake.domain.bo.StocktakeOrderBo;
import org.dromara.warehouse.stocktake.domain.vo.StocktakeOrderVo;
import org.dromara.warehouse.stocktake.service.IStocktakeOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 盘点单管理。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/warehouse/stocktake")
public class StocktakeOrderController extends BaseController {
    private final IStocktakeOrderService service;
    private final WarehouseEditInitializationService editInitializationService;

    @SaCheckPermission("warehouse:stocktake:list")
    @GetMapping("/list")
    public R<PageResult<StocktakeOrderVo>> list(StocktakeOrderBo bo, PageQuery pq) {
        return R.ok(service.queryPageList(bo, pq));
    }

    @SaCheckPermission("warehouse:stocktake:export")
    @Log(title = "盘点单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(StocktakeOrderBo bo, HttpServletResponse response) {
        ExcelBuilder.of(service.queryList(bo), StocktakeOrderVo.class)
                .sheetName("盘点单")
                .toResponse(response);
    }

    @SaCheckPermission("warehouse:stocktake:query")
    @GetMapping("/{id}")
    public R<StocktakeOrderVo> getInfo(@NotNull @PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("warehouse:stocktake:edit")
    @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.StocktakeEdit> editContext(
            @NotNull @PathVariable Long id) {
        return R.ok(editInitializationService.stocktake(id));
    }

    @SaCheckPermission("warehouse:stocktake:add")
    @Log(title = "盘点单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Long> add(@Valid @RequestBody StocktakeOrderBo bo) {
        return R.ok(service.create(bo));
    }

    @SaCheckPermission("warehouse:stocktake:edit")
    @Log(title = "盘点单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Valid @RequestBody StocktakeOrderBo bo) {
        service.update(bo);
        return R.ok();
    }

    @SaCheckPermission("warehouse:stocktake:confirm")
    @Log(title = "确认盘点单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        service.confirm(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:stocktake:complete")
    @Log(title = "完成盘点", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/complete")
    public R<Void> complete(@PathVariable Long id) {
        service.complete(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:stocktake:cancel")
    @Log(title = "取消盘点单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:stocktake:remove")
    @Log(title = "盘点单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty @PathVariable Long[] ids) {
        service.deleteDrafts(List.of(ids));
        return R.ok();
    }
}
