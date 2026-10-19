package org.dromara.property.resource.room.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.*;
import org.dromara.property.resource.room.domain.Room;

import java.math.BigDecimal;

@Data
@AutoMapper(target = Room.class, reverseConvertGenerate = false)
public class RoomBo {
    @NotNull(groups = EditGroup.class)
    private Long id;
    @NotNull(groups = AddGroup.class)
    private Long projectId;
    @NotNull(groups = AddGroup.class)
    private Long buildingId;
    @NotNull(groups = AddGroup.class)
    private Long unitId;
    @NotBlank(groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64)
    private String roomCode;
    @NotBlank(groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32)
    private String roomNo;
    private Integer floorNo;
    private String roomType;
    private String propertyType;
    @DecimalMin("0")
    private BigDecimal buildingArea;
    @DecimalMin("0")
    private BigDecimal usableArea;
    private String deliveryStatus;
    private String occupancyStatus;
    private Integer sort;
    private String status;
    @Size(max = 500)
    private String remark;
}
