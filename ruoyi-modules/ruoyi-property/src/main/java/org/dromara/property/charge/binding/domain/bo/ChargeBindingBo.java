package org.dromara.property.charge.binding.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.*;
import org.dromara.property.charge.binding.domain.ChargeBinding;
import java.time.LocalDate;

@Data
@AutoMapper(target = ChargeBinding.class, reverseConvertGenerate = false)
public class ChargeBindingBo {
    @NotNull(groups = EditGroup.class)
    private Long id;

    @NotNull(groups = AddGroup.class)
    private Long projectId;

    @NotBlank(groups = AddGroup.class)
    private String objectType;

    @NotNull(groups = AddGroup.class)
    private Long objectId;

    @NotNull(groups = AddGroup.class)
    private Long chargeItemId;

    @NotNull(groups = AddGroup.class)
    private Long chargeRuleId;

    @NotNull(groups = AddGroup.class)
    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    @Size(max = 1)
    private String status;

    private Integer sort;

    @Size(max = 500)
    private String remark;
}
