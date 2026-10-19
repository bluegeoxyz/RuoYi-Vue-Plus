package org.dromara.property.charge.item.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.property.charge.item.domain.bo.ChargeItemBo;
import org.dromara.property.charge.item.domain.vo.*;
import org.dromara.property.charge.item.service.IChargeItemService;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/charge/item")
public class ChargeItemController extends BaseController {
    private final IChargeItemService chargeItemService;

    @SaCheckPermission("property:chargeItem:list")
    @GetMapping("/list")
    public R<PageResult<ChargeItemVo>> list(ChargeItemBo bo, PageQuery pageQuery) {
        return R.ok(chargeItemService.queryPage(bo, pageQuery));
    }

    @SaCheckPermission("property:chargeItem:export")
    @Log(title = "收费项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ChargeItemBo bo, HttpServletResponse response) {
        ExcelBuilder.of(BeanUtil.copyToList(chargeItemService.queryExport(bo), ChargeItemExportVo.class), ChargeItemExportVo.class)
            .sheetName("收费项目").toResponse(response);
    }

    @SaCheckPermission("property:chargeItem:query")
    @GetMapping("/{id}")
    public R<ChargeItemVo> get(@PathVariable @NotNull Long id) { return R.ok(chargeItemService.queryById(id)); }

    @SaCheckPermission("property:chargeItem:add")
    @Log(title = "收费项目", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChargeItemBo bo) { return toAjax(chargeItemService.insert(bo)); }

    @SaCheckPermission("property:chargeItem:edit")
    @Log(title = "收费项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChargeItemBo bo) { return toAjax(chargeItemService.update(bo)); }

    @SaCheckPermission("property:chargeItem:edit")
    @Log(title = "收费项目状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody ChargeItemBo bo) { return toAjax(chargeItemService.changeStatus(bo.getId(), bo.getStatus())); }

    @SaCheckPermission("property:chargeItem:remove")
    @Log(title = "收费项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] ids) { return toAjax(chargeItemService.delete(List.of(ids))); }

    @SaCheckPermission("property:chargeItem:list")
    @GetMapping("/options")
    public R<List<ChargeItemOptionVo>> options(ChargeItemBo bo) { return R.ok(chargeItemService.options(bo)); }

    @SaCheckPermission("property:chargeItem:list")
    @GetMapping("/project-options")
    public R<List<ProjectOptionVo>> projectOptions() { return R.ok(chargeItemService.projectOptions()); }
}
