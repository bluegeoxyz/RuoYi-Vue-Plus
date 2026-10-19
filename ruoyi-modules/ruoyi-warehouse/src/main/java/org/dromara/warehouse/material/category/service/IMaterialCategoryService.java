package org.dromara.warehouse.material.category.service;

import org.dromara.warehouse.material.category.domain.bo.MaterialCategoryBo;
import org.dromara.warehouse.material.category.domain.MaterialCategory;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryVo;

import java.util.Collection;
import java.util.List;

/** 物料分类服务。 */
public interface IMaterialCategoryService {
    MaterialCategoryVo queryById(Long id);
    List<MaterialCategoryVo> queryTree(MaterialCategoryBo bo);
    Boolean insertByBo(MaterialCategoryBo bo);
    Boolean updateByBo(MaterialCategoryBo bo);
    Boolean changeStatus(Long id, String status);
    Boolean deleteWithValidByIds(Collection<Long> ids);
    List<MaterialCategoryOptionVo> selectOptions(Long companyId);
    MaterialCategory requireEnabledCategory(Long id, Long companyId);
}
