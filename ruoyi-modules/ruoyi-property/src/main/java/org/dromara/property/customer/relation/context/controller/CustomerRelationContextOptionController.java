package org.dromara.property.customer.relation.context.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.property.customer.relation.context.domain.vo.*;
import org.dromara.property.customer.relation.context.service.CustomerRelationContextOptionService;
import org.dromara.property.customer.relation.domain.vo.CustomerReferenceOptionVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 客户关系业务固定上下文选项；不接收客户端上下文或权限字符串。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/customer-relation/context")
public class CustomerRelationContextOptionController {
    private final CustomerRelationContextOptionService optionService;

    @SaCheckPermission("property:customerRelation:list")
    @GetMapping("/list/projects")
    public R<List<CustomerRelationProjectOptionVo>> listProjects() {
        return R.ok(optionService.projects(BusinessContext.CUSTOMER_RELATION_LIST));
    }

    @SaCheckPermission("property:customerRelation:list")
    @GetMapping("/list/buildings")
    public R<List<CustomerRelationBuildingOptionVo>> listBuildings(@RequestParam @NotNull Long projectId) {
        return R.ok(optionService.buildings(BusinessContext.CUSTOMER_RELATION_LIST, projectId));
    }

    @SaCheckPermission("property:customerRelation:list")
    @GetMapping("/list/units")
    public R<List<CustomerRelationUnitOptionVo>> listUnits(@RequestParam @NotNull Long projectId,
                                                           @RequestParam @NotNull Long buildingId) {
        return R.ok(optionService.units(BusinessContext.CUSTOMER_RELATION_LIST, projectId, buildingId));
    }

    @SaCheckPermission("property:customerRelation:list")
    @GetMapping("/list/rooms")
    public R<List<CustomerRelationRoomOptionVo>> listRooms(@RequestParam @NotNull Long projectId,
                                                           @RequestParam @NotNull Long buildingId,
                                                           @RequestParam @NotNull Long unitId) {
        return R.ok(optionService.rooms(BusinessContext.CUSTOMER_RELATION_LIST, projectId, buildingId, unitId));
    }

    @SaCheckPermission("property:customerRelation:add")
    @GetMapping("/create/projects")
    public R<List<CustomerRelationProjectOptionVo>> createProjects() {
        return R.ok(optionService.projects(BusinessContext.CUSTOMER_RELATION_CREATE));
    }

    @SaCheckPermission("property:customerRelation:add")
    @GetMapping("/create/buildings")
    public R<List<CustomerRelationBuildingOptionVo>> createBuildings(@RequestParam @NotNull Long projectId) {
        return R.ok(optionService.buildings(BusinessContext.CUSTOMER_RELATION_CREATE, projectId));
    }

    @SaCheckPermission("property:customerRelation:add")
    @GetMapping("/create/units")
    public R<List<CustomerRelationUnitOptionVo>> createUnits(@RequestParam @NotNull Long projectId,
                                                             @RequestParam @NotNull Long buildingId) {
        return R.ok(optionService.units(BusinessContext.CUSTOMER_RELATION_CREATE, projectId, buildingId));
    }

    @SaCheckPermission("property:customerRelation:add")
    @GetMapping("/create/rooms")
    public R<List<CustomerRelationRoomOptionVo>> createRooms(@RequestParam @NotNull Long projectId,
                                                             @RequestParam @NotNull Long buildingId,
                                                             @RequestParam @NotNull Long unitId) {
        return R.ok(optionService.rooms(BusinessContext.CUSTOMER_RELATION_CREATE, projectId, buildingId, unitId));
    }

    @SaCheckPermission("property:customerRelation:add")
    @GetMapping("/create/customers")
    public R<List<CustomerReferenceOptionVo>> createCustomers(@RequestParam @NotNull Long projectId,
                                                               @RequestParam String keyword) {
        return R.ok(optionService.customers(BusinessContext.CUSTOMER_RELATION_CREATE, projectId, keyword));
    }
}
