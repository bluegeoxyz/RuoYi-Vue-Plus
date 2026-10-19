package org.dromara.property.charge.receivable.domain.vo;

import lombok.*;

@Data
@Builder
public class ReceivableGenerationResultVo {
    private Long batchId;
    private String batchNo;
    private Long projectId;
    private String generateMonth;
    private int scannedCount;
    private int generatedCount;
    private int skippedExistingCount;
    private int skippedNotDueCount;
    private int failedCount;
    private String status;
    private String errorMessage;
}
