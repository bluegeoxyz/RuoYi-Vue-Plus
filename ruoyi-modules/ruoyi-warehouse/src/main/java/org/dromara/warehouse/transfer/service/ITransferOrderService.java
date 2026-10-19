package org.dromara.warehouse.transfer.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.transfer.domain.bo.TransferOrderBo;
import org.dromara.warehouse.transfer.domain.vo.TransferOrderVo;
import java.util.*;

public interface ITransferOrderService {
    TransferOrderVo queryById(Long id);

    PageResult<TransferOrderVo> queryPageList(TransferOrderBo bo, PageQuery pq);

    List<TransferOrderVo> queryList(TransferOrderBo bo);

    Long create(TransferOrderBo bo);

    void update(TransferOrderBo bo);

    void confirm(Long id);

    void complete(Long id);

    void cancel(Long id);

    void deleteDrafts(Collection<Long> ids);
}
