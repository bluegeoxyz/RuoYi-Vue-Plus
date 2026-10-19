package org.dromara.warehouse.material.service.impl;

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
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.warehouse.material.category.domain.MaterialCategory;
import org.dromara.warehouse.material.category.mapper.MaterialCategoryMapper;
import org.dromara.warehouse.material.category.service.IMaterialCategoryService;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.domain.bo.MaterialBo;
import org.dromara.warehouse.material.domain.vo.MaterialOptionVo;
import org.dromara.warehouse.material.domain.vo.MaterialVo;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.material.service.IMaterialService;
import org.dromara.warehouse.support.QuantityPrecisionUtils;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.transaction.mapper.InventoryTransactionMapper;
import org.dromara.warehouse.unit.domain.Unit;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.unit.service.IUnitService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 物料服务实现。 */
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements IMaterialService {
    private final MaterialMapper materialMapper;
    private final MaterialCategoryMapper categoryMapper;
    private final UnitMapper unitMapper;
    private final IMaterialCategoryService categoryService;
    private final IUnitService unitService;
    private final WarehouseAccessService accessService;
    private final DictService dictService;
    private final InventoryMapper inventoryMapper;
    private final InventoryTransactionMapper transactionMapper;

    @Override
    public MaterialVo queryById(Long id) {
        requireMaterial(id);
        MaterialVo vo = materialMapper.selectVoById(id);
        enrich(List.of(vo));
        return vo;
    }

    @Override
    public PageResult<MaterialVo> queryPageList(MaterialBo bo, PageQuery pageQuery) {
        Set<Long> companyIds = accessService.accessibleMaterialCompanyIds();
        if (companyIds.isEmpty()) return PageResult.build(List.of(), 0L);
        LambdaQueryWrapper<Material> wrapper =
                QueryBuilder.lambda(Material.class)
                        .in(Material::getCompanyId, companyIds)
                        .eqIfPresent(Material::getCompanyId, bo.getCompanyId())
                        .eqIfPresent(Material::getCategoryId, bo.getCategoryId())
                        .likeIfText(Material::getMaterialCode, bo.getMaterialCode())
                        .likeIfText(Material::getMaterialName, bo.getMaterialName())
                        .eqIfText(Material::getStatus, bo.getStatus())
                        .orderByAsc(Material::getSort)
                        .orderByDesc(Material::getId)
                        .build();
        Page<MaterialVo> result = materialMapper.selectVoPage(pageQuery.build(), wrapper);
        enrich(result.getRecords());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public Boolean insertByBo(MaterialBo bo) {
        normalize(bo);
        validateBeforeSave(bo, null);
        Material entity = BeanUtil.copyProperties(bo, Material.class);
        try {
            boolean success = materialMapper.insert(entity) > 0;
            if (success) bo.setId(entity.getId());
            return success;
        } catch (DuplicateKeyException ex) {
            throw duplicateException(bo);
        }
    }

    @Override
    public Boolean updateByBo(MaterialBo bo) {
        requireMaterial(bo.getId());
        normalize(bo);
        validateBeforeSave(bo, bo.getId());
        try {
            return materialMapper.updateById(BeanUtil.copyProperties(bo, Material.class)) > 0;
        } catch (DuplicateKeyException ex) {
            throw duplicateException(bo);
        }
    }

    @Override
    public Boolean changeStatus(Long id, String status) {
        requireMaterial(id);
        validateStatus(status);
        Material update = new Material();
        update.setId(id);
        update.setStatus(status);
        return materialMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        LinkedHashSet<Long> distinct = new LinkedHashSet<>(ids);
        distinct.forEach(this::requireMaterial);
        validateDeleteReferences(distinct);
        return materialMapper.deleteByIds(distinct) > 0;
    }

    @Override
    public List<MaterialOptionVo> selectOptions(Long companyId) {
        Set<Long> companyIds = accessService.accessibleMaterialCompanyIds();
        if (companyId != null) accessService.assertMaterialCompanyAccess(companyId);
        if (companyIds.isEmpty()) return List.of();
        return materialMapper.selectOptions(companyIds, companyId);
    }

    @Override
    public List<SysDeptVo> selectCompanyOptions() {
        return accessService.selectMaterialCompanyOptions();
    }

    private Material requireMaterial(Long id) {
        Material material = materialMapper.selectById(id);
        if (material == null) throw new ServiceException("物料不存在");
        accessService.assertMaterialCompanyAccess(material.getCompanyId());
        return material;
    }

    private void normalize(MaterialBo bo) {
        bo.setMaterialCode(StringUtils.trim(bo.getMaterialCode()).toUpperCase());
        bo.setMaterialName(StringUtils.trim(bo.getMaterialName()));
        bo.setSpecification(StringUtils.trim(bo.getSpecification()));
        bo.setModel(StringUtils.trim(bo.getModel()));
        bo.setBarcode(StringUtils.trim(bo.getBarcode()));
        if (StringUtils.isBlank(bo.getBarcode())) bo.setBarcode(null);
        if (bo.getSafetyStock() == null) bo.setSafetyStock(BigDecimal.ZERO);
        if (bo.getStatus() == null) bo.setStatus(SystemConstants.NORMAL);
        if (bo.getSort() == null) bo.setSort(0);
    }

    private void validateBeforeSave(MaterialBo bo, Long excludeId) {
        accessService.validateMaterialCompany(bo.getCompanyId());
        categoryService.requireEnabledCategory(bo.getCategoryId(), bo.getCompanyId());
        unitService.requireEnabledUnit(bo.getBaseUnitId());
        validateStatus(bo.getStatus());
        if (bo.getSafetyStock().signum() < 0 || QuantityPrecisionUtils.exceedsPrecision(bo.getSafetyStock(), 6))
            throw new ServiceException("安全库存必须为非负且小数位不超过6位");
        if (materialMapper.countByCode(bo.getCompanyId(), bo.getMaterialCode(), excludeId) > 0)
            throw new ServiceException("同一公司内物料编码已存在");
        if (bo.getBarcode() != null
                && materialMapper.countByBarcode(bo.getCompanyId(), bo.getBarcode(), excludeId) > 0)
            throw new ServiceException("同一公司内条码已存在");
    }

    private void validateStatus(String status) {
        if (!dictService.getAllDictByDictType("sys_normal_disable").containsKey(status))
            throw new ServiceException("状态不合法");
    }

    private void validateDeleteReferences(Collection<Long> ids) {
        if (transactionMapper.existsByMaterialIds(ids))
            throw new ServiceException("物料存在库存流水，不能删除，请停用物料");
        if (inventoryMapper.existsByMaterialIds(ids))
            throw new ServiceException("物料存在库存余额，不能删除，请停用物料");
    }

    private ServiceException duplicateException(MaterialBo bo) {
        if (bo.getBarcode() != null
                && materialMapper.countByBarcode(bo.getCompanyId(), bo.getBarcode(), bo.getId())
                        > 0) return new ServiceException("同一公司内条码已存在");
        return new ServiceException("同一公司内物料编码已存在");
    }

    private void enrich(List<MaterialVo> rows) {
        List<Long> categoryIds = rows.stream().map(MaterialVo::getCategoryId).distinct().toList();
        List<Long> unitIds = rows.stream().map(MaterialVo::getBaseUnitId).distinct().toList();
        Map<Long, MaterialCategory> categories =
                categoryIds.isEmpty()
                        ? Map.of()
                        : categoryMapper.selectByIds(categoryIds).stream()
                                .collect(
                                        Collectors.toMap(
                                                MaterialCategory::getId, Function.identity()));
        Map<Long, Unit> units =
                unitIds.isEmpty()
                        ? Map.of()
                        : unitMapper.selectByIds(unitIds).stream()
                                .collect(Collectors.toMap(Unit::getId, Function.identity()));
        rows.forEach(
                row -> {
                    MaterialCategory category = categories.get(row.getCategoryId());
                    if (category != null) row.setCategoryName(category.getCategoryName());
                    Unit unit = units.get(row.getBaseUnitId());
                    if (unit != null) {
                        row.setBaseUnitName(unit.getUnitName());
                        row.setBaseUnitSymbol(unit.getSymbol());
                        row.setPrecisionScale(unit.getPrecisionScale());
                    }
                });
    }
}
