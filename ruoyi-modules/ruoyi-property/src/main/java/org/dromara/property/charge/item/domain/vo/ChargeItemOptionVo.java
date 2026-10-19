package org.dromara.property.charge.item.domain.vo;

import lombok.Data;

/** 收费项目轻量选项。 */
@Data
public class ChargeItemOptionVo {
    private Long id;
    private Long projectId;
    private String itemCode;
    private String itemName;
    private String chargeType;
    private String chargeCategory;
    private String chargeObjectType;
}
