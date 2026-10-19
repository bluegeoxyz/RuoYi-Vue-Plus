package org.dromara.property.customer.relation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.relation.domain.RoomCustomerRelation;
import org.dromara.property.customer.relation.domain.bo.*;
import org.dromara.property.customer.relation.domain.vo.*;
import org.dromara.property.customer.relation.mapper.RoomCustomerRelationMapper;
import org.dromara.property.customer.relation.service.IRoomCustomerRelationService;
import org.dromara.property.customer.service.CustomerReferenceService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.room.service.RoomOccupancyStatusService;
import org.dromara.property.scope.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomCustomerRelationServiceImpl implements IRoomCustomerRelationService {
    private final RoomCustomerRelationMapper relationMapper;
    private final RoomMapper roomMapper;
    private final RoomOccupancyStatusService occupancyStatusService;
    private final CustomerMapper customerMapper;
    private final CustomerReferenceService customerReferenceService;
    private final ProjectMapper projectMapper;
    private final PropertyProjectScopeService scopeService;
    private final PropertyFunctionalPermissionService functionalPermissionService;

    @Override
    public PageResult<RoomCustomerRelationVo> queryPage(RoomCustomerRelationBo bo, PageQuery pageQuery) {
        PropertyProjectScope scope = scope(ProjectBusinessAction.READ, "property:customerRelation:list");
        if (scope.type() == PropertyProjectScope.Type.NONE) return PageResult.build(List.of(), 0L);
        Page<RoomCustomerRelationVo> page = relationMapper.selectDisplayPage(pageQuery.build(), bo, scopedProjectIds(scope));
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public RoomCustomerRelationVo queryById(Long id) {
        RoomCustomerRelation relation = require(id);
        access(relation.getProjectId(), ProjectBusinessAction.READ, "property:customerRelation:query");
        return relationMapper.selectDisplayById(id);
    }

    @Override
    public List<RoomCustomerRelationVo> queryExport(RoomCustomerRelationBo bo) {
        PropertyProjectScope scope = scope(ProjectBusinessAction.EXPORT, "property:customerRelation:export");
        return scope.type() == PropertyProjectScope.Type.NONE ? List.of() : relationMapper.selectDisplayList(bo, scopedProjectIds(scope));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insert(RoomCustomerRelationBo bo) {
        access(bo.getProjectId(), ProjectBusinessAction.WRITE, "property:customerRelation:add");
        return insertCore(bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertForImport(RoomCustomerRelationBo bo) {
        scopeService.assertBusinessAccess(bo.getProjectId(), PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.WRITE);
        return insertCore(bo);
    }

    private boolean insertCore(RoomCustomerRelationBo bo) {
        Room room = lockRoom(bo.getRoomId(), bo.getProjectId());
        customerReferenceService.lockAndRequireActiveForRelation(bo.getCustomerId());
        assertNoEffectiveDuplicate(room.getId(), bo.getCustomerId(), bo.getRelationType());
        RoomCustomerRelation relation = BeanUtil.copyProperties(bo, RoomCustomerRelation.class);
        relation.setProjectId(room.getProjectId());
        relation.setRoomId(room.getId());
        relation.setRelationStatus("PENDING");
        relation.setStatus("0");
        relation.setIsPrimary("1".equals(bo.getIsPrimary()) ? "1" : "0");
        relation.setEndDate(null);
        relation.setMoveInDate(null);
        relation.setMoveOutDate(null);
        try {
            return relationMapper.insert(relation) > 0;
        } catch (DuplicateKeyException e) {
            throw duplicateRelation();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(RoomCustomerRelationBo bo) {
        RoomCustomerRelation snapshot = require(bo.getId());
        access(snapshot.getProjectId(), ProjectBusinessAction.WRITE, "property:customerRelation:edit");
        lockParentsAndRelation(snapshot, false);
        rejectIdentityChanges(bo, snapshot);
        RoomCustomerRelation update = new RoomCustomerRelation();
        update.setId(snapshot.getId());
        update.setIsPrimary("1".equals(bo.getIsPrimary()) ? "1" : "0");
        update.setRemark(bo.getRemark());
        return relationMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveIn(RoomCustomerMoveInBo bo) {
        RoomCustomerRelation snapshot = require(bo.getId());
        access(snapshot.getProjectId(), ProjectBusinessAction.WRITE, "property:customerRelation:moveIn");
        RoomCustomerRelation relation = lockParentsAndRelation(snapshot, true);
        if ("ACTIVE".equals(relation.getRelationStatus())) throw new ServiceException("关系已经入住，不能重复办理入住");
        if (!"PENDING".equals(relation.getRelationStatus())) throw new ServiceException("只有待生效关系可以办理入住");
        if (bo.getMoveInDate().isBefore(relation.getStartDate())) throw new ServiceException("迁入日期不能早于关系开始日期");
        assertNoOtherEffectiveDuplicate(relation);
        RoomCustomerRelation update = new RoomCustomerRelation();
        update.setId(relation.getId());
        update.setMoveInDate(bo.getMoveInDate());
        update.setRelationStatus("ACTIVE");
        if (relationMapper.updateById(update) != 1) throw new ServiceException("房屋客户关系状态更新失败");
        occupancyStatusService.recalculate(relation.getRoomId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveOut(RoomCustomerMoveOutBo bo) {
        RoomCustomerRelation snapshot = require(bo.getId());
        access(snapshot.getProjectId(), ProjectBusinessAction.WRITE, "property:customerRelation:moveOut");
        RoomCustomerRelation relation = lockParentsAndRelation(snapshot, false);
        if ("ENDED".equals(relation.getRelationStatus())) throw new ServiceException("关系已经迁出，不能重复办理迁出");
        if (!"ACTIVE".equals(relation.getRelationStatus())) throw new ServiceException("只有有效关系可以办理迁出");
        LocalDate lowerBound = relation.getMoveInDate() == null ? relation.getStartDate() : relation.getMoveInDate();
        if (bo.getMoveOutDate().isBefore(lowerBound)) throw new ServiceException("迁出日期不能早于迁入或开始日期");
        RoomCustomerRelation update = new RoomCustomerRelation();
        update.setId(relation.getId());
        update.setMoveOutDate(bo.getMoveOutDate());
        update.setEndDate(bo.getMoveOutDate());
        update.setRelationStatus("ENDED");
        if (relationMapper.updateById(update) != 1) throw new ServiceException("房屋客户关系状态更新失败");
        occupancyStatusService.recalculate(relation.getRoomId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Collection<Long> ids) {
        List<RoomCustomerRelation> snapshots = ids.stream().map(this::require)
            .sorted(Comparator.comparing(RoomCustomerRelation::getRoomId).thenComparing(RoomCustomerRelation::getCustomerId))
            .toList();
        for (RoomCustomerRelation snapshot : snapshots) {
            access(snapshot.getProjectId(), ProjectBusinessAction.WRITE, "property:customerRelation:remove");
            RoomCustomerRelation relation = lockParentsAndRelation(snapshot, false);
            if (!"PENDING".equals(relation.getRelationStatus()) || relation.getMoveInDate() != null) {
                throw new ServiceException("只有未入住的待生效误录关系可以删除，已生效关系必须办理迁出并保留历史");
            }
        }
        return relationMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<RoomCustomerRelationVo> options(RoomCustomerRelationBo bo) {
        PropertyProjectScope scope = scope(ProjectBusinessAction.READ, "property:customerRelation:list");
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        return relationMapper.selectDisplayList(bo, scopedProjectIds(scope));
    }

    @Override
    public List<CustomerReferenceOptionVo> customerOptions(Long projectId, String keyword) {
        access(projectId, ProjectBusinessAction.OPTION, "property:customerRelation:add");
        if (StringUtils.isBlank(keyword) || keyword.trim().length() < 2) return List.of();
        List<Customer> customers = customerMapper.selectList(QueryBuilder.lambda(Customer.class)
            .and(q -> q.like(Customer::getCustomerNo, keyword.trim()).or().like(Customer::getCustomerName, keyword.trim()))
            .eq(Customer::getLifecycleStatus, "ACTIVE").eq(Customer::getStatus, "0").last("LIMIT 20").build());
        return customers.stream().map(c -> new CustomerReferenceOptionVo(c.getId(), c.getCustomerNo(), c.getCustomerType(),
            maskName(c), maskMobile(c.getMobile()))).toList();
    }

    @Override
    public List<ProjectOptionVo> projectOptions() {
        PropertyProjectScope scope = scope(ProjectBusinessAction.OPTION, "property:customerRelation:list");
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        if (scope.type() == PropertyProjectScope.Type.ALL) return projectMapper.selectOptions();
        return projectMapper.selectVoList(QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getProjectShortName)
            .in(Project::getId, scope.projectIds()).eq(Project::getStatus, "0")
            .orderByAsc(Project::getSort).orderByAsc(Project::getId).build(), ProjectOptionVo.class);
    }

    private Collection<Long> scopedProjectIds(PropertyProjectScope scope) {
        return scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS ? scope.projectIds() : null;
    }

    private RoomCustomerRelation lockParentsAndRelation(RoomCustomerRelation snapshot, boolean requireActiveCustomer) {
        lockRoom(snapshot.getRoomId(), snapshot.getProjectId());
        if (requireActiveCustomer) {
            customerReferenceService.lockAndRequireActiveForRelation(snapshot.getCustomerId());
        } else {
            customerReferenceService.lockForExistingRelation(snapshot.getCustomerId());
        }
        RoomCustomerRelation locked = relationMapper.selectActiveByIdForUpdate(snapshot.getId());
        if (locked == null) throw new ServiceException("房屋客户关系不存在");
        return locked;
    }

    private Room lockRoom(Long roomId, Long projectId) {
        Room room = roomMapper.selectActiveByIdForUpdate(roomId);
        if (room == null || !"0".equals(room.getStatus()) || !projectId.equals(room.getProjectId())) {
            throw new ServiceException("房屋不存在、已停用或不属于目标项目");
        }
        return room;
    }

    private RoomCustomerRelation require(Long id) {
        RoomCustomerRelation relation = relationMapper.selectById(id);
        if (relation == null) throw new ServiceException("房屋客户关系不存在");
        return relation;
    }

    private void rejectIdentityChanges(RoomCustomerRelationBo bo, RoomCustomerRelation current) {
        if (bo.getProjectId() != null && !bo.getProjectId().equals(current.getProjectId())
            || bo.getRoomId() != null && !bo.getRoomId().equals(current.getRoomId())
            || bo.getCustomerId() != null && !bo.getCustomerId().equals(current.getCustomerId())
            || bo.getRelationType() != null && !bo.getRelationType().equals(current.getRelationType())
            || bo.getRelationStatus() != null && !bo.getRelationStatus().equals(current.getRelationStatus())) {
            throw new ServiceException("普通编辑不允许变更项目、房屋、客户、关系类型或生命周期");
        }
    }

    private void assertNoEffectiveDuplicate(Long roomId, Long customerId, String relationType) {
        if (!relationMapper.selectEffectiveDuplicatesForUpdate(roomId, customerId, relationType).isEmpty()) throw duplicateRelation();
    }

    private void assertNoOtherEffectiveDuplicate(RoomCustomerRelation relation) {
        boolean duplicate = relationMapper.selectEffectiveDuplicatesForUpdate(relation.getRoomId(), relation.getCustomerId(), relation.getRelationType())
            .stream().anyMatch(id -> !id.equals(relation.getId()));
        if (duplicate) throw duplicateRelation();
    }

    private ServiceException duplicateRelation() {
        return new ServiceException("同一房屋、客户和关系类型已存在待生效或有效关系");
    }

    private PropertyProjectScope scope(ProjectBusinessAction action, String permission) {
        functionalPermissionService.assertCurrent(permission);
        return scopeService.resolveBusinessScope(PropertyBusinessDomain.CUSTOMER_RELATION, action);
    }

    private void access(Long projectId, ProjectBusinessAction action, String permission) {
        functionalPermissionService.assertCurrent(permission);
        scopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.CUSTOMER_RELATION, action);
    }

    private String maskName(Customer customer) {
        return StringUtils.isBlank(customer.getCustomerName()) || "COMPANY".equals(customer.getCustomerType())
            ? customer.getCustomerName() : DesensitizedUtil.chineseName(customer.getCustomerName());
    }

    private String maskMobile(String mobile) {
        return StringUtils.isBlank(mobile) ? mobile : DesensitizedUtil.mobilePhone(mobile);
    }
}
