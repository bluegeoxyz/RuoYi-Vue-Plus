package org.dromara.property.charge.receivable.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.*;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.property.charge.receivable.domain.bo.*;
import org.dromara.property.charge.receivable.domain.vo.*;
import org.dromara.property.charge.receivable.service.IReceivableService;
import org.dromara.property.charge.receivable.service.ReceivablePaymentHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/receivable")
public class ReceivableController extends BaseController {
  private final IReceivableService service;
  private final ReceivablePaymentHistoryService paymentHistoryService;

  @SaCheckPermission("property:receivable:list")
  @GetMapping("/list")
  public R<PageResult<ReceivableVo>> list(ReceivableQueryBo bo, PageQuery pq) {
    return R.ok(service.queryPage(bo, pq));
  }

  @SaCheckPermission("property:receivable:list")
  @GetMapping("/summary")
  public R<ReceivableSummaryVo> summary(ReceivableQueryBo bo) {
    return R.ok(service.summary(bo));
  }

  @SaCheckPermission("property:receivable:list")
  @GetMapping("/batch/list")
  public R<PageResult<ReceivableBatchVo>> batchList(ReceivableBatchQueryBo bo, PageQuery pq) {
    return R.ok(service.queryBatchPage(bo, pq));
  }

  @SaCheckPermission("property:receivable:list")
  @GetMapping("/batch/{id}")
  public R<ReceivableBatchVo> batch(@PathVariable @NotNull Long id) {
    return R.ok(service.queryBatchById(id));
  }

  @SaCheckPermission("property:receivable:export")
  @Log(title = "应收管理", businessType = BusinessType.EXPORT)
  @PostMapping("/export")
  public void export(ReceivableQueryBo bo, HttpServletResponse response) {
    ExcelBuilder.of(service.export(bo), ReceivableExportVo.class)
        .sheetName("应收管理")
        .toResponse(response);
  }

  @SaCheckPermission("property:receivable:query")
  @GetMapping("/{id}")
  public R<ReceivableVo> detail(@PathVariable @NotNull Long id) {
    return R.ok(service.queryById(id));
  }

  @SaCheckPermission("property:receivable:query")
  @GetMapping("/{id}/payments")
  public R<List<ReceivablePaymentVo>> payments(@PathVariable @NotNull Long id) {
    return R.ok(paymentHistoryService.query(id));
  }

  @SaCheckPermission("property:receivable:cancel")
  @Log(title = "应收作废", businessType = BusinessType.UPDATE)
  @PutMapping("/{id}/cancel")
  public R<Void> cancel(@PathVariable @NotNull Long id, @Valid @RequestBody ReceivableCancelBo bo) {
    return toAjax(service.cancel(id, bo));
  }
}
