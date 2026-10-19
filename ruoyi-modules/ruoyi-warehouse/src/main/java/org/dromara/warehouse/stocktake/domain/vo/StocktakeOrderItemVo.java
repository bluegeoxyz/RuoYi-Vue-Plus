package org.dromara.warehouse.stocktake.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StocktakeOrderItemVo {
  private Long id;
  private Long materialId;
  private String materialCode;
  private String materialName;
  private String specification;
  private Long locationId;
  private String zoneName;
  private String locationCode;
  private String locationName;
  private Long unitId;
  private String unitName;
  private Integer precisionScale;
  private BigDecimal bookQuantity;
  private BigDecimal actualQuantity;
  private BigDecimal differenceQuantity;
  private String remark;
}
