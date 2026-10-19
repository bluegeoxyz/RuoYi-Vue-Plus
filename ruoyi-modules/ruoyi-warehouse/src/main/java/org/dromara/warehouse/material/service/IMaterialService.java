package org.dromara.warehouse.material.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.warehouse.material.domain.bo.MaterialBo;
import org.dromara.warehouse.material.domain.vo.MaterialOptionVo;
import org.dromara.warehouse.material.domain.vo.MaterialVo;

import java.util.Collection;
import java.util.List;

/** 物料服务。 */
public interface IMaterialService {
    MaterialVo queryById(Long id);
    PageResult<MaterialVo> queryPageList(MaterialBo bo, PageQuery pageQuery);
    Boolean insertByBo(MaterialBo bo);
    Boolean updateByBo(MaterialBo bo);
    Boolean changeStatus(Long id, String status);
    Boolean deleteWithValidByIds(Collection<Long> ids);
    List<MaterialOptionVo> selectOptions(Long companyId);
    List<SysDeptVo> selectCompanyOptions();
}
