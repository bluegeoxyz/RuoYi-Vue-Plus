package org.dromara.property.charge.receivable.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.property.charge.receivable.domain.bo.ReceivableGenerateBo;
import org.dromara.property.charge.receivable.domain.vo.ReceivableGenerationResultVo;
import org.dromara.property.charge.receivable.service.IReceivableGenerationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/receivable")
public class ReceivableGenerationController {
  private final IReceivableGenerationService service;

  @SaCheckPermission("property:receivable:generate")
  @Log(title = "应收生成", businessType = BusinessType.INSERT)
  @PostMapping("/generate")
  public R<ReceivableGenerationResultVo> generate(@Validated @RequestBody ReceivableGenerateBo bo) {
    return R.ok(service.generate(bo));
  }
}
