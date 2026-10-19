package org.dromara.warehouse.transfer.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.warehouse.transfer.domain.TransferOrderItem;
import java.time.LocalDateTime;
import java.util.*;

/** 调拨单请求和查询对象。 */
@Data
public class TransferOrderBo {
    private Long id;
    private String transferNo;

    @NotNull(message = "源仓库不能为空")
    private Long sourceWarehouseId;

    @NotNull(message = "目标仓库不能为空")
    private Long targetWarehouseId;

    @NotBlank(message = "调拨类型不能为空")
    private String transferType;

    private String status;
    private LocalDateTime transferAt;
    private LocalDateTime beginTransferAt;
    private LocalDateTime endTransferAt;
    private String remark;
    @Valid private List<TransferOrderItem> items = new ArrayList<>();
}
