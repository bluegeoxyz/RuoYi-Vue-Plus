package org.dromara.warehouse.inbound.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

/** 入库明细展示对象。 */
@Data
public class InboundOrderItemVo {
    private Long id;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String specification;
    private Long locationId;
    private String zoneName;
    private String locationCode;
    private String locationName;
    private BigDecimal quantity;
    private Long unitId;
    private String unitName;
    private Integer precisionScale;
    private String remark;
}
