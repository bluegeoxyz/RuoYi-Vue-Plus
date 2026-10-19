package org.dromara.property.charge.binding.domain.bo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/** 批量新增房屋收费设置请求。 */
@Data
public class ChargeBindingBatchBo {
    @NotNull private Long projectId;

    @NotEmpty
    @Size(max = 500)
    private List<Long> roomIds;

    @NotNull private Long chargeItemId;
    @NotNull private Long chargeRuleId;
    @NotNull private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private Integer sort;

    @Size(max = 500)
    private String remark;
}
