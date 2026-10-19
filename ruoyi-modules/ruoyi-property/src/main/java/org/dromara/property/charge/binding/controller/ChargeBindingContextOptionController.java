package org.dromara.property.charge.binding.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.property.charge.context.domain.vo.*;
import org.dromara.property.charge.context.service.FinanceContextOptionService;
import org.dromara.property.charge.item.domain.vo.ChargeItemOptionVo;
import org.dromara.property.charge.rule.domain.vo.ChargeRuleOptionVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated @RestController @RequiredArgsConstructor
@RequestMapping("/property/charge/binding/context")
public class ChargeBindingContextOptionController {
    private final FinanceContextOptionService optionService;

    @SaCheckPermission("property:chargeBinding:list")
    @GetMapping("/list/projects")
    public R<List<ChargeBindingProjectOptionVo>> listProjects() {
        return R.ok(optionService.chargeBindingProjects());
    }

    @SaCheckPermission("property:chargeBinding:list")
    @GetMapping("/list/buildings")
    public R<List<ChargeBindingBuildingOptionVo>> listBuildings(@RequestParam @NotNull Long projectId) {
        return R.ok(optionService.chargeBindingBuildings(projectId));
    }

    @SaCheckPermission("property:chargeBinding:list")
    @GetMapping("/list/units")
    public R<List<ChargeBindingUnitOptionVo>> listUnits(@RequestParam @NotNull Long projectId,
                                                        @RequestParam @NotNull Long buildingId) {
        return R.ok(optionService.chargeBindingUnits(projectId, buildingId));
    }

    @SaCheckPermission("property:chargeBinding:list")
    @GetMapping("/list/rooms")
    public R<List<ChargeBindingRoomOptionVo>> listRooms(@RequestParam @NotNull Long projectId,
                                                        @RequestParam @NotNull Long buildingId,
                                                        @RequestParam @NotNull Long unitId) {
        return R.ok(optionService.chargeBindingRooms(projectId, buildingId, unitId));
    }

    @SaCheckPermission("property:chargeBinding:add")
    @GetMapping("/create/items")
    public R<List<ChargeItemOptionVo>> createItems(@RequestParam @NotNull Long projectId) {
        return R.ok(optionService.chargeItems(BusinessContext.CHARGE_BINDING_CREATE, projectId, "ROOM"));
    }

    @SaCheckPermission("property:chargeBinding:add")
    @GetMapping("/create/rules")
    public R<List<ChargeRuleOptionVo>> createRules(@RequestParam @NotNull Long projectId,
                                                   @RequestParam @NotNull Long chargeItemId) {
        return R.ok(optionService.chargeRules(BusinessContext.CHARGE_BINDING_CREATE, projectId, chargeItemId));
    }

    @SaCheckPermission("property:chargeBinding:edit")
    @GetMapping("/edit/items")
    public R<List<ChargeItemOptionVo>> editItems(@RequestParam @NotNull Long projectId) {
        return R.ok(optionService.chargeItems(BusinessContext.CHARGE_BINDING_EDIT, projectId, "ROOM"));
    }

    @SaCheckPermission("property:chargeBinding:edit")
    @GetMapping("/edit/rules")
    public R<List<ChargeRuleOptionVo>> editRules(@RequestParam @NotNull Long projectId,
                                                 @RequestParam @NotNull Long chargeItemId) {
        return R.ok(optionService.chargeRules(BusinessContext.CHARGE_BINDING_EDIT, projectId, chargeItemId));
    }
}
