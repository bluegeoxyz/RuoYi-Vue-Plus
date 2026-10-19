package org.dromara.warehouse.zone.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.zone.domain.bo.WarehouseZoneBo;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneOptionVo;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneVo;

import java.util.Collection;
import java.util.List;

/** 库区服务。 */
public interface IWarehouseZoneService {
    WarehouseZoneVo queryById(Long id);
    PageResult<WarehouseZoneVo> queryPageList(WarehouseZoneBo bo, PageQuery pageQuery);
    Boolean insertByBo(WarehouseZoneBo bo);
    Boolean updateByBo(WarehouseZoneBo bo);
    Boolean changeStatus(Long id, String status);
    Boolean deleteWithValidByIds(Collection<Long> ids);
    List<WarehouseZoneOptionVo> selectOptions(Long warehouseId);
}
