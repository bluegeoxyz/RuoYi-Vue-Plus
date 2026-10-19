package org.dromara.warehouse.outbound.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.*;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;
import org.dromara.warehouse.context.service.WarehouseEditInitializationService;
import org.dromara.warehouse.outbound.domain.bo.OutboundOrderBo;
import org.dromara.warehouse.outbound.domain.vo.OutboundOrderVo;
import org.dromara.warehouse.outbound.service.IOutboundOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** 出库单管理。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/warehouse/outbound")
public class OutboundOrderController extends BaseController {
    private final IOutboundOrderService service;
    private final WarehouseEditInitializationService editInitializationService;

    @SaCheckPermission("warehouse:outbound:list")
    @GetMapping("/list")
    public R<PageResult<OutboundOrderVo>> list(OutboundOrderBo bo, PageQuery pq) {
        return R.ok(service.queryPageList(bo, pq));
    }

    @SaCheckPermission("warehouse:outbound:export")
    @Log(title = "出库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(OutboundOrderBo bo, HttpServletResponse response) {
        ExcelBuilder.of(service.queryList(bo), OutboundOrderVo.class)
                .sheetName("出库单")
                .toResponse(response);
    }

    @SaCheckPermission("warehouse:outbound:query")
    @GetMapping("/{id}")
    public R<OutboundOrderVo> getInfo(@NotNull @PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("warehouse:outbound:edit")
    @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.OutboundEdit> editContext(
            @NotNull @PathVariable Long id) {
        return R.ok(editInitializationService.outbound(id));
    }

    @SaCheckPermission("warehouse:outbound:add")
    @Log(title = "出库单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Long> add(@Valid @RequestBody OutboundOrderBo bo) {
        return R.ok(service.create(bo));
    }

    @SaCheckPermission("warehouse:outbound:edit")
    @Log(title = "出库单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Valid @RequestBody OutboundOrderBo bo) {
        service.update(bo);
        return R.ok();
    }

    @SaCheckPermission("warehouse:outbound:confirm")
    @Log(title = "确认出库单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        service.confirm(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:outbound:complete")
    @Log(title = "完成出库", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/complete")
    public R<Void> complete(@PathVariable Long id) {
        service.complete(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:outbound:cancel")
    @Log(title = "取消出库单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return R.ok();
    }

    @SaCheckPermission("warehouse:outbound:remove")
    @Log(title = "出库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty @PathVariable Long[] ids) {
        service.deleteDrafts(List.of(ids));
        return R.ok();
    }
}
