package org.dromara.warehouse.inventory.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/** 库位级库存余额实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_inventory")
public class Inventory extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private Long warehouseId;
    private Long locationId;
    private Long materialId;
    private BigDecimal quantity;
    private BigDecimal lockedQuantity;
    @Version private Integer version;
}
