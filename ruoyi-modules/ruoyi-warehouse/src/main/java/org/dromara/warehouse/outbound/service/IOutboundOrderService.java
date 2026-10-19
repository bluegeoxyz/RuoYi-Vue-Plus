package org.dromara.warehouse.outbound.service;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.outbound.domain.bo.OutboundOrderBo;
import org.dromara.warehouse.outbound.domain.vo.OutboundOrderVo;
import java.util.Collection; import java.util.List;
public interface IOutboundOrderService {
    OutboundOrderVo queryById(Long id); PageResult<OutboundOrderVo> queryPageList(OutboundOrderBo bo, PageQuery pageQuery); List<OutboundOrderVo> queryList(OutboundOrderBo bo);
    Long create(OutboundOrderBo bo); void update(OutboundOrderBo bo); void confirm(Long id); void complete(Long id); void cancel(Long id); void deleteDrafts(Collection<Long> ids);
}
