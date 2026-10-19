package org.dromara.property.resource.room.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_room")
public class Room extends BaseEntity {
    @TableId
    private Long id;
    private Long projectId;
    private Long buildingId;
    private Long unitId;
    private String roomCode;
    private String roomNo;
    private Integer floorNo;
    private String roomType;
    private String propertyType;
    private BigDecimal buildingArea;
    private BigDecimal usableArea;
    private String deliveryStatus;
    private String occupancyStatus;
    private Integer sort;
    private String status;
    @TableLogic
    private String delFlag;
}
