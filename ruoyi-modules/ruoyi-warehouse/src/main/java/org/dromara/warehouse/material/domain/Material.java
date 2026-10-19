package org.dromara.warehouse.material.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/** 物料主数据实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_material")
public class Material extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long companyId;
    private String materialCode;
    private String materialName;
    private Long categoryId;
    private Long baseUnitId;
    private String specification;
    private String model;
    private String barcode;
    private BigDecimal safetyStock;
    private String status;
    private Integer sort;
    private String remark;
    @TableLogic private String delFlag;
}
