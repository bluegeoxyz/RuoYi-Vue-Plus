package org.dromara.warehouse.location.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.location.domain.bo.WarehouseLocationBo;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationOptionVo;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationVo;

import java.util.Collection;
import java.util.List;

/** 库位服务。 */
public interface IWarehouseLocationService {
    WarehouseLocationVo queryById(Long id);
    PageResult<WarehouseLocationVo> queryPageList(WarehouseLocationBo bo, PageQuery pageQuery);
    Boolean insertByBo(WarehouseLocationBo bo);
    Boolean updateByBo(WarehouseLocationBo bo);
    Boolean changeStatus(Long id, String status);
    Boolean deleteWithValidByIds(Collection<Long> ids);
    List<WarehouseLocationOptionVo> selectOptions(Long warehouseId, Long zoneId);
}
