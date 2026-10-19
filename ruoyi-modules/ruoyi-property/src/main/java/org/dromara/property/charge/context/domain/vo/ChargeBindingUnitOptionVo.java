package org.dromara.property.charge.context.domain.vo;

/** 房屋收费设置单元选项。 */
public record ChargeBindingUnitOptionVo(Long id, Long projectId, Long buildingId, String unitCode, String unitName) {
}
