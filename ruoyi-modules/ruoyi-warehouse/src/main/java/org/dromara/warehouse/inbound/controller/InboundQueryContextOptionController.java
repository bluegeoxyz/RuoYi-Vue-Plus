package org.dromara.warehouse.inbound.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.service.WarehouseDocumentContextOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 入库单只读查询上下文选项。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/inbound/context/query")
public class InboundQueryContextOptionController {

    private static final BusinessContext CONTEXT = BusinessContext.INBOUND_QUERY;

    private final WarehouseDocumentContextOptionService optionService;

    /** 查询当前用户可访问的仓库筛选项。 */
    @SaCheckPermission(value = {"warehouse:inbound:list", "warehouse:inbound:query"}, mode = SaMode.OR)
    @GetMapping("/warehouses")
    public R<List<DocumentWarehouseOptionVo>> warehouses() {
        return R.ok(optionService.selectWarehouses(CONTEXT));
    }
}
