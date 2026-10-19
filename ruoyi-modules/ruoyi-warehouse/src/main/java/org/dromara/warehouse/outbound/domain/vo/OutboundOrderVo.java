package org.dromara.warehouse.outbound.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import java.time.LocalDateTime;
import java.util.List;

/** 出库单展示对象。 */
@Data
public class OutboundOrderVo {
    private Long id; @ExcelProperty("出库单号") private String outboundNo;
    private Long companyId; @ExcelProperty("所属公司") private String companyName;
    private Long projectId; @ExcelProperty("所属项目") private String projectName;
    private Long warehouseId; @ExcelProperty("仓库") private String warehouseName;
    @ExcelProperty("出库类型") private String outboundType; @ExcelProperty("状态") private String status;
    @ExcelProperty("出库时间") private LocalDateTime outboundAt; private LocalDateTime confirmedAt;
    @ExcelProperty("完成时间") private LocalDateTime completedAt; private LocalDateTime cancelledAt;
    private Long operatorId; @ExcelProperty("操作人") private String operatorName;
    @ExcelProperty("创建时间") private LocalDateTime createTime; private String remark;
    private List<OutboundOrderItemVo> items;
}
