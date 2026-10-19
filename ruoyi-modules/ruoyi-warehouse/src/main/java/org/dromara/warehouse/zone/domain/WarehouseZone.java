package org.dromara.warehouse.zone.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 库区实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_warehouse_zone")
public class WarehouseZone extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long warehouseId;
    private String zoneCode;
    private String zoneName;
    private String status;
    private Integer sort;
    private String remark;
    @TableLogic private String delFlag;
}
