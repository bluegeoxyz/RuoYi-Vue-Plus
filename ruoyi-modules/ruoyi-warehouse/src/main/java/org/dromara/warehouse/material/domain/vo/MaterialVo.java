package org.dromara.warehouse.material.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.warehouse.material.domain.Material;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 物料视图对象。 */
@Data
@AutoMapper(target = Material.class)
public class MaterialVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long companyId;
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "companyId") private String companyName;
    private String materialCode;
    private String materialName;
    private Long categoryId;
    private String categoryName;
    private Long baseUnitId;
    private String baseUnitName;
    private String baseUnitSymbol;
    private Integer precisionScale;
    private String specification;
    private String model;
    private String barcode;
    private BigDecimal safetyStock;
    private String status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
