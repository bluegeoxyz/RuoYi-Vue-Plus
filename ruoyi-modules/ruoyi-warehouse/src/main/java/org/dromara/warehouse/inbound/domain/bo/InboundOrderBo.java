package org.dromara.warehouse.inbound.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.warehouse.inbound.domain.InboundOrderItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 入库单写入及查询对象。 */
@Data
public class InboundOrderBo {
    private Long id;
    private String inboundNo;
    private Long companyId;
    @NotNull(message = "仓库不能为空") private Long warehouseId;
    @NotBlank(message = "入库类型不能为空") private String inboundType;
    private String status;
    private LocalDateTime inboundAt;
    private LocalDateTime beginInboundAt;
    private LocalDateTime endInboundAt;
    private String remark;
    @Valid private List<InboundOrderItem> items = new ArrayList<>();
}
