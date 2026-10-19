package org.dromara.warehouse.outbound.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.DocumentInventoryOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentLocationOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentMaterialOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentWarehouseOptionVo;
import org.dromara.warehouse.context.domain.vo.DocumentZoneOptionVo;
import org.dromara.warehouse.context.service.WarehouseDocumentContextOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 出库制单业务上下文选项。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/outbound/context")
public class OutboundContextOptionController {
    private static final BusinessContext CONTEXT = BusinessContext.OUTBOUND_MAINTAIN;
    private final WarehouseDocumentContextOptionService optionService;

    @SaCheckPermission(value = {"warehouse:outbound:add", "warehouse:outbound:edit"}, mode = SaMode.OR)
    @GetMapping("/warehouses")
    public R<List<DocumentWarehouseOptionVo>> warehouses() { return R.ok(optionService.selectWarehouses(CONTEXT)); }

    @SaCheckPermission(value = {"warehouse:outbound:add", "warehouse:outbound:edit"}, mode = SaMode.OR)
    @GetMapping("/zones")
    public R<List<DocumentZoneOptionVo>> zones(@NotNull @RequestParam Long warehouseId) {
        return R.ok(optionService.selectZones(CONTEXT, warehouseId));
    }

    @SaCheckPermission(value = {"warehouse:outbound:add", "warehouse:outbound:edit"}, mode = SaMode.OR)
    @GetMapping("/locations")
    public R<List<DocumentLocationOptionVo>> locations(@NotNull @RequestParam Long warehouseId,
                                                       @RequestParam(required = false) Long zoneId) {
        return R.ok(optionService.selectLocations(CONTEXT, warehouseId, zoneId));
    }

    @SaCheckPermission(value = {"warehouse:outbound:add", "warehouse:outbound:edit"}, mode = SaMode.OR)
    @GetMapping("/materials")
    public R<List<DocumentMaterialOptionVo>> materials(@NotNull @RequestParam Long warehouseId) {
        return R.ok(optionService.selectMaterials(CONTEXT, warehouseId));
    }

    @SaCheckPermission(value = {"warehouse:outbound:add", "warehouse:outbound:edit"}, mode = SaMode.OR)
    @GetMapping("/inventory")
    public R<DocumentInventoryOptionVo> inventory(@NotNull @RequestParam Long warehouseId,
                                                  @NotNull @RequestParam Long locationId,
                                                  @NotNull @RequestParam Long materialId) {
        return R.ok(optionService.selectInventory(CONTEXT, warehouseId, locationId, materialId));
    }
}
