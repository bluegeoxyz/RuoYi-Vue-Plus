package org.dromara.warehouse.zone.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.service.WarehouseMasterDataQueryOptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 库区只读查询上下文选项。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/zone/context/query")
public class WarehouseZoneQueryContextOptionController {

    private static final BusinessContext CONTEXT = BusinessContext.WAREHOUSE_ZONE_QUERY;

    private final WarehouseMasterDataQueryOptionService optionService;

    @SaCheckPermission(value = {"warehouse:zone:list", "warehouse:zone:query"}, mode = SaMode.OR)
    @GetMapping("/warehouses")
    public R<List<DocumentWarehouseOptionVo>> warehouses() {
        return R.ok(optionService.selectWarehouses(CONTEXT));
    }
}
