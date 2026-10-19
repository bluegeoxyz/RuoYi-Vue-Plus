package org.dromara.warehouse.outbound.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

/** 出库明细展示对象。 */
@Data
public class OutboundOrderItemVo {
    private Long id; private Long materialId; private String materialCode; private String materialName; private String specification;
    private Long locationId; private String zoneName; private String locationCode; private String locationName;
    private Long unitId; private String unitName; private Integer precisionScale; private BigDecimal quantity; private String remark;
    private BigDecimal inventoryQuantity; private BigDecimal lockedQuantity; private BigDecimal availableQuantity;
}
