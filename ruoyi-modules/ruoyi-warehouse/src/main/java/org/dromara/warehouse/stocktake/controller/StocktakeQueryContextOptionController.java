package org.dromara.warehouse.stocktake.controller;

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

/** 盘点单只读查询上下文选项。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/stocktake/context/query")
public class StocktakeQueryContextOptionController {

    private static final BusinessContext CONTEXT = BusinessContext.STOCKTAKE_QUERY;

    private final WarehouseDocumentContextOptionService optionService;

    /** 查询当前用户可访问的仓库筛选项。 */
    @SaCheckPermission(value = {"warehouse:stocktake:list", "warehouse:stocktake:query"}, mode = SaMode.OR)
    @GetMapping("/warehouses")
    public R<List<DocumentWarehouseOptionVo>> warehouses() {
        return R.ok(optionService.selectWarehouses(CONTEXT));
    }
}
