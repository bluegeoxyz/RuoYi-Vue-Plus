package org.dromara.property.charge.receivable.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.charge.receivable.domain.bo.*;
import org.dromara.property.charge.receivable.domain.vo.*;
import java.util.List;

public interface IReceivableService {
    PageResult<ReceivableVo> queryPage(ReceivableQueryBo bo, PageQuery pageQuery);

    ReceivableVo queryById(Long id);

    ReceivableSummaryVo summary(ReceivableQueryBo bo);

    List<ReceivableExportVo> export(ReceivableQueryBo bo);

    boolean cancel(Long id, ReceivableCancelBo bo);

    PageResult<ReceivableBatchVo> queryBatchPage(ReceivableBatchQueryBo bo, PageQuery pageQuery);

    ReceivableBatchVo queryBatchById(Long id);
}
