package org.dromara.warehouse.warehouse.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.warehouse.warehouse.domain.Warehouse;

import java.io.Serial;
import java.io.Serializable;

/** 仓库选项。 */
@Data
@AutoMapper(target = Warehouse.class)
public class WarehouseOptionVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long companyId;
    private Long projectId;
    private String warehouseScope;
    private String warehouseCode;
    private String warehouseName;
}
