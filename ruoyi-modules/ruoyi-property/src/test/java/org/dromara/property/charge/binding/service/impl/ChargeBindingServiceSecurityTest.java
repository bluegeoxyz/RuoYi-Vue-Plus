package org.dromara.property.charge.binding.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.charge.binding.domain.ChargeBinding;
import org.dromara.property.charge.binding.domain.bo.ChargeBindingBo;
import org.dromara.property.charge.binding.mapper.ChargeBindingMapper;
import org.dromara.property.charge.item.domain.ChargeItem;
import org.dromara.property.charge.item.mapper.ChargeItemMapper;
import org.dromara.property.charge.rule.domain.ChargeRule;
import org.dromara.property.charge.rule.mapper.ChargeRuleMapper;
import org.dromara.property.project.mapper.ProjectMapper;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.dromara.property.charge.binding.domain.bo.ChargeBindingBatchBo;
import java.util.List;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeBindingServiceSecurityTest {
 @Mock ChargeBindingMapper mapper; @Mock RoomMapper roomMapper; @Mock ChargeItemMapper itemMapper; @Mock ChargeRuleMapper ruleMapper; @Mock ProjectMapper projectMapper; @Mock BuildingMapper buildingMapper; @Mock BuildingUnitMapper unitMapper; @Mock PropertyProjectScopeService scopeService; ChargeBindingServiceImpl service;
 @BeforeEach void setUp(){TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),"chargeBinding"),ChargeBinding.class);PlatformTransactionManager tx=new AbstractPlatformTransactionManager(){protected Object doGetTransaction(){return new Object();}protected void doBegin(Object t,TransactionDefinition d){}protected void doCommit(DefaultTransactionStatus s){}protected void doRollback(DefaultTransactionStatus s){}};service=new ChargeBindingServiceImpl(mapper,roomMapper,itemMapper,ruleMapper,projectMapper,buildingMapper,unitMapper,scopeService,tx);}
 @Test void detailAuthorizesProjectDerivedFromPersistedRoom(){when(mapper.selectById(9L)).thenReturn(binding());when(roomMapper.selectById(7L)).thenReturn(room(101L));doThrow(new ServiceException("deny")).when(scopeService).assertBusinessAccess(101L,PropertyBusinessDomain.FINANCE,ProjectBusinessAction.READ);assertThrows(ServiceException.class,()->service.queryById(9L));verify(mapper,never()).selectVoById(9L);}
 @Test void insertRejectsNonRoomBeforeWrite(){ChargeBindingBo bo=validBo();bo.setObjectType("PARKING_SPACE");assertEquals("当前仅支持房屋收费设置",assertThrows(ServiceException.class,()->service.insert(bo)).getMessage());verify(mapper,never()).insert(any(ChargeBinding.class));}
 @Test void insertDerivesProjectFromRoomAndRejectsForgery(){ChargeBindingBo bo=validBo();bo.setProjectId(202L);when(roomMapper.selectActiveByIdForUpdate(7L)).thenReturn(room(101L));assertEquals("所属项目与房屋不一致",assertThrows(ServiceException.class,()->service.insert(bo)).getMessage());verify(scopeService).assertBusinessAccess(101L,PropertyBusinessDomain.FINANCE,ProjectBusinessAction.WRITE);verify(itemMapper,never()).selectById(anyLong());}
 @Test void insertRejectsRuleForAnotherItem(){ChargeBindingBo bo=validBo();when(roomMapper.selectActiveByIdForUpdate(7L)).thenReturn(room(101L));when(itemMapper.selectById(11L)).thenReturn(item(101L));ChargeRule r=rule(101L);r.setChargeItemId(99L);when(ruleMapper.selectById(12L)).thenReturn(r);assertEquals("计费规则与收费项目不匹配",assertThrows(ServiceException.class,()->service.insert(bo)).getMessage());}
 @Test void insertRejectsClosedIntervalOverlap(){ChargeBindingBo bo=validBo();stubValid();when(mapper.overlaps(101L,7L,11L,bo.getEffectiveStartDate(),bo.getEffectiveEndDate(),null)).thenReturn(true);assertEquals("同一房屋同一收费项目的有效期不能重叠",assertThrows(ServiceException.class,()->service.insert(bo)).getMessage());verify(mapper).lockItemBindings(101L,7L,11L);verify(mapper,never()).insert(any(ChargeBinding.class));}
 @Test void updateRejectsImmutableRule(){when(mapper.selectActiveByIdForUpdate(9L)).thenReturn(binding());when(roomMapper.selectActiveByIdForUpdate(7L)).thenReturn(room(101L));ChargeBindingBo bo=validBo();bo.setId(9L);bo.setChargeRuleId(13L);assertTrue(assertThrows(ServiceException.class,()->service.update(bo)).getMessage().contains("不允许修改"));verify(itemMapper,never()).selectById(anyLong());}
 @Test void insertRejectsDateOutsideRule(){ChargeBindingBo bo=validBo();bo.setEffectiveStartDate(LocalDate.of(2025,12,31));when(roomMapper.selectActiveByIdForUpdate(7L)).thenReturn(room(101L));when(itemMapper.selectById(11L)).thenReturn(item(101L));when(ruleMapper.selectById(12L)).thenReturn(rule(101L));assertEquals("收费设置有效期必须位于计费规则有效期内",assertThrows(ServiceException.class,()->service.insert(bo)).getMessage());}
 @Test void batchRejectsCrossProjectBeforeAnyInsert(){Room a=room(101L),b=room(202L);b.setId(8L);when(roomMapper.selectBatchIds(List.of(7L,8L))).thenReturn(List.of(a,b));assertThrows(ServiceException.class,()->service.batchCreate(batch(List.of(7L,8L))));verify(mapper,never()).insert(any(ChargeBinding.class));}
 @Test void batchAllowsOtherChargeItemAndReturnsPartialOverlap(){Room a=room(101L),b=room(101L);b.setId(8L);when(roomMapper.selectBatchIds(List.of(7L,8L))).thenReturn(List.of(a,b));when(itemMapper.selectById(11L)).thenReturn(item(101L));when(ruleMapper.selectById(12L)).thenReturn(rule(101L));when(roomMapper.selectActiveByIdForUpdate(7L)).thenReturn(a);when(roomMapper.selectActiveByIdForUpdate(8L)).thenReturn(b);when(mapper.overlaps(eq(101L),anyLong(),eq(11L),any(),any(),isNull())).thenReturn(true,false);when(mapper.insert(any(ChargeBinding.class))).thenReturn(1);var result=service.batchCreate(batch(List.of(7L,8L)));assertEquals(2,result.getTotalCount());assertEquals(1,result.getSuccessCount());assertEquals(1,result.getFailedCount());assertEquals("OVERLAP",result.getFailures().get(0).getReasonCode());}
 private static ChargeBindingBatchBo batch(List<Long> rooms){ChargeBindingBatchBo b=new ChargeBindingBatchBo();b.setProjectId(101L);b.setRoomIds(rooms);b.setChargeItemId(11L);b.setChargeRuleId(12L);b.setEffectiveStartDate(LocalDate.of(2026,1,1));b.setEffectiveEndDate(LocalDate.of(2026,6,30));return b;}
 private void stubValid(){when(roomMapper.selectActiveByIdForUpdate(7L)).thenReturn(room(101L));when(itemMapper.selectById(11L)).thenReturn(item(101L));when(ruleMapper.selectById(12L)).thenReturn(rule(101L));}
 private static ChargeBindingBo validBo(){ChargeBindingBo b=new ChargeBindingBo();b.setProjectId(101L);b.setObjectType("ROOM");b.setObjectId(7L);b.setChargeItemId(11L);b.setChargeRuleId(12L);b.setEffectiveStartDate(LocalDate.of(2026,1,1));b.setEffectiveEndDate(LocalDate.of(2026,6,30));b.setStatus("0");return b;}
 private static ChargeBinding binding(){ChargeBinding b=new ChargeBinding();b.setId(9L);b.setProjectId(101L);b.setObjectType("ROOM");b.setObjectId(7L);b.setChargeItemId(11L);b.setChargeRuleId(12L);b.setEffectiveStartDate(LocalDate.of(2026,1,1));b.setStatus("0");return b;}
 private static Room room(long p){Room r=new Room();r.setId(7L);r.setProjectId(p);return r;}
 private static ChargeItem item(long p){ChargeItem i=new ChargeItem();i.setId(11L);i.setProjectId(p);i.setChargeObjectType("ROOM");i.setStatus("0");return i;}
 private static ChargeRule rule(long p){ChargeRule r=new ChargeRule();r.setId(12L);r.setProjectId(p);r.setChargeItemId(11L);r.setEffectiveStartDate(LocalDate.of(2026,1,1));r.setEffectiveEndDate(LocalDate.of(2026,12,31));r.setStatus("0");return r;}
}
