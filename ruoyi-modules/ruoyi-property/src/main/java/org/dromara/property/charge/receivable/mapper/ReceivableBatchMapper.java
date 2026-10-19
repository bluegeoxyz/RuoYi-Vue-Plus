package org.dromara.property.charge.receivable.mapper;

import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.charge.receivable.domain.ReceivableBatch;

public interface ReceivableBatchMapper extends BaseMapperPlus<ReceivableBatch, ReceivableBatch> {
    @Update(
            "UPDATE pm_receivable_batch SET started_at=NOW(),update_time=NOW() WHERE id=#{id} AND del_flag='0'")
    int markStarted(@Param("id") Long id);

    @Update(
            "UPDATE pm_receivable_batch SET status=#{status},scanned_count=#{scannedCount},generated_count=#{generatedCount},skipped_existing_count=#{skippedExistingCount},skipped_not_due_count=#{skippedNotDueCount},failed_count=#{failedCount},finished_at=NOW(),error_message=#{errorMessage},update_time=NOW() WHERE id=#{id} AND del_flag='0'")
    int finish(ReceivableBatch batch);
}
