package org.dromara.property.customer.relation.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.customer.relation.domain.bo.*;
import org.dromara.property.customer.relation.domain.vo.*;
import org.dromara.property.project.domain.vo.ProjectOptionVo;

import java.util.Collection;
import java.util.List;

public interface IRoomCustomerRelationService {
    PageResult<RoomCustomerRelationVo> queryPage(RoomCustomerRelationBo bo, PageQuery pageQuery);
    RoomCustomerRelationVo queryById(Long id);
    List<RoomCustomerRelationVo> queryExport(RoomCustomerRelationBo bo);
    boolean insert(RoomCustomerRelationBo bo);
    /** 在外层已校验独立 import 功能权限时创建关系，仍实时校验项目范围及全部业务规则。 */
    boolean insertForImport(RoomCustomerRelationBo bo);
    boolean update(RoomCustomerRelationBo bo);
    boolean moveIn(RoomCustomerMoveInBo bo);
    boolean moveOut(RoomCustomerMoveOutBo bo);
    boolean delete(Collection<Long> ids);
    List<RoomCustomerRelationVo> options(RoomCustomerRelationBo bo);
    List<CustomerReferenceOptionVo> customerOptions(Long projectId, String keyword);
    List<ProjectOptionVo> projectOptions();
}
