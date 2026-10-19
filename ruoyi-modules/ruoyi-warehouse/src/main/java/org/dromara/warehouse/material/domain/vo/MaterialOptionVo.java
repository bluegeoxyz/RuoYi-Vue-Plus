package org.dromara.warehouse.material.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.warehouse.material.domain.Material;

import java.io.Serial;
import java.io.Serializable;

/** 启用物料选项。 */
@Data
@AutoMapper(target = Material.class)
public class MaterialOptionVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long companyId;
    private String materialCode;
    private String materialName;
    private Long categoryId;
    private Long baseUnitId;
    private String specification;
    private String model;
}
