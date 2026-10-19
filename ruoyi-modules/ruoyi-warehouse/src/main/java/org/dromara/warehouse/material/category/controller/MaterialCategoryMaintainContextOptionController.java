package org.dromara.warehouse.material.category.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.warehouse.context.domain.vo.ContextCompanyOptionVo;
import org.dromara.warehouse.context.service.WarehouseMasterDataQueryOptionService;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 物料分类新增、编辑上下文选项。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse/materialCategory/context/maintain")
public class MaterialCategoryMaintainContextOptionController {

    private static final BusinessContext CONTEXT = BusinessContext.WAREHOUSE_MATERIAL_CATEGORY_MAINTAIN;

    private final WarehouseMasterDataQueryOptionService optionService;

    @SaCheckPermission(value = {"warehouse:materialCategory:add", "warehouse:materialCategory:edit"}, mode = SaMode.OR)
    @GetMapping("/companies")
    public R<List<ContextCompanyOptionVo>> companies() {
        return R.ok(optionService.selectCompanies(CONTEXT));
    }

    @SaCheckPermission(value = {"warehouse:materialCategory:add", "warehouse:materialCategory:edit"}, mode = SaMode.OR)
    @GetMapping("/categories")
    public R<List<MaterialCategoryOptionVo>> categories(@RequestParam(required = false) Long companyId) {
        return R.ok(optionService.selectMaterialCategories(CONTEXT, companyId));
    }
}
