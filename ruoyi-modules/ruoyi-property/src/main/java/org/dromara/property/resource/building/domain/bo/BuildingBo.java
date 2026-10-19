package org.dromara.property.resource.building.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.property.resource.building.domain.Building;

/**
 * 楼栋业务对象。
 */
@Data
@AutoMapper(target = Building.class, reverseConvertGenerate = false)
public class BuildingBo {
    @NotNull(groups = EditGroup.class)
    private Long id;
    @NotNull(groups = AddGroup.class)
    private Long projectId;
    @NotBlank(groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32)
    private String buildingCode;
    @NotBlank(groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64)
    private String buildingName;
    private String buildingType;
    private Integer aboveGroundFloors;
    private Integer undergroundFloors;
    private Integer sort;
    private String status;
    @Size(max = 500)
    private String remark;
}
