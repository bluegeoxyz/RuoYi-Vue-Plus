package org.dromara.warehouse.unit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.unit.domain.Unit;
import org.dromara.warehouse.unit.domain.bo.UnitBo;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.dromara.warehouse.unit.domain.vo.UnitVo;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.unit.service.IUnitService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** 计量单位服务实现。 */
@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements IUnitService {
    private final UnitMapper unitMapper;
    private final MaterialMapper materialMapper;
    private final DictService dictService;

    @Override public UnitVo queryById(Long id) { requireUnit(id); return unitMapper.selectVoById(id); }

    @Override
    public PageResult<UnitVo> queryPageList(UnitBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Unit> wrapper = QueryBuilder.lambda(Unit.class)
            .likeIfText(Unit::getUnitCode, bo.getUnitCode()).likeIfText(Unit::getUnitName, bo.getUnitName())
            .eqIfText(Unit::getStatus, bo.getStatus()).orderByAsc(Unit::getSort).orderByDesc(Unit::getId).build();
        Page<UnitVo> result = unitMapper.selectVoPage(pageQuery.build(), wrapper);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public Boolean insertByBo(UnitBo bo) {
        normalize(bo); validateBeforeSave(bo, null); Unit entity = BeanUtil.copyProperties(bo, Unit.class);
        try { boolean success = unitMapper.insert(entity) > 0; if (success) bo.setId(entity.getId()); return success; }
        catch (DuplicateKeyException ex) { throw new ServiceException("单位编码已存在"); }
    }

    @Override
    public Boolean updateByBo(UnitBo bo) {
        requireUnit(bo.getId()); normalize(bo); validateBeforeSave(bo, bo.getId());
        try { return unitMapper.updateById(BeanUtil.copyProperties(bo, Unit.class)) > 0; }
        catch (DuplicateKeyException ex) { throw new ServiceException("单位编码已存在"); }
    }

    @Override
    public Boolean changeStatus(Long id, String status) {
        requireUnit(id); validateStatus(status); Unit update = new Unit(); update.setId(id); update.setStatus(status);
        return unitMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        LinkedHashSet<Long> distinct = new LinkedHashSet<>(ids); distinct.forEach(this::requireUnit);
        if (materialMapper.existsByUnitIds(distinct)) throw new ServiceException("单位已被未删除物料引用，不能删除");
        return unitMapper.deleteByIds(distinct) > 0;
    }

    @Override public List<UnitOptionVo> selectOptions() { return unitMapper.selectOptions(); }

    @Override
    public Unit requireEnabledUnit(Long id) {
        Unit unit = requireUnit(id);
        if (!SystemConstants.NORMAL.equals(unit.getStatus())) throw new ServiceException("基础单位已停用");
        return unit;
    }

    private Unit requireUnit(Long id) {
        Unit unit = unitMapper.selectById(id); if (unit == null) throw new ServiceException("计量单位不存在"); return unit;
    }

    private void normalize(UnitBo bo) {
        bo.setUnitCode(StringUtils.trim(bo.getUnitCode()).toUpperCase()); bo.setUnitName(StringUtils.trim(bo.getUnitName()));
        bo.setSymbol(StringUtils.trim(bo.getSymbol()));
        if (bo.getStatus() == null) bo.setStatus(SystemConstants.NORMAL); if (bo.getSort() == null) bo.setSort(0);
    }

    private void validateBeforeSave(UnitBo bo, Long excludeId) {
        validateStatus(bo.getStatus());
        if (bo.getPrecisionScale() == null || bo.getPrecisionScale() < 0 || bo.getPrecisionScale() > 6) throw new ServiceException("小数精度必须在0到6之间");
        if (unitMapper.countByCode(bo.getUnitCode(), excludeId) > 0) throw new ServiceException("单位编码已存在");
    }

    private void validateStatus(String status) {
        if (!dictService.getAllDictByDictType("sys_normal_disable").containsKey(status)) throw new ServiceException("状态不合法");
    }
}
