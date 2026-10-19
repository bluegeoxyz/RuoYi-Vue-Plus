package org.dromara.property.charge.context.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.DesensitizedUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.common.security.business.*;
import org.dromara.property.charge.context.domain.vo.*;
import org.dromara.property.charge.context.service.FinanceContextOptionService;
import org.dromara.property.charge.item.domain.ChargeItem;
import org.dromara.property.charge.item.domain.vo.ChargeItemOptionVo;
import org.dromara.property.charge.item.mapper.ChargeItemMapper;
import org.dromara.property.charge.payment.domain.vo.PaymentRoomOptionVo;
import org.dromara.property.charge.payment.mapper.PaymentMapper;
import org.dromara.property.charge.receivable.domain.Receivable;
import org.dromara.property.charge.receivable.mapper.ReceivableMapper;
import org.dromara.property.charge.rule.domain.ChargeRule;
import org.dromara.property.charge.rule.domain.vo.ChargeRuleOptionVo;
import org.dromara.property.charge.rule.mapper.ChargeRuleMapper;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.relation.domain.vo.CustomerReferenceOptionVo;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceContextOptionServiceImpl implements FinanceContextOptionService {
    private final BusinessContextAccessService contextAccessService;
    private final PropertyProjectScopeService projectScopeService;
    private final ChargeItemMapper itemMapper;
    private final ChargeRuleMapper ruleMapper;
    private final PaymentMapper paymentMapper;
    private final ReceivableMapper receivableMapper;
    private final ProjectMapper projectMapper;
    private final BuildingMapper buildingMapper;
    private final BuildingUnitMapper unitMapper;
    private final RoomMapper roomMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<ChargeBindingProjectOptionVo> chargeBindingProjects() {
        contextAccessService.requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.PROJECT);
        PropertyProjectScope scope = projectScopeService.resolveBusinessScope(
            PropertyBusinessDomain.FINANCE, ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        var query = QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName)
            .eq(Project::getStatus, "0");
        if (scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS) query.in(Project::getId, scope.projectIds());
        return projectMapper.selectList(query.orderByAsc(Project::getSort).orderByAsc(Project::getId).build()).stream()
            .map(p -> new ChargeBindingProjectOptionVo(p.getId(), p.getProjectCode(), p.getProjectName())).toList();
    }

    @Override
    public List<ChargeBindingBuildingOptionVo> chargeBindingBuildings(Long projectId) {
        contextAccessService.requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.BUILDING);
        requireProject(projectId, ProjectBusinessAction.OPTION);
        return buildingMapper.selectList(QueryBuilder.lambda(Building.class)
            .select(Building::getId, Building::getProjectId, Building::getBuildingCode, Building::getBuildingName)
            .eq(Building::getProjectId, projectId).eq(Building::getStatus, "0")
            .orderByAsc(Building::getSort).orderByAsc(Building::getId).build()).stream()
            .map(b -> new ChargeBindingBuildingOptionVo(b.getId(), b.getProjectId(),
                b.getBuildingCode(), b.getBuildingName())).toList();
    }

    @Override
    public List<ChargeBindingUnitOptionVo> chargeBindingUnits(Long projectId, Long buildingId) {
        contextAccessService.requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.UNIT);
        requireBuilding(projectId, buildingId);
        return unitMapper.selectList(QueryBuilder.lambda(BuildingUnit.class)
            .select(BuildingUnit::getId, BuildingUnit::getProjectId, BuildingUnit::getBuildingId,
                BuildingUnit::getUnitCode, BuildingUnit::getUnitName)
            .eq(BuildingUnit::getProjectId, projectId).eq(BuildingUnit::getBuildingId, buildingId)
            .eq(BuildingUnit::getStatus, "0").orderByAsc(BuildingUnit::getSort)
            .orderByAsc(BuildingUnit::getId).build()).stream()
            .map(u -> new ChargeBindingUnitOptionVo(u.getId(), u.getProjectId(), u.getBuildingId(),
                u.getUnitCode(), u.getUnitName())).toList();
    }

    @Override
    public List<ChargeBindingRoomOptionVo> chargeBindingRooms(Long projectId, Long buildingId, Long unitId) {
        contextAccessService.requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.ROOM);
        requireUnit(projectId, buildingId, unitId);
        return roomMapper.selectList(QueryBuilder.lambda(Room.class)
            .select(Room::getId, Room::getProjectId, Room::getBuildingId, Room::getUnitId,
                Room::getRoomCode, Room::getRoomNo, Room::getFloorNo)
            .eq(Room::getProjectId, projectId).eq(Room::getBuildingId, buildingId)
            .eq(Room::getUnitId, unitId).eq(Room::getStatus, "0")
            .orderByAsc(Room::getSort).orderByAsc(Room::getId).build()).stream()
            .map(r -> new ChargeBindingRoomOptionVo(r.getId(), r.getProjectId(), r.getBuildingId(), r.getUnitId(),
                r.getRoomCode(), r.getRoomNo(), r.getFloorNo())).toList();
    }

    @Override
    public List<ChargeItemOptionVo> chargeItems(BusinessContext context, Long projectId, String objectType) {
        contextAccessService.requireAccess(context, ReferenceType.CHARGE_ITEM);
        requireProject(projectId, ProjectBusinessAction.OPTION);
        return BeanUtil.copyToList(itemMapper.selectList(QueryBuilder.lambda(ChargeItem.class)
            .eq(ChargeItem::getProjectId, projectId).eq(ChargeItem::getStatus, "0")
            .eqIfText(ChargeItem::getChargeObjectType, objectType)
            .orderByAsc(ChargeItem::getSort).orderByAsc(ChargeItem::getId).build()), ChargeItemOptionVo.class);
    }

    @Override
    public List<ChargeRuleOptionVo> chargeRules(BusinessContext context, Long projectId, Long chargeItemId) {
        contextAccessService.requireAccess(context, ReferenceType.CHARGE_RULE);
        requireProject(projectId, ProjectBusinessAction.OPTION);
        ChargeItem item = itemMapper.selectById(chargeItemId);
        if (item == null || !projectId.equals(item.getProjectId()) || !"0".equals(item.getStatus())) {
            throw new ServiceException("收费项目无效、已停用或不属于当前项目");
        }
        LocalDate today = LocalDate.now();
        return BeanUtil.copyToList(ruleMapper.selectList(QueryBuilder.lambda(ChargeRule.class)
            .eq(ChargeRule::getProjectId, projectId).eq(ChargeRule::getChargeItemId, chargeItemId)
            .eq(ChargeRule::getStatus, "0").le(ChargeRule::getEffectiveStartDate, today)
            .and(q -> q.isNull(ChargeRule::getEffectiveEndDate).or().ge(ChargeRule::getEffectiveEndDate, today))
            .orderByAsc(ChargeRule::getSort).orderByAsc(ChargeRule::getId).build()), ChargeRuleOptionVo.class);
    }

    @Override
    public List<PaymentRoomOptionVo> paymentRooms(Long projectId) {
        contextAccessService.requireAccess(BusinessContext.PAYMENT_CREATE, ReferenceType.ROOM);
        requireProject(projectId, ProjectBusinessAction.OPTION);
        return paymentMapper.selectCollectableRoomOptions(projectId);
    }

    @Override
    public List<CustomerReferenceOptionVo> paymentCustomers(Long projectId, String keyword) {
        contextAccessService.requireAccess(BusinessContext.PAYMENT_CREATE, ReferenceType.CUSTOMER);
        requireProject(projectId, ProjectBusinessAction.OPTION);
        if (StringUtils.isBlank(keyword) || keyword.trim().length() < 2) return List.of();
        return customerMapper.selectList(QueryBuilder.lambda(Customer.class)
            .and(q -> q.like(Customer::getCustomerNo, keyword.trim()).or().like(Customer::getCustomerName, keyword.trim()))
            .eq(Customer::getLifecycleStatus, "ACTIVE").eq(Customer::getStatus, "0").last("LIMIT 20").build()).stream()
            .map(c -> new CustomerReferenceOptionVo(c.getId(), c.getCustomerNo(), c.getCustomerType(), maskName(c), maskMobile(c.getMobile())))
            .toList();
    }

    @Override
    public List<PaymentReceivableOptionVo> paymentReceivables(Long projectId, Long roomId) {
        contextAccessService.requireAccess(BusinessContext.PAYMENT_CREATE, ReferenceType.RECEIVABLE);
        requireProject(projectId, ProjectBusinessAction.OPTION);
        Room room = roomMapper.selectById(roomId);
        if (room == null || !projectId.equals(room.getProjectId()) || !"0".equals(room.getStatus())) {
            throw new ServiceException("房屋无效、已停用或不属于当前项目");
        }
        return receivableMapper.selectList(QueryBuilder.lambda(Receivable.class)
            .eq(Receivable::getProjectId, projectId).eq(Receivable::getRoomId, roomId)
            .in(Receivable::getPaymentStatus, List.of("UNPAID", "PARTIAL"))
            .orderByAsc(Receivable::getDueDate).orderByAsc(Receivable::getId).build()).stream()
            .map(r -> new PaymentReceivableOptionVo(r.getId(), r.getReceivableNo(), r.getProjectId(), r.getRoomId(),
                r.getChargeItemNameSnapshot(), r.getResponsibleCustomerNameSnapshot(), r.getPeriodStartDate(),
                r.getPeriodEndDate(), r.getDueDate(), r.getReceivableAmount(), r.getPaidAmount(),
                money(r.getReceivableAmount()).subtract(money(r.getPaidAmount())), r.getPaymentStatus())).toList();
    }

    private void requireProject(Long projectId, ProjectBusinessAction action) {
        if (projectId == null) throw new ServiceException("所属项目不能为空");
        projectScopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.FINANCE, action);
    }

    private void requireBuilding(Long projectId, Long buildingId) {
        requireProject(projectId, ProjectBusinessAction.OPTION);
        Building building = buildingId == null ? null : buildingMapper.selectById(buildingId);
        if (building == null || !projectId.equals(building.getProjectId()) || !"0".equals(building.getStatus())) {
            throw new ServiceException("楼栋无效、已停用或不属于当前项目");
        }
    }

    private void requireUnit(Long projectId, Long buildingId, Long unitId) {
        requireBuilding(projectId, buildingId);
        BuildingUnit unit = unitId == null ? null : unitMapper.selectById(unitId);
        if (unit == null || !projectId.equals(unit.getProjectId()) || !buildingId.equals(unit.getBuildingId())
            || !"0".equals(unit.getStatus())) {
            throw new ServiceException("单元无效、已停用或不属于当前楼栋");
        }
    }

    private BigDecimal money(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private String maskName(Customer c) { return "INDIVIDUAL".equals(c.getCustomerType())
        ? DesensitizedUtil.desensitized(c.getCustomerName(), DesensitizedUtil.DesensitizedType.CHINESE_NAME) : c.getCustomerName(); }
    private String maskMobile(String mobile) { return StringUtils.isBlank(mobile) ? mobile
        : DesensitizedUtil.desensitized(mobile, DesensitizedUtil.DesensitizedType.MOBILE_PHONE); }
}
