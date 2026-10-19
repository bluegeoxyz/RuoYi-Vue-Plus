package org.dromara.property.charge.item.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/** 项目级收费项目。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_charge_item")
public class ChargeItem extends BaseEntity {
    @TableId private Long id;
    private Long projectId;
    private String itemCode;
    private String itemName;
    private String chargeType;
    private String chargeCategory;
    private String chargeObjectType;
    private String status;
    private Integer sort;
    @TableLogic private String delFlag;
}
