package org.dromara.warehouse.unit.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.unit.domain.Unit;
import org.dromara.warehouse.unit.domain.bo.UnitBo;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 计量单位业务规则测试。 */
@ExtendWith(MockitoExtension.class) @MockitoSettings(strictness = Strictness.LENIENT)
class UnitServiceImplTest {
    @Mock UnitMapper unitMapper; @Mock MaterialMapper materialMapper; @Mock DictService dictService; @InjectMocks UnitServiceImpl service;
    @BeforeEach void setUp() { when(dictService.getAllDictByDictType("sys_normal_disable")).thenReturn(Map.of("0", "正常", "1", "停用")); when(unitMapper.insert(any(Unit.class))).thenAnswer(i -> { ((Unit)i.getArgument(0)).setId(1L); return 1; }); }
    @Test void createUnitSucceeds() { UnitBo bo = bo("PCS", 0); assertTrue(service.insertByBo(bo)); assertEquals(1L, bo.getId()); }
    @Test void duplicateUnitCodeFails() { when(unitMapper.countByCode("PCS", null)).thenReturn(1L); assertThrows(ServiceException.class, () -> service.insertByBo(bo("PCS", 0))); }
    @Test void precisionScaleOutOfRangeFails() { assertThrows(ServiceException.class, () -> service.insertByBo(bo("KG", 7))); }
    @Test void disabledUnitCannotBeUsed() { Unit unit = new Unit(); unit.setStatus("1"); when(unitMapper.selectById(1L)).thenReturn(unit); assertThrows(ServiceException.class, () -> service.requireEnabledUnit(1L)); }
    private UnitBo bo(String code, int precision) { UnitBo bo = new UnitBo(); bo.setUnitCode(code); bo.setUnitName(code); bo.setPrecisionScale(precision); bo.setStatus("0"); return bo; }
}
