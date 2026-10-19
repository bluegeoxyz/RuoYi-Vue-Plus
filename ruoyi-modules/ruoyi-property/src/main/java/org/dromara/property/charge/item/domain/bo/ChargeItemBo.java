package org.dromara.property.charge.item.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.property.charge.item.domain.ChargeItem;

/** 收费项目业务对象。 */
@Data
@AutoMapper(target = ChargeItem.class, reverseConvertGenerate = false)
public class ChargeItemBo {
    @NotNull(groups = EditGroup.class) private Long id;
    @NotNull(groups = AddGroup.class) private Long projectId;
    @NotBlank(groups = AddGroup.class) @Size(max = 64) private String itemCode;
    @NotBlank(groups = {AddGroup.class, EditGroup.class}) @Size(max = 128) private String itemName;
    @NotBlank(groups = {AddGroup.class, EditGroup.class}) @Size(max = 32) private String chargeType;
    @NotBlank(groups = {AddGroup.class, EditGroup.class}) @Size(max = 32) private String chargeCategory;
    @NotBlank(groups = {AddGroup.class, EditGroup.class}) @Size(max = 32) private String chargeObjectType;
    @Size(max = 1) private String status;
    private Integer sort;
    @Size(max = 500) private String remark;
}
