package org.dromara.warehouse.material.category.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 物料分类实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_material_category")
public class MaterialCategory extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long companyId;
    private Long parentId;
    private String ancestors;
    private String categoryCode;
    private String categoryName;
    private Integer sort;
    private String status;
    private String remark;
    @TableLogic private String delFlag;
}
