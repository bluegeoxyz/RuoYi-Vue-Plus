package org.dromara.property.charge.receivable.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import java.time.*;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_receivable_batch")
public class ReceivableBatch extends BaseEntity {
    @TableId private Long id;
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
    @TableLogic private String delFlag;
}
