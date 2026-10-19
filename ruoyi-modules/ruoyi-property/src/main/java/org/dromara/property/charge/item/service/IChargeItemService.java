package org.dromara.property.charge.item.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.charge.item.domain.bo.ChargeItemBo;
import org.dromara.property.charge.item.domain.vo.ChargeItemOptionVo;
import org.dromara.property.charge.item.domain.vo.ChargeItemVo;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import java.util.Collection;
import java.util.List;

public interface IChargeItemService {
    ChargeItemVo queryById(Long id);
    PageResult<ChargeItemVo> queryPage(ChargeItemBo bo, PageQuery pageQuery);
    List<ChargeItemVo> queryExport(ChargeItemBo bo);
    boolean insert(ChargeItemBo bo);
    boolean update(ChargeItemBo bo);
    boolean changeStatus(Long id, String status);
    boolean delete(Collection<Long> ids);
    List<ChargeItemOptionVo> options(ChargeItemBo bo);
    List<ProjectOptionVo> projectOptions();
}
