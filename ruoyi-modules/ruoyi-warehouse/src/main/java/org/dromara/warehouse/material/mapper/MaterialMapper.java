package org.dromara.warehouse.material.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.domain.vo.MaterialOptionVo;
import org.dromara.warehouse.material.domain.vo.MaterialVo;

import java.util.Collection;
import java.util.List;

/** 物料数据层。 */
public interface MaterialMapper extends BaseMapperPlus<Material, MaterialVo> {
    default long countByCode(Long companyId, String code, Long excludeId) {
        return lambda().eq(Material::getCompanyId, companyId).eq(Material::getMaterialCode, code)
            .ne(excludeId != null, Material::getId, excludeId).count();
    }

    default long countByBarcode(Long companyId, String barcode, Long excludeId) {
        return lambda().eq(Material::getCompanyId, companyId).eq(Material::getBarcode, barcode)
            .ne(excludeId != null, Material::getId, excludeId).count();
    }

    default boolean existsByCategoryIds(Collection<Long> categoryIds) {
        return !categoryIds.isEmpty() && lambda().in(Material::getCategoryId, categoryIds).exists();
    }

    default boolean existsByUnitIds(Collection<Long> unitIds) {
        return !unitIds.isEmpty() && lambda().in(Material::getBaseUnitId, unitIds).exists();
    }

    default List<MaterialOptionVo> selectOptions(Collection<Long> companyIds, Long companyId) {
        return selectVoList(lambda().in(Material::getCompanyId, companyIds)
            .eq(companyId != null, Material::getCompanyId, companyId).eq(Material::getStatus, "0")
            .orderByAsc(Material::getSort).orderByAsc(Material::getId).build(), MaterialOptionVo.class);
    }
}
