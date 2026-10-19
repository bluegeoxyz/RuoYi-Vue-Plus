package org.dromara.warehouse.location.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.warehouse.location.domain.WarehouseLocation;

import java.io.Serial;
import java.io.Serializable;

/** 库位选项。 */
@Data
@AutoMapper(target = WarehouseLocation.class)
public class WarehouseLocationOptionVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long warehouseId;
    private Long zoneId;
    private String locationCode;
    private String locationName;
}
