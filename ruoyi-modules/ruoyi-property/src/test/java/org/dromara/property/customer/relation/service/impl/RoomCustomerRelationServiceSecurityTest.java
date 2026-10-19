package org.dromara.property.customer.relation.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.relation.domain.RoomCustomerRelation;
import org.dromara.property.customer.relation.domain.bo.*;
import org.dromara.property.customer.relation.domain.vo.RoomCustomerRelationVo;
import org.dromara.property.customer.relation.mapper.RoomCustomerRelationMapper;
import org.dromara.property.customer.service.CustomerReferenceService;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.room.service.RoomOccupancyStatusService;
import org.dromara.property.scope.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomCustomerRelationServiceSecurityTest {
    @Mock RoomCustomerRelationMapper relationMapper;
    @Mock RoomMapper roomMapper;
    @Mock CustomerMapper customerMapper;
    @Mock CustomerReferenceService customerReferenceService;
    @Mock ProjectMapper projectMapper;
    @Mock PropertyProjectScopeService scopeService;
    @Mock PropertyFunctionalPermissionService functionalPermissionService;
    @Mock RoomOccupancyStatusService occupancyStatusService;
    private RoomCustomerRelationServiceImpl service;

    @BeforeEach void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "relation-test"), RoomCustomerRelation.class);
        service = new RoomCustomerRelationServiceImpl(relationMapper, roomMapper, occupancyStatusService, customerMapper, customerReferenceService, projectMapper, scopeService, functionalPermissionService);
    }

    @Test void noneScopeReturnsEmptyWithoutRelationQuery() {
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.READ)).thenReturn(PropertyProjectScope.none());
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.EXPORT)).thenReturn(PropertyProjectScope.none());
        assertEquals(0, service.queryPage(new RoomCustomerRelationBo(), new PageQuery()).getTotal());
        assertTrue(service.queryExport(new RoomCustomerRelationBo()).isEmpty());
        assertTrue(service.options(new RoomCustomerRelationBo()).isEmpty());
        verifyNoInteractions(relationMapper);
    }

    @Test void detailAuthorizesPersistedProject() {
        when(relationMapper.selectById(1L)).thenReturn(relation(1L, 101L, 11L, 21L, "PENDING"));
        doThrow(new ServiceException("拒绝")).when(scopeService).assertBusinessAccess(101L, PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.READ);
        assertThrows(ServiceException.class, () -> service.queryById(1L));
        verify(relationMapper, never()).selectDisplayById(anyLong());
    }

    @Test void insertRejectsForgedRoomProject() {
        RoomCustomerRelationBo bo = addBo(101L, 11L, 21L);
        when(roomMapper.selectActiveByIdForUpdate(11L)).thenReturn(room(11L, 202L));
        assertThrows(ServiceException.class, () -> service.insert(bo));
        verifyNoInteractions(customerReferenceService);
        verify(relationMapper, never()).insert(any(RoomCustomerRelation.class));
    }

    @Test void ordinaryEditRejectsIdentityAndLifecycleChanges() {
        RoomCustomerRelation current = relation(1L, 101L, 11L, 21L, "PENDING");
        when(relationMapper.selectById(1L)).thenReturn(current);
        when(roomMapper.selectActiveByIdForUpdate(11L)).thenReturn(room(11L, 101L));
        when(relationMapper.selectActiveByIdForUpdate(1L)).thenReturn(current);
        RoomCustomerRelationBo bo = new RoomCustomerRelationBo(); bo.setId(1L); bo.setCustomerId(99L);
        assertThrows(ServiceException.class, () -> service.update(bo));
        verify(relationMapper, never()).updateById(any(RoomCustomerRelation.class));
    }

    @Test void activeRelationCannotBeDeleted() {
        RoomCustomerRelation current = relation(1L, 101L, 11L, 21L, "ACTIVE");
        when(relationMapper.selectById(1L)).thenReturn(current);
        when(roomMapper.selectActiveByIdForUpdate(11L)).thenReturn(room(11L, 101L));
        when(relationMapper.selectActiveByIdForUpdate(1L)).thenReturn(current);
        assertThrows(ServiceException.class, () -> service.delete(List.of(1L)));
        verify(relationMapper, never()).deleteByIds(anyCollection());
    }

    @Test void repeatedMoveOutIsRejected() {
        LocalDate date = LocalDate.of(2026, 1, 2);
        RoomCustomerRelation current = relation(1L, 101L, 11L, 21L, "ENDED"); current.setMoveOutDate(date);
        when(relationMapper.selectById(1L)).thenReturn(current);
        when(roomMapper.selectActiveByIdForUpdate(11L)).thenReturn(room(11L, 101L));
        when(relationMapper.selectActiveByIdForUpdate(1L)).thenReturn(current);
        RoomCustomerMoveOutBo bo = new RoomCustomerMoveOutBo(); bo.setId(1L); bo.setMoveOutDate(date);
        assertThrows(ServiceException.class, () -> service.moveOut(bo));
        verify(relationMapper, never()).updateById(any(RoomCustomerRelation.class));
    }

    @Test @SuppressWarnings("unchecked") void listExportOptionsResolveIndependentActions() {
        when(scopeService.resolveBusinessScope(any(), any())).thenReturn(PropertyProjectScope.explicit(Set.of(101L)));
        when(relationMapper.selectDisplayPage(any(), any(), anyCollection())).thenReturn(new Page<RoomCustomerRelationVo>());
        when(relationMapper.selectDisplayList(any(), anyCollection())).thenReturn(List.of());
        service.queryPage(new RoomCustomerRelationBo(), new PageQuery()); service.queryExport(new RoomCustomerRelationBo()); service.options(new RoomCustomerRelationBo());
        verify(scopeService, times(2)).resolveBusinessScope(PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.READ);
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.EXPORT);
        verify(scopeService, never()).resolveBusinessScope(PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.OPTION);
    }

    private static RoomCustomerRelationBo addBo(long project, long room, long customer) {
        RoomCustomerRelationBo bo = new RoomCustomerRelationBo(); bo.setProjectId(project); bo.setRoomId(room); bo.setCustomerId(customer);
        bo.setRelationType("OWNER"); bo.setStartDate(LocalDate.of(2026, 1, 1)); return bo;
    }
    private static Room room(long id, long project) { Room room = new Room(); room.setId(id); room.setProjectId(project); room.setStatus("0"); return room; }
    private static RoomCustomerRelation relation(long id, long project, long room, long customer, String status) {
        RoomCustomerRelation relation = new RoomCustomerRelation(); relation.setId(id); relation.setProjectId(project); relation.setRoomId(room); relation.setCustomerId(customer);
        relation.setRelationType("OWNER"); relation.setStartDate(LocalDate.of(2026, 1, 1)); relation.setRelationStatus(status); relation.setStatus("0"); return relation;
    }
}
