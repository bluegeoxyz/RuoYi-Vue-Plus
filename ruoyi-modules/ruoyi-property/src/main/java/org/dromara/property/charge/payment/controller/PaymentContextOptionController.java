package org.dromara.property.charge.payment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.property.charge.context.domain.vo.PaymentReceivableOptionVo;
import org.dromara.property.charge.context.service.FinanceContextOptionService;
import org.dromara.property.charge.payment.domain.vo.PaymentRoomOptionVo;
import org.dromara.property.customer.relation.domain.vo.CustomerReferenceOptionVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated @RestController @RequiredArgsConstructor
@RequestMapping("/property/payment/context/create")
public class PaymentContextOptionController {
    private final FinanceContextOptionService optionService;

    @SaCheckPermission(value = {"property:payment:add", "property:payment:allocate"}, mode = SaMode.AND)
    @GetMapping("/rooms")
    public R<List<PaymentRoomOptionVo>> rooms(@RequestParam @NotNull Long projectId) {
        return R.ok(optionService.paymentRooms(projectId));
    }

    @SaCheckPermission(value = {"property:payment:add", "property:payment:allocate"}, mode = SaMode.AND)
    @GetMapping("/customers")
    public R<List<CustomerReferenceOptionVo>> customers(@RequestParam @NotNull Long projectId,
                                                         @RequestParam String keyword) {
        return R.ok(optionService.paymentCustomers(projectId, keyword));
    }

    @SaCheckPermission(value = {"property:payment:add", "property:payment:allocate"}, mode = SaMode.AND)
    @GetMapping("/receivables")
    public R<List<PaymentReceivableOptionVo>> receivables(@RequestParam @NotNull Long projectId,
                                                           @RequestParam @NotNull Long roomId) {
        return R.ok(optionService.paymentReceivables(projectId, roomId));
    }
}
