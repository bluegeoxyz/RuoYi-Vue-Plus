package org.dromara.warehouse.material.category.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.material.category.domain.MaterialCategory;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryVo;

import java.util.Collection;
import java.util.List;

/** 物料分类数据层。 */
public interface MaterialCategoryMapper extends BaseMapperPlus<MaterialCategory, MaterialCategoryVo> {
    default long countByCode(Long companyId, String code, Long excludeId) {
        return lambda().eq(MaterialCategory::getCompanyId, companyId).eq(MaterialCategory::getCategoryCode, code)
            .ne(excludeId != null, MaterialCategory::getId, excludeId).count();
    }

    default boolean existsChild(Collection<Long> ids) {
        return !ids.isEmpty() && lambda().in(MaterialCategory::getParentId, ids).exists();
    }

    default List<MaterialCategoryOptionVo> selectOptions(Collection<Long> companyIds, Long companyId) {
        return selectVoList(lambda().in(MaterialCategory::getCompanyId, companyIds)
            .eq(companyId != null, MaterialCategory::getCompanyId, companyId)
            .eq(MaterialCategory::getStatus, "0")
            .orderByAsc(MaterialCategory::getAncestors).orderByAsc(MaterialCategory::getSort)
            .orderByAsc(MaterialCategory::getId).build(), MaterialCategoryOptionVo.class);
    }
}
