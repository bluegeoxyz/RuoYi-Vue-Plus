package org.dromara.property.charge.receivable.domain.bo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReceivableGenerateBo {
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    @NotBlank(message = "生成月份不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "生成月份格式必须为yyyy-MM")
    private String generateMonth;
}
