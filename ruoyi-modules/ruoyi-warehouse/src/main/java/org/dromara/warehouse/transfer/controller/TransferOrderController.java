package org.dromara.warehouse.transfer.controller;

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
import org.dromara.warehouse.transfer.domain.bo.TransferOrderBo;
import org.dromara.warehouse.transfer.domain.vo.TransferOrderVo;
import org.dromara.warehouse.transfer.service.ITransferOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 调拨单管理。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/warehouse/transfer")
public class TransferOrderController extends BaseController {
    private final ITransferOrderService service;
    private final WarehouseEditInitializationService editInitializationService;

    @SaCheckPermission("warehouse:transfer:list")
    @GetMapping("/list")
    public R<PageResult<TransferOrderVo>> list(TransferOrderBo bo, PageQuery pq) {
        return R.ok(service.queryPageList(bo, pq));
    }

    @SaCheckPermission("warehouse:transfer:export")
    @Log(title = "调拨单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(TransferOrderBo bo, HttpServletResponse response) {
        ExcelBuilder.of(service.queryList(bo), TransferOrderVo.class)
                .sheetName("调拨单")
                .toResponse(response);
    }

    @SaCheckPermission("warehouse:transfer:query")
    @GetMapping("/{id}")
    public R<TransferOrderVo> getInfo(@NotNull @PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("warehouse:transfer:edit")
    @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.TransferEdit> editContext(
            @NotNull @PathVariable Long id) {
        return R.ok(editInitializationService.transfer(id));
    }

    @SaCheckPermission("warehouse:transfer:add")
    @Log(title = "调拨单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Long> add(@Valid @RequestBody TransferOrderBo bo) {
        return R.ok(service.create(bo));
    }

    @SaCheckPermission("warehouse:transfer:edit")
    @Log(title = "调拨单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Valid @RequestBody TransferOrderBo bo) {
        service.update(bo);
        return R.ok();
    }

    @SaCheckPermission("warehouse:transfer:confirm")
    @Log(title = "确认调拨单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        service.confirm(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:transfer:complete")
    @Log(title = "完成调拨", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/complete")
    public R<Void> complete(@PathVariable Long id) {
        service.complete(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:transfer:cancel")
    @Log(title = "取消调拨单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:transfer:remove")
    @Log(title = "调拨单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty @PathVariable Long[] ids) {
        service.deleteDrafts(List.of(ids));
        return R.ok();
    }
}
