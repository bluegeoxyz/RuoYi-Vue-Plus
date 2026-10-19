package org.dromara.warehouse.warehouse.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.api.ProjectReferenceDTO;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.warehouse.warehouse.domain.bo.WarehouseBo;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseOptionVo;
import org.dromara.warehouse.warehouse.domain.vo.WarehouseVo;

import java.util.Collection;
import java.util.List;

/** 仓库服务。 */
public interface IWarehouseService {
    WarehouseVo queryById(Long id);
    PageResult<WarehouseVo> queryPageList(WarehouseBo bo, PageQuery pageQuery);
    Boolean insertByBo(WarehouseBo bo);
    Boolean updateByBo(WarehouseBo bo);
    Boolean changeStatus(Long id, String status);
    Boolean deleteWithValidByIds(Collection<Long> ids);
    List<WarehouseOptionVo> selectOptions();
    List<SysDeptVo> selectCompanyOptions();
    List<ProjectReferenceDTO> selectProjectOptions();
}
