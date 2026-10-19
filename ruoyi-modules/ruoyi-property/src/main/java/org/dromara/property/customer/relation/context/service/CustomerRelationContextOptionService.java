package org.dromara.property.customer.relation.context.service;

import org.dromara.common.security.business.BusinessContext;
import org.dromara.property.customer.relation.context.domain.vo.*;
import org.dromara.property.customer.relation.domain.vo.CustomerReferenceOptionVo;

import java.util.List;

/** 客户关系业务上下文选项服务。业务上下文只允许由固定路由传入。 */
public interface CustomerRelationContextOptionService {
    List<CustomerRelationProjectOptionVo> projects(BusinessContext context);
    List<CustomerRelationBuildingOptionVo> buildings(BusinessContext context, Long projectId);
    List<CustomerRelationUnitOptionVo> units(BusinessContext context, Long projectId, Long buildingId);
    List<CustomerRelationRoomOptionVo> rooms(BusinessContext context, Long projectId, Long buildingId, Long unitId);
    List<CustomerReferenceOptionVo> customers(BusinessContext context, Long projectId, String keyword);
}
