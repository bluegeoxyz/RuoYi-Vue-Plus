package org.dromara.property.charge.item.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.charge.item.domain.ChargeItem;
import org.dromara.property.charge.item.domain.bo.ChargeItemBo;
import org.dromara.property.charge.item.mapper.ChargeItemMapper;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.scope.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeItemServiceSecurityTest {
    @Mock private ChargeItemMapper mapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private PropertyProjectScopeService scopeService;
    private ChargeItemServiceImpl service;

    @BeforeEach void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "charge-item-test"), ChargeItem.class);
        service = new ChargeItemServiceImpl(mapper, projectMapper, scopeService);
    }

    @Test void noneScopeMustReturnEmptyWithoutQueryingStorage() {
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.FINANCE, ProjectBusinessAction.READ)).thenReturn(PropertyProjectScope.none());
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.FINANCE, ProjectBusinessAction.EXPORT)).thenReturn(PropertyProjectScope.none());
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.FINANCE, ProjectBusinessAction.OPTION)).thenReturn(PropertyProjectScope.none());
        assertEquals(0, service.queryPage(new ChargeItemBo(), new org.dromara.common.mybatis.core.page.PageQuery()).getTotal());
        assertTrue(service.queryExport(new ChargeItemBo()).isEmpty());
        assertTrue(service.options(new ChargeItemBo()).isEmpty());
        assertTrue(service.projectOptions().isEmpty());
        verifyNoInteractions(mapper, projectMapper);
    }

    @Test void detailMustAuthorizePersistedProject() {
        when(mapper.selectById(10L)).thenReturn(item(10L, 101L, "FEE"));
        doThrow(new ServiceException("无项目业务访问权限")).when(scopeService)
            .assertBusinessAccess(101L, PropertyBusinessDomain.FINANCE, ProjectBusinessAction.READ);
        assertThrows(ServiceException.class, () -> service.queryById(10L));
        verify(mapper, never()).selectVoById(anyLong());
    }

    @Test void updateMustRejectProjectAndCodeChangesUsingPersistedScope() {
        when(mapper.selectActiveByIdForUpdate(10L)).thenReturn(item(10L, 101L, "FEE"));
        ChargeItemBo forgedProject = validBo(); forgedProject.setId(10L); forgedProject.setProjectId(202L); forgedProject.setItemCode("FEE");
        assertEquals("所属项目不允许修改", assertThrows(ServiceException.class, () -> service.update(forgedProject)).getMessage());
        ChargeItemBo forgedCode = validBo(); forgedCode.setId(10L); forgedCode.setProjectId(101L); forgedCode.setItemCode("OTHER");
        assertEquals("收费项目编码不允许修改", assertThrows(ServiceException.class, () -> service.update(forgedCode)).getMessage());
        verify(scopeService, times(2)).assertBusinessAccess(101L, PropertyBusinessDomain.FINANCE, ProjectBusinessAction.WRITE);
        verify(mapper, never()).updateById(any(ChargeItem.class));
    }

    @Test void ownerAndHouseholdMustBeRejected() {
        Project project = new Project(); project.setId(101L); project.setStatus("0");
        when(projectMapper.selectById(101L)).thenReturn(project);
        ChargeItemBo owner = validBo(); owner.setProjectId(101L); owner.setItemCode("A"); owner.setChargeObjectType("OWNER");
        assertTrue(assertThrows(ServiceException.class, () -> service.insert(owner)).getMessage().contains("房屋或车位"));
        ChargeItemBo household = validBo(); household.setProjectId(101L); household.setItemCode("B"); household.setChargeObjectType("HOUSEHOLD");
        assertTrue(assertThrows(ServiceException.class, () -> service.insert(household)).getMessage().contains("房屋或车位"));
        verify(mapper, never()).insert(any(ChargeItem.class));
    }

    @Test void batchDeleteMustAuthorizeEveryPersistedProjectBeforeDelete() {
        when(mapper.selectActiveByIdForUpdate(10L)).thenReturn(item(10L, 101L, "A"));
        when(mapper.selectActiveByIdForUpdate(20L)).thenReturn(item(20L, 202L, "B"));
        doAnswer(invocation -> {
            if (Long.valueOf(202L).equals(invocation.getArgument(0))) throw new ServiceException("无项目业务访问权限");
            return null;
        }).when(scopeService).assertBusinessAccess(anyLong(), eq(PropertyBusinessDomain.FINANCE), eq(ProjectBusinessAction.WRITE));
        assertThrows(ServiceException.class, () -> service.delete(List.of(10L, 20L)));
        verify(mapper, never()).deleteByIds(anyCollection());
    }

    private static ChargeItemBo validBo() {
        ChargeItemBo bo = new ChargeItemBo();
        bo.setItemName("物业费"); bo.setChargeType("PERIODIC"); bo.setChargeCategory("PROPERTY_FEE");
        bo.setChargeObjectType("ROOM"); bo.setStatus("0");
        return bo;
    }

    private static ChargeItem item(Long id, Long projectId, String code) {
        ChargeItem item = new ChargeItem(); item.setId(id); item.setProjectId(projectId); item.setItemCode(code); return item;
    }
}
