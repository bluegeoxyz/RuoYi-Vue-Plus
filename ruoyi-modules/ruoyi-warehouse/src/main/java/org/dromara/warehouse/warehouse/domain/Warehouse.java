package org.dromara.warehouse.warehouse.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 仓库实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_warehouse")
public class Warehouse extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long companyId;
    private Long projectId;
    private String warehouseScope;
    private String warehouseCode;
    private String warehouseName;
    private Long managerId;
    private String contactPhone;
    private String address;
    private String status;
    private Integer sort;
    private String remark;
    @TableLogic private String delFlag;
}
