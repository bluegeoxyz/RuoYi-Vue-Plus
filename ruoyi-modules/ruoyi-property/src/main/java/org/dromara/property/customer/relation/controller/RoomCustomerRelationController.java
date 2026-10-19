package org.dromara.property.customer.relation.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.*;
import org.dromara.common.core.validate.*;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.property.customer.relation.domain.bo.*;
import org.dromara.property.customer.relation.domain.vo.*;
import org.dromara.property.customer.relation.service.IRoomCustomerRelationService;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/customer-relation")
public class RoomCustomerRelationController extends BaseController {
    private final IRoomCustomerRelationService service;

    @SaCheckPermission("property:customerRelation:list")
    @GetMapping("/list")
    public R<PageResult<RoomCustomerRelationVo>> list(RoomCustomerRelationBo bo, PageQuery pageQuery) {
        return R.ok(service.queryPage(bo, pageQuery));
    }

    @SaCheckPermission("property:customerRelation:export")
    @Log(title = "房屋客户关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(RoomCustomerRelationBo bo, HttpServletResponse response) {
        ExcelBuilder.of(service.queryExport(bo), RoomCustomerRelationVo.class).sheetName("房屋客户关系").toResponse(response);
    }

    @SaCheckPermission("property:customerRelation:query")
    @GetMapping("/{id}")
    public R<RoomCustomerRelationVo> get(@PathVariable @NotNull Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("property:customerRelation:add")
    @Log(title = "房屋客户关系", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody RoomCustomerRelationBo bo) {
        return toAjax(service.insert(bo));
    }

    @SaCheckPermission("property:customerRelation:edit")
    @Log(title = "房屋客户关系", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody RoomCustomerRelationBo bo) {
        return toAjax(service.update(bo));
    }

    @SaCheckPermission("property:customerRelation:moveIn")
    @Log(title = "客户入住", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/move-in")
    public R<Void> moveIn(@Validated @RequestBody RoomCustomerMoveInBo bo) {
        return toAjax(service.moveIn(bo));
    }

    @SaCheckPermission("property:customerRelation:moveOut")
    @Log(title = "客户迁出", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/move-out")
    public R<Void> moveOut(@Validated @RequestBody RoomCustomerMoveOutBo bo) {
        return toAjax(service.moveOut(bo));
    }

    @SaCheckPermission("property:customerRelation:remove")
    @Log(title = "房屋客户关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] ids) {
        return toAjax(service.delete(List.of(ids)));
    }

    @SaCheckPermission("property:customerRelation:list")
    @GetMapping("/options")
    public R<List<RoomCustomerRelationVo>> options(RoomCustomerRelationBo bo) {
        return R.ok(service.options(bo));
    }

    @SaCheckPermission("property:customerRelation:add")
    @GetMapping("/customer-options")
    public R<List<CustomerReferenceOptionVo>> customerOptions(@RequestParam @NotNull Long projectId,
                                                               @RequestParam String keyword) {
        return R.ok(service.customerOptions(projectId, keyword));
    }

    @SaCheckPermission("property:customerRelation:list")
    @GetMapping("/project-options")
    public R<List<ProjectOptionVo>> projectOptions() {
        return R.ok(service.projectOptions());
    }
}
