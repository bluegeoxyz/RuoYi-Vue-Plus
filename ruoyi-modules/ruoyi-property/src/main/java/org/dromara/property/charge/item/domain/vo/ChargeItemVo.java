package org.dromara.property.charge.item.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.property.charge.item.domain.ChargeItem;

/** 收费项目展示对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ChargeItem.class)
public class ChargeItemVo extends ChargeItem {
    private String projectName;
}
