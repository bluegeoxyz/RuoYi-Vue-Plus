package org.dromara.warehouse.location.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentZoneOptionVo;
import org.dromara.warehouse.context.service.WarehouseMasterDataQueryOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 库位只读查询上下文选项。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/location/context/query")
public class WarehouseLocationQueryContextOptionController {

    private static final BusinessContext CONTEXT = BusinessContext.WAREHOUSE_LOCATION_QUERY;

    private final WarehouseMasterDataQueryOptionService optionService;

    @SaCheckPermission(value = {"warehouse:location:list", "warehouse:location:query"}, mode = SaMode.OR)
    @GetMapping("/warehouses")
    public R<List<DocumentWarehouseOptionVo>> warehouses() {
        return R.ok(optionService.selectWarehouses(CONTEXT));
    }

    @SaCheckPermission(value = {"warehouse:location:list", "warehouse:location:query"}, mode = SaMode.OR)
    @GetMapping("/zones")
    public R<List<DocumentZoneOptionVo>> zones(@NotNull @RequestParam Long warehouseId) {
        return R.ok(optionService.selectZones(CONTEXT, warehouseId));
    }
}
