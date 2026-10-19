package org.dromara.warehouse.outbound.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.warehouse.outbound.domain.OutboundOrderItem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 出库单请求与查询对象。 */
@Data
public class OutboundOrderBo {
    private Long id;
    private String outboundNo;
    private Long companyId;
    @NotNull(message="仓库不能为空") private Long warehouseId;
    @NotBlank(message="出库类型不能为空") private String outboundType;
    private String status;
    private LocalDateTime outboundAt;
    private LocalDateTime beginOutboundAt;
    private LocalDateTime endOutboundAt;
    private String remark;
    @Valid private List<OutboundOrderItem> items = new ArrayList<>();
}
