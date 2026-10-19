package org.dromara.property.resident.context.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.property.resident.context.domain.vo.ResidentBuildingOptionVo;
import org.dromara.property.resident.context.domain.vo.ResidentRoomOptionVo;
import org.dromara.property.resident.context.domain.vo.ResidentUnitOptionVo;
import org.dromara.property.resident.context.service.ResidentContextOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 住户只读页面固定上下文选项。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/resident/context/query")
public class ResidentContextOptionController {

    private final ResidentContextOptionService optionService;

    @SaCheckPermission(value = {"property:resident:list", "property:resident:query"}, mode = SaMode.OR)
    @GetMapping("/buildings")
    public R<List<ResidentBuildingOptionVo>> buildings(@RequestParam @NotNull Long projectId) {
        return R.ok(optionService.buildings(projectId));
    }

    @SaCheckPermission(value = {"property:resident:list", "property:resident:query"}, mode = SaMode.OR)
    @GetMapping("/units")
    public R<List<ResidentUnitOptionVo>> units(@RequestParam @NotNull Long projectId,
                                               @RequestParam @NotNull Long buildingId) {
        return R.ok(optionService.units(projectId, buildingId));
    }

    @SaCheckPermission(value = {"property:resident:list", "property:resident:query"}, mode = SaMode.OR)
    @GetMapping("/rooms")
    public R<List<ResidentRoomOptionVo>> rooms(@RequestParam @NotNull Long projectId,
                                               @RequestParam @NotNull Long buildingId,
                                               @RequestParam @NotNull Long unitId) {
        return R.ok(optionService.rooms(projectId, buildingId, unitId));
    }
}
