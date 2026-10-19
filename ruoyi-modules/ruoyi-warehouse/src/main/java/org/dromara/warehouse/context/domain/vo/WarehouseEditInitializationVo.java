package org.dromara.warehouse.context.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 仓库模块编辑初始化专用视图。 */
public final class WarehouseEditInitializationVo {

    private WarehouseEditInitializationVo() {
    }

    @Data
    public static class WarehouseEdit {
        private Long id;
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
    }

    @Data
    public static class ZoneEdit {
        private Long id;
        private Long warehouseId;
        private String zoneCode;
        private String zoneName;
        private String status;
        private Integer sort;
        private String remark;
    }

    @Data
    public static class LocationEdit {
        private Long id;
        private Long warehouseId;
        private Long zoneId;
        private String locationCode;
        private String locationName;
        private String status;
        private Integer sort;
        private String remark;
    }

    @Data
    public static class MaterialCategoryEdit {
        private Long id;
        private Long companyId;
        private Long parentId;
        private String categoryCode;
        private String categoryName;
        private Integer sort;
        private String status;
        private String remark;
    }

    @Data
    public static class UnitEdit {
        private Long id;
        private String unitCode;
        private String unitName;
        private String symbol;
        private Integer precisionScale;
        private String status;
        private Integer sort;
        private String remark;
    }

    @Data
    public static class MaterialEdit {
        private Long id;
        private Long companyId;
        private String materialCode;
        private String materialName;
        private Long categoryId;
        private Long baseUnitId;
        private String specification;
        private String model;
        private String barcode;
        private BigDecimal safetyStock;
        private String status;
        private Integer sort;
        private String remark;
    }

    @Data
    public static class InboundEdit {
        private Long id;
        private Long warehouseId;
        private String inboundType;
        private LocalDateTime inboundAt;
        private String remark;
        private List<InboundItemEdit> items;
    }

    @Data
    public static class InboundItemEdit {
        private Long id;
        private Long materialId;
        private Long locationId;
        private BigDecimal quantity;
        private Long unitId;
        private String unitName;
        private Integer precisionScale;
        private String remark;
    }

    @Data
    public static class OutboundEdit {
        private Long id;
        private Long warehouseId;
        private String outboundType;
        private LocalDateTime outboundAt;
        private String remark;
        private List<OutboundItemEdit> items;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class OutboundItemEdit extends InboundItemEdit {
        private BigDecimal inventoryQuantity;
        private BigDecimal lockedQuantity;
        private BigDecimal availableQuantity;
    }

    @Data
    public static class TransferEdit {
        private Long id;
        private Long sourceWarehouseId;
        private Long targetWarehouseId;
        private String transferType;
        private LocalDateTime transferAt;
        private String remark;
        private List<TransferItemEdit> items;
    }

    @Data
    public static class TransferItemEdit {
        private Long id;
        private Long materialId;
        private Long sourceLocationId;
        private Long targetLocationId;
        private Long unitId;
        private String unitName;
        private Integer precisionScale;
        private BigDecimal quantity;
        private String remark;
        private BigDecimal inventoryQuantity;
        private BigDecimal lockedQuantity;
        private BigDecimal availableQuantity;
    }

    @Data
    public static class StocktakeEdit {
        private Long id;
        private Long warehouseId;
        private String stocktakeType;
        private String scopeType;
        private Long zoneId;
        private Long locationId;
        private LocalDateTime stocktakeAt;
        private String remark;
        private List<StocktakeItemEdit> items;
    }

    @Data
    public static class StocktakeItemEdit {
        private Long id;
        private Long materialId;
        private Long locationId;
        private Long unitId;
        private String unitName;
        private Integer precisionScale;
        private BigDecimal bookQuantity;
        private BigDecimal actualQuantity;
        private BigDecimal differenceQuantity;
        private String remark;
    }
}
