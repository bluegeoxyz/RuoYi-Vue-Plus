package org.dromara.warehouse.material.category.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.warehouse.material.category.domain.MaterialCategory;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 物料分类树选项。 */
@Data
@AutoMapper(target = MaterialCategory.class)
public class MaterialCategoryOptionVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long companyId;
    private Long parentId;
    private String categoryCode;
    private String categoryName;
    private List<MaterialCategoryOptionVo> children;
}
