package org.dromara.warehouse.material.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.ContextCompanyOptionVo;
import org.dromara.warehouse.context.service.WarehouseMasterDataQueryOptionService;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 物料只读查询上下文选项。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/material/context/query")
public class MaterialQueryContextOptionController {

    private static final BusinessContext CONTEXT = BusinessContext.WAREHOUSE_MATERIAL_QUERY;

    private final WarehouseMasterDataQueryOptionService optionService;

    @SaCheckPermission(value = {"warehouse:material:list", "warehouse:material:query"}, mode = SaMode.OR)
    @GetMapping("/companies")
    public R<List<ContextCompanyOptionVo>> companies() {
        return R.ok(optionService.selectCompanies(CONTEXT));
    }

    @SaCheckPermission(value = {"warehouse:material:list", "warehouse:material:query"}, mode = SaMode.OR)
    @GetMapping("/categories")
    public R<List<MaterialCategoryOptionVo>> categories(@RequestParam(required = false) Long companyId) {
        return R.ok(optionService.selectMaterialCategories(CONTEXT, companyId));
    }

    @SaCheckPermission(value = {"warehouse:material:list", "warehouse:material:query"}, mode = SaMode.OR)
    @GetMapping("/units")
    public R<List<UnitOptionVo>> units() {
        return R.ok(optionService.selectUnits(CONTEXT));
    }
}
