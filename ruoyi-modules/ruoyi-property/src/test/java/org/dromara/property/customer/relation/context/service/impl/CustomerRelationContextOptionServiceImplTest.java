package org.dromara.property.customer.relation.context.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.*;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
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
class CustomerRelationContextOptionServiceImplTest {
    @Mock BusinessContextAccessService contextAccessService;
    @Mock PropertyProjectScopeService scopeService;
    @Mock ProjectMapper projectMapper;
    @Mock BuildingMapper buildingMapper;
    @Mock BuildingUnitMapper unitMapper;
    @Mock RoomMapper roomMapper;
    @Mock CustomerMapper customerMapper;
    CustomerRelationContextOptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerRelationContextOptionServiceImpl(contextAccessService, scopeService, projectMapper,
            buildingMapper, unitMapper, roomMapper, customerMapper);
    }

    @Test
    void noProjectMembershipReturnsNoProjects() {
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.OPTION))
            .thenReturn(PropertyProjectScope.none());
        assertTrue(service.projects(BusinessContext.CUSTOMER_RELATION_LIST).isEmpty());
        verify(contextAccessService).requireAccess(BusinessContext.CUSTOMER_RELATION_LIST, ReferenceType.PROJECT);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void crossProjectBuildingIsRejectedBeforeUnitQuery() {
        Project project = new Project(); project.setId(1L); project.setStatus("0");
        Building building = new Building(); building.setId(10L); building.setProjectId(2L); building.setStatus("0");
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(buildingMapper.selectById(10L)).thenReturn(building);
        assertThrows(ServiceException.class,
            () -> service.units(BusinessContext.CUSTOMER_RELATION_CREATE, 1L, 10L));
        verify(scopeService).assertBusinessAccess(1L, PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.OPTION);
        verifyNoInteractions(unitMapper);
    }

    @Test
    void customerCandidatesStayMinimalAndMasked() {
        Project project = new Project(); project.setId(1L); project.setStatus("0");
        Customer customer = new Customer(); customer.setId(7L); customer.setCustomerNo("C007");
        customer.setCustomerType("INDIVIDUAL"); customer.setCustomerName("张三"); customer.setMobile("13812345678");
        customer.setLifecycleStatus("ACTIVE"); customer.setStatus("0");
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(customerMapper.selectList(any())).thenReturn(List.of(customer));
        var result = service.customers(BusinessContext.CUSTOMER_RELATION_CREATE, 1L, "张三");
        assertEquals(1, result.size());
        assertNotEquals("张三", result.getFirst().getCustomerName());
        assertEquals("138****5678", result.getFirst().getMobile());
        verify(contextAccessService).requireAccess(BusinessContext.CUSTOMER_RELATION_CREATE, ReferenceType.CUSTOMER);
    }
}
