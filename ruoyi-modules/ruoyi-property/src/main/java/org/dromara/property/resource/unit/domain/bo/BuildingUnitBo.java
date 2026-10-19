package org.dromara.property.resource.unit.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.property.resource.unit.domain.BuildingUnit;

/**
 * 楼栋单元业务对象。
 */
@Data
@AutoMapper(target = BuildingUnit.class, reverseConvertGenerate = false)
public class BuildingUnitBo {
    @NotNull(groups = EditGroup.class)
    private Long id;
    @NotNull(groups = AddGroup.class)
    private Long projectId;
    @NotNull(groups = AddGroup.class)
    private Long buildingId;
    @NotBlank(groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32)
    private String unitCode;
    @NotBlank(groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64)
    private String unitName;
    @Min(0)
    private Integer floorCount;
    private Integer sort;
    private String status;
    @Size(max = 500)
    private String remark;
}
