package org.dromara.property.charge.receivable.domain.vo;

import lombok.Data;
import java.time.*;

@Data
public class ReceivableBatchVo {
    private Long id;
    private Long projectId;
    private String batchNo;
    private LocalDate generateMonth;
    private String status;
    private Integer scannedCount;
    private Integer generatedCount;
    private Integer skippedExistingCount;
    private Integer skippedNotDueCount;
    private Integer failedCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String errorMessage;
    private Long createBy;
    private LocalDateTime createTime;
}
