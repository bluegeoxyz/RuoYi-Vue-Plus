package org.dromara.property.charge.receivable.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.property.charge.context.service.FinanceContextOptionService;
import org.dromara.property.charge.item.domain.vo.ChargeItemOptionVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated @RestController @RequiredArgsConstructor
@RequestMapping("/property/receivable/context/query")
public class ReceivableContextOptionController {
    private final FinanceContextOptionService optionService;

    @SaCheckPermission("property:receivable:list")
    @GetMapping("/charge-items")
    public R<List<ChargeItemOptionVo>> chargeItems(@RequestParam @NotNull Long projectId) {
        return R.ok(optionService.chargeItems(BusinessContext.RECEIVABLE_QUERY, projectId, null));
    }
}
