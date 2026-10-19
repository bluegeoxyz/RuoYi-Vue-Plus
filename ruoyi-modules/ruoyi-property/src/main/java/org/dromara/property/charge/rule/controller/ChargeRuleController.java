package org.dromara.property.charge.rule.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.*;
import org.dromara.common.core.validate.*;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.property.charge.rule.domain.bo.ChargeRuleBo;
import org.dromara.property.charge.rule.domain.vo.*;
import org.dromara.property.charge.rule.service.IChargeRuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/charge/rule")
public class ChargeRuleController extends BaseController {
  private final IChargeRuleService service;

  @SaCheckPermission("property:chargeRule:list")
  @GetMapping("/list")
  public R<PageResult<ChargeRuleVo>> list(ChargeRuleBo bo, PageQuery pq) {
    return R.ok(service.queryPage(bo, pq));
  }

  @SaCheckPermission("property:chargeRule:export")
  @Log(title = "计费规则", businessType = BusinessType.EXPORT)
  @PostMapping("/export")
  public void export(ChargeRuleBo bo, HttpServletResponse response) {
    ExcelBuilder.of(service.queryExport(bo), ChargeRuleExportVo.class)
        .sheetName("计费规则")
        .toResponse(response);
  }

  @SaCheckPermission("property:chargeRule:query")
  @GetMapping("/{id}")
  public R<ChargeRuleVo> get(@PathVariable @NotNull Long id) {
    return R.ok(service.queryById(id));
  }

  @SaCheckPermission("property:chargeRule:add")
  @Log(title = "计费规则", businessType = BusinessType.INSERT)
  @PostMapping
  public R<Void> add(@Validated(AddGroup.class) @RequestBody ChargeRuleBo bo) {
    return toAjax(service.insert(bo));
  }

  @SaCheckPermission("property:chargeRule:edit")
  @Log(title = "计费规则", businessType = BusinessType.UPDATE)
  @PutMapping
  public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChargeRuleBo bo) {
    return toAjax(service.update(bo));
  }

  @SaCheckPermission("property:chargeRule:edit")
  @PutMapping("/changeStatus")
  public R<Void> status(@RequestBody ChargeRuleBo bo) {
    return toAjax(service.changeStatus(bo.getId(), bo.getStatus()));
  }

  @SaCheckPermission("property:chargeRule:remove")
  @Log(title = "计费规则", businessType = BusinessType.DELETE)
  @DeleteMapping("/{ids}")
  public R<Void> remove(@PathVariable @NotEmpty Long[] ids) {
    return toAjax(service.delete(List.of(ids)));
  }

  @SaCheckPermission("property:chargeRule:list")
  @GetMapping("/options")
  public R<List<ChargeRuleOptionVo>> options(ChargeRuleBo bo) {
    return R.ok(service.options(bo));
  }
}
