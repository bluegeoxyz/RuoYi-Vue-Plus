package org.dromara.property.charge.context.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.*;
import org.dromara.property.charge.item.domain.ChargeItem;
import org.dromara.property.charge.item.mapper.ChargeItemMapper;
import org.dromara.property.charge.payment.mapper.PaymentMapper;
import org.dromara.property.charge.receivable.mapper.ReceivableMapper;
import org.dromara.property.charge.rule.mapper.ChargeRuleMapper;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceContextOptionServiceImplTest {
    @Mock BusinessContextAccessService contextAccessService;
    @Mock PropertyProjectScopeService projectScopeService;
    @Mock ChargeItemMapper itemMapper;
    @Mock ChargeRuleMapper ruleMapper;
    @Mock PaymentMapper paymentMapper;
    @Mock ReceivableMapper receivableMapper;
    @Mock ProjectMapper projectMapper;
    @Mock BuildingMapper buildingMapper;
    @Mock BuildingUnitMapper unitMapper;
    @Mock RoomMapper roomMapper;
    @Mock CustomerMapper customerMapper;
    FinanceContextOptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FinanceContextOptionServiceImpl(contextAccessService, projectScopeService, itemMapper,
            ruleMapper, paymentMapper, receivableMapper, projectMapper, buildingMapper, unitMapper,
            roomMapper, customerMapper);
    }

    @Test
    void noFinanceProjectScopeReturnsNoBindingProjects() {
        when(projectScopeService.resolveBusinessScope(PropertyBusinessDomain.FINANCE, ProjectBusinessAction.OPTION))
            .thenReturn(PropertyProjectScope.none());

        assertTrue(service.chargeBindingProjects().isEmpty());

        verify(contextAccessService).requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.PROJECT);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void crossProjectBuildingCannotExposeUnits() {
        Building building = new Building();
        building.setId(10L);
        building.setProjectId(2L);
        building.setStatus("0");
        when(buildingMapper.selectById(10L)).thenReturn(building);

        assertThrows(ServiceException.class, () -> service.chargeBindingUnits(1L, 10L));

        verify(contextAccessService).requireAccess(BusinessContext.CHARGE_BINDING_QUERY, ReferenceType.UNIT);
        verifyNoInteractions(unitMapper);
    }

    @Test
    void projectScopeFailureStopsChargeItemQuery() {
        doThrow(new ServiceException("无项目权限")).when(projectScopeService)
            .assertBusinessAccess(2L, PropertyBusinessDomain.FINANCE, ProjectBusinessAction.OPTION);

        assertThrows(ServiceException.class,
            () -> service.chargeItems(BusinessContext.RECEIVABLE_QUERY, 2L, null));

        verify(contextAccessService).requireAccess(BusinessContext.RECEIVABLE_QUERY, ReferenceType.CHARGE_ITEM);
        verifyNoInteractions(itemMapper);
    }

    @Test
    void receivableQueryCanReadActiveChargeItemOptions() {
        ChargeItem item = new ChargeItem();
        item.setId(10L);
        item.setProjectId(1L);
        item.setItemCode("WYF");
        item.setItemName("物业费");
        item.setStatus("0");
        when(itemMapper.selectList(any())).thenReturn(List.of(item));

        var result = service.chargeItems(BusinessContext.RECEIVABLE_QUERY, 1L, null);

        assertEquals(1, result.size());
        verify(contextAccessService).requireAccess(BusinessContext.RECEIVABLE_QUERY, ReferenceType.CHARGE_ITEM);
        verify(projectScopeService).assertBusinessAccess(1L, PropertyBusinessDomain.FINANCE,
            ProjectBusinessAction.OPTION);
    }

    @Test
    void crossProjectChargeItemCannotExposeRules() {
        ChargeItem item = new ChargeItem();
        item.setId(10L);
        item.setProjectId(2L);
        item.setStatus("0");
        when(itemMapper.selectById(10L)).thenReturn(item);

        assertThrows(ServiceException.class,
            () -> service.chargeRules(BusinessContext.CHARGE_BINDING_CREATE, 1L, 10L));

        verify(contextAccessService).requireAccess(BusinessContext.CHARGE_BINDING_CREATE, ReferenceType.CHARGE_RULE);
        verifyNoInteractions(ruleMapper);
    }

    @Test
    void crossProjectRoomCannotExposeReceivables() {
        Room room = new Room();
        room.setId(20L);
        room.setProjectId(2L);
        room.setStatus("0");
        when(roomMapper.selectById(20L)).thenReturn(room);

        assertThrows(ServiceException.class, () -> service.paymentReceivables(1L, 20L));

        verify(contextAccessService).requireAccess(BusinessContext.PAYMENT_CREATE, ReferenceType.RECEIVABLE);
        verifyNoInteractions(receivableMapper);
    }

    @Test
    void paymentCustomerCandidatesRequireKeywordAndMaskPersonalData() {
        assertTrue(service.paymentCustomers(1L, " ").isEmpty());
        verifyNoInteractions(customerMapper);

        Customer customer = new Customer();
        customer.setId(7L);
        customer.setCustomerNo("C007");
        customer.setCustomerType("INDIVIDUAL");
        customer.setCustomerName("张三");
        customer.setMobile("13812345678");
        customer.setLifecycleStatus("ACTIVE");
        customer.setStatus("0");
        when(customerMapper.selectList(any())).thenReturn(List.of(customer));

        var result = service.paymentCustomers(1L, "张三");

        assertEquals(1, result.size());
        assertNotEquals("张三", result.getFirst().getCustomerName());
        assertEquals("138****5678", result.getFirst().getMobile());
        verify(contextAccessService, times(2)).requireAccess(BusinessContext.PAYMENT_CREATE, ReferenceType.CUSTOMER);
    }
}
