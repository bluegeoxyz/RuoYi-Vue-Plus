package org.dromara.property.charge.context.service;

import org.dromara.common.security.business.BusinessContext;
import org.dromara.property.charge.context.domain.vo.ChargeBindingBuildingOptionVo;
import org.dromara.property.charge.context.domain.vo.ChargeBindingProjectOptionVo;
import org.dromara.property.charge.context.domain.vo.ChargeBindingRoomOptionVo;
import org.dromara.property.charge.context.domain.vo.ChargeBindingUnitOptionVo;
import org.dromara.property.charge.context.domain.vo.PaymentReceivableOptionVo;
import org.dromara.property.charge.item.domain.vo.ChargeItemOptionVo;
import org.dromara.property.charge.payment.domain.vo.PaymentRoomOptionVo;
import org.dromara.property.charge.rule.domain.vo.ChargeRuleOptionVo;
import org.dromara.property.customer.relation.domain.vo.CustomerReferenceOptionVo;

import java.util.List;

/** 收费业务上下文引用数据服务。 */
public interface FinanceContextOptionService {
    List<ChargeBindingProjectOptionVo> chargeBindingProjects();
    List<ChargeBindingBuildingOptionVo> chargeBindingBuildings(Long projectId);
    List<ChargeBindingUnitOptionVo> chargeBindingUnits(Long projectId, Long buildingId);
    List<ChargeBindingRoomOptionVo> chargeBindingRooms(Long projectId, Long buildingId, Long unitId);
    List<ChargeItemOptionVo> chargeItems(BusinessContext context, Long projectId, String objectType);
    List<ChargeRuleOptionVo> chargeRules(BusinessContext context, Long projectId, Long chargeItemId);
    List<PaymentRoomOptionVo> paymentRooms(Long projectId);
    List<CustomerReferenceOptionVo> paymentCustomers(Long projectId, String keyword);
    List<PaymentReceivableOptionVo> paymentReceivables(Long projectId, Long roomId);
}
