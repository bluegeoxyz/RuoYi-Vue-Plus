package org.dromara.property.charge.payment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.*;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.property.charge.payment.domain.bo.*;
import org.dromara.property.charge.payment.domain.vo.PaymentVo;
import org.dromara.property.charge.payment.domain.vo.PaymentExportVo;
import org.dromara.property.charge.payment.domain.vo.PaymentRoomOptionVo;
import org.dromara.property.charge.payment.domain.vo.PaymentReverseResultVo;
import org.dromara.property.charge.payment.service.IPaymentService;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/payment")
public class PaymentController extends BaseController {
    private final IPaymentService service;

    @SaCheckPermission("property:payment:list")
    @GetMapping("/list")
    public R<PageResult<PaymentVo>> list(PaymentQueryBo bo, PageQuery pq) {
        return R.ok(service.queryPage(bo, pq));
    }

    @SaCheckPermission("property:payment:export")
    @Log(title = "收款管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PaymentQueryBo bo, HttpServletResponse response) {
        ExcelBuilder.of(service.export(bo), PaymentExportVo.class)
                .sheetName("收款管理")
                .toResponse(response);
    }

    @SaCheckPermission("property:payment:query")
    @GetMapping("/{id}")
    public R<PaymentVo> detail(@PathVariable @NotNull Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission(
            value = {"property:payment:add", "property:payment:allocate"},
            mode = SaMode.AND)
    @GetMapping("/room-options")
    public R<List<PaymentRoomOptionVo>> roomOptions(@RequestParam @NotNull Long projectId) {
        return R.ok(service.roomOptions(projectId));
    }

    @SaCheckPermission(
            value = {"property:payment:add", "property:payment:allocate"},
            mode = SaMode.AND)
    @RepeatSubmit
    @Log(title = "收款核销", businessType = BusinessType.INSERT)
    @PostMapping
    public R<PaymentVo> create(@Valid @RequestBody PaymentCreateBo bo) {
        return R.ok(service.create(bo));
    }

    @SaCheckPermission("property:payment:reverse")
    @RepeatSubmit
    @Log(title = "收款冲销", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/reverse")
    public R<PaymentReverseResultVo> reverse(
            @PathVariable @NotNull Long id, @Valid @RequestBody PaymentReverseBo bo) {
        return R.ok(service.reverse(id, bo));
    }
}
