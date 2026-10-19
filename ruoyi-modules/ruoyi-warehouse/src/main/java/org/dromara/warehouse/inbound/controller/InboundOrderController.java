package org.dromara.warehouse.inbound.controller;

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
import org.dromara.warehouse.inbound.domain.bo.InboundOrderBo;
import org.dromara.warehouse.inbound.domain.vo.InboundOrderVo;
import org.dromara.warehouse.inbound.service.IInboundOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 入库单管理。 */
@Validated @RequiredArgsConstructor @RestController @RequestMapping("/warehouse/inbound")
public class InboundOrderController extends BaseController {
    private final IInboundOrderService inboundService;
    private final WarehouseEditInitializationService editInitializationService;

    @SaCheckPermission("warehouse:inbound:list") @GetMapping("/list")
    public R<PageResult<InboundOrderVo>> list(InboundOrderBo bo, PageQuery pageQuery) { return R.ok(inboundService.queryPageList(bo, pageQuery)); }

    @SaCheckPermission("warehouse:inbound:export") @Log(title = "入库单", businessType = BusinessType.EXPORT) @PostMapping("/export")
    public void export(InboundOrderBo bo, HttpServletResponse response) { ExcelBuilder.of(inboundService.queryList(bo), InboundOrderVo.class).sheetName("入库单").toResponse(response); }

    @SaCheckPermission("warehouse:inbound:query") @GetMapping("/{id}")
    public R<InboundOrderVo> getInfo(@NotNull @PathVariable Long id) { return R.ok(inboundService.queryById(id)); }

    @SaCheckPermission("warehouse:inbound:edit") @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.InboundEdit> editContext(@NotNull @PathVariable Long id) { return R.ok(editInitializationService.inbound(id)); }

    @SaCheckPermission("warehouse:inbound:add") @Log(title = "入库单", businessType = BusinessType.INSERT) @RepeatSubmit @PostMapping
    public R<Long> add(@Valid @RequestBody InboundOrderBo bo) { return R.ok(inboundService.create(bo)); }

    @SaCheckPermission("warehouse:inbound:edit") @Log(title = "入库单", businessType = BusinessType.UPDATE) @RepeatSubmit @PutMapping
    public R<Void> edit(@Valid @RequestBody InboundOrderBo bo) { inboundService.update(bo); return R.ok(); }

    @SaCheckPermission("warehouse:inbound:confirm") @Log(title = "确认入库单", businessType = BusinessType.UPDATE) @RepeatSubmit @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) { inboundService.confirm(id); return R.ok(); }

    @SaCheckPermission("warehouse:inbound:complete") @Log(title = "完成入库", businessType = BusinessType.UPDATE) @RepeatSubmit @PostMapping("/{id}/complete")
    public R<Void> complete(@PathVariable Long id) { inboundService.complete(id); return R.ok(); }

    @SaCheckPermission("warehouse:inbound:cancel") @Log(title = "取消入库单", businessType = BusinessType.UPDATE) @RepeatSubmit @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) { inboundService.cancel(id); return R.ok(); }

    @SaCheckPermission("warehouse:inbound:remove") @Log(title = "入库单", businessType = BusinessType.DELETE) @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty @PathVariable Long[] ids) { inboundService.deleteDrafts(List.of(ids)); return R.ok(); }
}
