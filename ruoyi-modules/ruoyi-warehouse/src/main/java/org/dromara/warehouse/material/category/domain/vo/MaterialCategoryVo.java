package org.dromara.warehouse.material.category.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.warehouse.material.category.domain.MaterialCategory;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 物料分类视图对象。 */
@Data
@AutoMapper(target = MaterialCategory.class)
public class MaterialCategoryVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long companyId;
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "companyId") private String companyName;
    private Long parentId;
    private String ancestors;
    private String categoryCode;
    private String categoryName;
    private Integer sort;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<MaterialCategoryVo> children;
}
