package org.dromara.property.resource.unit.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 物业楼栋单元。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_unit")
public class BuildingUnit extends BaseEntity {
    @TableId
    private Long id;
    private Long projectId;
    private Long buildingId;
    private String unitCode;
    private String unitName;
    private Integer floorCount;
    private Integer sort;
    private String status;
    @TableLogic
    private String delFlag;
}
