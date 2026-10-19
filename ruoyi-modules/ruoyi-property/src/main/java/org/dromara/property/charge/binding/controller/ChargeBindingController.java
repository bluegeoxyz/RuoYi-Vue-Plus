package org.dromara.property.charge.binding.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.*;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.property.charge.binding.domain.bo.*;
import org.dromara.property.charge.binding.domain.vo.*;
import org.dromara.property.charge.binding.service.IChargeBindingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/charge/binding")
public class ChargeBindingController extends BaseController {
  private final IChargeBindingService service;

  @SaCheckPermission("property:chargeBinding:list")
  @GetMapping("/list")
  public R<List<ChargeBindingVo>> list(@RequestParam @NotNull Long roomId) {
    return R.ok(service.queryRoomBindings(roomId));
  }

  @SaCheckPermission("property:chargeBinding:query")
  @GetMapping("/{id}")
  public R<ChargeBindingVo> get(@PathVariable @NotNull Long id) {
    return R.ok(service.queryById(id));
  }

  @SaCheckPermission("property:chargeBinding:add")
  @Log(title = "房屋收费设置", businessType = BusinessType.INSERT)
  @PostMapping
  public R<Void> add(@Validated(AddGroup.class) @RequestBody ChargeBindingBo bo) {
    return toAjax(service.insert(bo));
  }

  @SaCheckPermission("property:chargeBinding:add")
  @Log(title = "批量房屋收费设置", businessType = BusinessType.INSERT)
  @PostMapping("/batch")
  public R<ChargeBindingBatchResultVo> batch(@Validated @RequestBody ChargeBindingBatchBo bo) {
    return R.ok(service.batchCreate(bo));
  }

  @SaCheckPermission("property:chargeBinding:edit")
  @Log(title = "房屋收费设置", businessType = BusinessType.UPDATE)
  @PutMapping
  public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChargeBindingBo bo) {
    return toAjax(service.update(bo));
  }

  @SaCheckPermission("property:chargeBinding:edit")
  @PutMapping("/changeStatus")
  public R<Void> status(@RequestBody ChargeBindingBo bo) {
    return toAjax(service.changeStatus(bo.getId(), bo.getStatus(), bo.getEffectiveEndDate()));
  }

  @SaCheckPermission("property:chargeBinding:remove")
  @Log(title = "房屋收费设置", businessType = BusinessType.DELETE)
  @DeleteMapping("/{ids}")
  public R<Void> remove(@PathVariable @NotEmpty Long[] ids) {
    return toAjax(service.delete(List.of(ids)));
  }
}
