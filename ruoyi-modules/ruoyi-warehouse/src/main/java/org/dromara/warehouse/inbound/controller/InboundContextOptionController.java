package org.dromara.warehouse.inbound.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.warehouse.inbound.domain.vo.InboundLocationOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundMaterialOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundWarehouseOptionVo;
import org.dromara.warehouse.inbound.domain.vo.InboundZoneOptionVo;
import org.dromara.warehouse.inbound.service.IInboundContextOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 入库制单业务上下文选项。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/inbound/context")
public class InboundContextOptionController {

    private final IInboundContextOptionService optionService;

    @SaCheckPermission(value = {"warehouse:inbound:add", "warehouse:inbound:edit"}, mode = SaMode.OR)
    @GetMapping("/warehouses")
    public R<List<InboundWarehouseOptionVo>> warehouses() {
        return R.ok(optionService.selectWarehouses());
    }

    @SaCheckPermission(value = {"warehouse:inbound:add", "warehouse:inbound:edit"}, mode = SaMode.OR)
    @GetMapping("/zones")
    public R<List<InboundZoneOptionVo>> zones(
        @NotNull(message = "仓库ID不能为空") @RequestParam Long warehouseId) {
        return R.ok(optionService.selectZones(warehouseId));
    }

    @SaCheckPermission(value = {"warehouse:inbound:add", "warehouse:inbound:edit"}, mode = SaMode.OR)
    @GetMapping("/locations")
    public R<List<InboundLocationOptionVo>> locations(
        @NotNull(message = "仓库ID不能为空") @RequestParam Long warehouseId,
        @RequestParam(required = false) Long zoneId) {
        return R.ok(optionService.selectLocations(warehouseId, zoneId));
    }

    @SaCheckPermission(value = {"warehouse:inbound:add", "warehouse:inbound:edit"}, mode = SaMode.OR)
    @GetMapping("/materials")
    public R<List<InboundMaterialOptionVo>> materials(
        @NotNull(message = "仓库ID不能为空") @RequestParam Long warehouseId) {
        return R.ok(optionService.selectMaterials(warehouseId));
    }
}
