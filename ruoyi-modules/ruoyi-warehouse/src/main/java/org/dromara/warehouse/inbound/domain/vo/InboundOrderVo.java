package org.dromara.warehouse.inbound.domain.vo;

import org.apache.fesod.sheet.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 入库单展示对象。 */
@Data
public class InboundOrderVo {
    private Long id;
    @ExcelProperty("入库单号") private String inboundNo;
    private Long companyId;
    @ExcelProperty("所属公司") private String companyName;
    private Long projectId;
    @ExcelProperty("所属项目") private String projectName;
    private Long warehouseId;
    @ExcelProperty("仓库") private String warehouseName;
    @ExcelProperty("入库类型") private String inboundType;
    @ExcelProperty("状态") private String status;
    @ExcelProperty("入库时间") private LocalDateTime inboundAt;
    @ExcelProperty("完成时间") private LocalDateTime completedAt;
    private Long operatorId;
    @ExcelProperty("操作人") private String operatorName;
    @ExcelProperty("创建时间") private LocalDateTime createTime;
    private String remark;
    private List<InboundOrderItemVo> items;
}
