package org.dromara.warehouse.inventory.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.warehouse.inventory.domain.bo.InventoryBo;
import org.dromara.warehouse.inventory.domain.vo.InventorySummaryVo;
import org.dromara.warehouse.inventory.domain.vo.InventoryVo;
import org.dromara.warehouse.inventory.service.IInventoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 实时库存只读接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/warehouse/inventory")
public class InventoryController extends BaseController {
    private final IInventoryService inventoryService;

    @SaCheckPermission("warehouse:inventory:list")
    @GetMapping("/list")
    public R<PageResult<InventoryVo>> list(InventoryBo bo, PageQuery pageQuery) {
        return R.ok(inventoryService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("warehouse:inventory:list")
    @GetMapping("/summary")
    public R<PageResult<InventorySummaryVo>> summary(InventoryBo bo, PageQuery pageQuery) {
        return R.ok(inventoryService.querySummaryPage(bo, pageQuery));
    }

    @SaCheckPermission("warehouse:inventory:export")
    @Log(title = "实时库存", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InventoryBo bo, HttpServletResponse response) {
        ExcelBuilder.of(inventoryService.queryList(bo), InventoryVo.class).sheetName("实时库存").toResponse(response);
    }

    @SaCheckPermission("warehouse:inventory:query")
    @GetMapping("/{id}")
    public R<InventoryVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(inventoryService.queryById(id));
    }
}
