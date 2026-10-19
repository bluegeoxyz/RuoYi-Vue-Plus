package org.dromara.warehouse.inbound.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.inbound.domain.bo.InboundOrderBo;
import org.dromara.warehouse.inbound.domain.vo.InboundOrderVo;

import java.util.Collection;
import java.util.List;

/** 入库单业务接口。 */
public interface IInboundOrderService {
    InboundOrderVo queryById(Long id);
    PageResult<InboundOrderVo> queryPageList(InboundOrderBo bo, PageQuery pageQuery);
    List<InboundOrderVo> queryList(InboundOrderBo bo);
    Long create(InboundOrderBo bo);
    void update(InboundOrderBo bo);
    void confirm(Long id);
    void complete(Long id);
    void cancel(Long id);
    void deleteDrafts(Collection<Long> ids);
}
