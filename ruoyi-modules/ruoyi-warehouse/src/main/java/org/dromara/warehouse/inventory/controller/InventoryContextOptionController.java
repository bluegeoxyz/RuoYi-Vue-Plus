package org.dromara.warehouse.inventory.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.ContextCompanyOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentLocationOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentMaterialOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentZoneOptionVo;
import org.dromara.warehouse.context.service.WarehouseDocumentContextOptionService;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 库存余额查询上下文选项。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/inventory/context")
public class InventoryContextOptionController {
    private static final BusinessContext CONTEXT = BusinessContext.INVENTORY_QUERY;
    private final WarehouseDocumentContextOptionService optionService;

    @SaCheckPermission(value = {"warehouse:inventory:list", "warehouse:inventory:query"}, mode = SaMode.OR)
    @GetMapping("/companies")
    public R<List<ContextCompanyOptionVo>> companies() { return R.ok(optionService.selectCompanies(CONTEXT)); }

    @SaCheckPermission(value = {"warehouse:inventory:list", "warehouse:inventory:query"}, mode = SaMode.OR)
    @GetMapping("/warehouses")
    public R<List<DocumentWarehouseOptionVo>> warehouses() { return R.ok(optionService.selectWarehouses(CONTEXT)); }

    @SaCheckPermission(value = {"warehouse:inventory:list", "warehouse:inventory:query"}, mode = SaMode.OR)
    @GetMapping("/categories")
    public R<List<MaterialCategoryOptionVo>> categories(@RequestParam(required = false) Long companyId) {
        return R.ok(optionService.selectMaterialCategories(CONTEXT, companyId));
    }

    @SaCheckPermission(value = {"warehouse:inventory:list", "warehouse:inventory:query"}, mode = SaMode.OR)
    @GetMapping("/zones")
    public R<List<DocumentZoneOptionVo>> zones(@NotNull @RequestParam Long warehouseId) {
        return R.ok(optionService.selectZones(CONTEXT, warehouseId));
    }

    @SaCheckPermission(value = {"warehouse:inventory:list", "warehouse:inventory:query"}, mode = SaMode.OR)
    @GetMapping("/locations")
    public R<List<DocumentLocationOptionVo>> locations(@NotNull @RequestParam Long warehouseId,
                                                       @RequestParam(required = false) Long zoneId) {
        return R.ok(optionService.selectLocations(CONTEXT, warehouseId, zoneId));
    }

    @SaCheckPermission(value = {"warehouse:inventory:list", "warehouse:inventory:query"}, mode = SaMode.OR)
    @GetMapping("/materials")
    public R<List<DocumentMaterialOptionVo>> materials(@NotNull @RequestParam Long warehouseId) {
        return R.ok(optionService.selectMaterials(CONTEXT, warehouseId));
    }
}
