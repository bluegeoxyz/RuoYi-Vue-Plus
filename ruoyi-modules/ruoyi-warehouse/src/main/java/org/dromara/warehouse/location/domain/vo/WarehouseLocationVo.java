package org.dromara.warehouse.location.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.warehouse.location.domain.WarehouseLocation;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 库位视图对象。 */
@Data
@AutoMapper(target = WarehouseLocation.class)
public class WarehouseLocationVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private Long zoneId;
    private String zoneName;
    private String locationCode;
    private String locationName;
    private String status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
