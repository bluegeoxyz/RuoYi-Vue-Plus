package org.dromara.warehouse.transfer.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

/** 调拨明细展示对象。 */
@Data
public class TransferOrderItemVo {
  private Long id;
  private Long materialId;
  private String materialCode;
  private String materialName;
  private String specification;
  private Long sourceLocationId;
  private String sourceZoneName;
  private String sourceLocationCode;
  private String sourceLocationName;
  private Long targetLocationId;
  private String targetZoneName;
  private String targetLocationCode;
  private String targetLocationName;
  private Long unitId;
  private String unitName;
  private Integer precisionScale;
  private BigDecimal quantity;
  private String remark;
  private BigDecimal inventoryQuantity;
  private BigDecimal lockedQuantity;
  private BigDecimal availableQuantity;
}
