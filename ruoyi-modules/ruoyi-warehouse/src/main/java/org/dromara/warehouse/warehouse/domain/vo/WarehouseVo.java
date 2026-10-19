package org.dromara.warehouse.warehouse.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.warehouse.warehouse.domain.Warehouse;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 仓库视图对象。 */
@Data
@AutoMapper(target = Warehouse.class)
public class WarehouseVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private Long companyId;
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "companyId")
    private String companyName;
    private Long projectId;
    private String projectName;
    private String warehouseScope;
    private String warehouseCode;
    private String warehouseName;
    private Long managerId;
    @Translation(type = TransConstant.USER_ID_TO_NAME, mapper = "managerId")
    private String managerName;
    private String contactPhone;
    private String address;
    private String status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
