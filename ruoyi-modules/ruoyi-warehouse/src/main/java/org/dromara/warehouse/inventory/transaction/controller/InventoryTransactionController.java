package org.dromara.warehouse.inventory.transaction.controller;

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
import org.dromara.warehouse.inventory.transaction.domain.bo.InventoryTransactionBo;
import org.dromara.warehouse.inventory.transaction.domain.vo.InventoryTransactionVo;
import org.dromara.warehouse.inventory.transaction.service.IInventoryTransactionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 库存流水只读接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/warehouse/inventoryTransaction")
public class InventoryTransactionController extends BaseController {
    private final IInventoryTransactionService transactionService;

    @SaCheckPermission("warehouse:inventoryTransaction:list")
    @GetMapping("/list")
    public R<PageResult<InventoryTransactionVo>> list(InventoryTransactionBo bo, PageQuery pageQuery) {
        return R.ok(transactionService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("warehouse:inventoryTransaction:export")
    @Log(title = "库存流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InventoryTransactionBo bo, HttpServletResponse response) {
        ExcelBuilder.of(transactionService.queryList(bo), InventoryTransactionVo.class).sheetName("库存流水").toResponse(response);
    }

    @SaCheckPermission("warehouse:inventoryTransaction:query")
    @GetMapping("/{id}")
    public R<InventoryTransactionVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(transactionService.queryById(id));
    }
}
