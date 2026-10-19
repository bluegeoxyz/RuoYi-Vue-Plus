package org.dromara.warehouse.zone.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.warehouse.zone.domain.WarehouseZone;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 库区视图对象。 */
@Data
@AutoMapper(target = WarehouseZone.class)
public class WarehouseZoneVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private String zoneCode;
    private String zoneName;
    private String status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
