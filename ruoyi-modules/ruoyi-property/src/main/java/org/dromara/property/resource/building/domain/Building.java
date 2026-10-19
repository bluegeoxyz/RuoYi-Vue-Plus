package org.dromara.property.resource.building.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 物业楼栋。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_building")
public class Building extends BaseEntity {
    @TableId
    private Long id;
    private Long projectId;
    private String buildingCode;
    private String buildingName;
    private String buildingType;
    private Integer aboveGroundFloors;
    private Integer undergroundFloors;
    private Integer sort;
    private String status;
    @TableLogic
    private String delFlag;
}
