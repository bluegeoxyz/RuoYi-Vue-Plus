package org.dromara.warehouse.location.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 库位实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_warehouse_location")
public class WarehouseLocation extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long warehouseId;
    private Long zoneId;
    private String locationCode;
    private String locationName;
    private String status;
    private Integer sort;
    private String remark;
    @TableLogic private String delFlag;
}
