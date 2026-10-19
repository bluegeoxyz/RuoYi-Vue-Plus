package org.dromara.warehouse.material.category.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.ContextCompanyOptionVo;
import org.dromara.warehouse.context.service.WarehouseMasterDataQueryOptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 物料分类只读查询上下文选项。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/materialCategory/context/query")
public class MaterialCategoryQueryContextOptionController {

    private static final BusinessContext CONTEXT = BusinessContext.WAREHOUSE_MATERIAL_CATEGORY_QUERY;

    private final WarehouseMasterDataQueryOptionService optionService;

    @SaCheckPermission(value = {"warehouse:materialCategory:list", "warehouse:materialCategory:query"}, mode = SaMode.OR)
    @GetMapping("/companies")
    public R<List<ContextCompanyOptionVo>> companies() {
        return R.ok(optionService.selectCompanies(CONTEXT));
    }
}
