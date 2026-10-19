package org.dromara.warehouse.material.category.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.warehouse.material.category.domain.MaterialCategory;
import org.dromara.warehouse.material.category.domain.bo.MaterialCategoryBo;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryVo;
import org.dromara.warehouse.material.category.mapper.MaterialCategoryMapper;
import org.dromara.warehouse.material.category.service.IMaterialCategoryService;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 物料分类服务实现。 */
@Service
@RequiredArgsConstructor
public class MaterialCategoryServiceImpl implements IMaterialCategoryService {
    private final MaterialCategoryMapper categoryMapper;
    private final MaterialMapper materialMapper;
    private final WarehouseAccessService accessService;
    private final DictService dictService;

    @Override
    public MaterialCategoryVo queryById(Long id) {
        requireCategory(id);
        return categoryMapper.selectVoById(id);
    }

    @Override
    public List<MaterialCategoryVo> queryTree(MaterialCategoryBo bo) {
        Set<Long> companyIds = accessService.accessibleMaterialCompanyIds();
        if (companyIds.isEmpty()) return List.of();
        LambdaQueryWrapper<MaterialCategory> wrapper = QueryBuilder.lambda(MaterialCategory.class)
            .in(MaterialCategory::getCompanyId, companyIds)
            .eqIfPresent(MaterialCategory::getCompanyId, bo.getCompanyId())
            .eqIfPresent(MaterialCategory::getParentId, bo.getParentId())
            .likeIfText(MaterialCategory::getCategoryCode, bo.getCategoryCode())
            .likeIfText(MaterialCategory::getCategoryName, bo.getCategoryName())
            .eqIfText(MaterialCategory::getStatus, bo.getStatus())
            .orderByAsc(MaterialCategory::getAncestors).orderByAsc(MaterialCategory::getSort)
            .orderByAsc(MaterialCategory::getId).build();
        return buildVoTree(categoryMapper.selectVoList(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(MaterialCategoryBo bo) {
        normalize(bo); accessService.validateMaterialCompany(bo.getCompanyId());
        MaterialCategory entity = BeanUtil.copyProperties(bo, MaterialCategory.class);
        entity.setAncestors(resolveAncestors(bo.getCompanyId(), bo.getParentId(), true));
        validateStatus(bo.getStatus()); validateCode(bo, null);
        try { boolean success = categoryMapper.insert(entity) > 0; if (success) bo.setId(entity.getId()); return success; }
        catch (DuplicateKeyException ex) { throw new ServiceException("同一公司内分类编码已存在"); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(MaterialCategoryBo bo) {
        MaterialCategory old = requireCategory(bo.getId()); normalize(bo);
        if (!old.getCompanyId().equals(bo.getCompanyId())) throw new ServiceException("物料分类不允许变更所属公司");
        if (bo.getId().equals(bo.getParentId())) throw new ServiceException("上级分类不能选择自身");
        MaterialCategory parent = bo.getParentId() == 0L ? null : categoryMapper.selectById(bo.getParentId());
        if (parent != null && (parent.getAncestors() + "," + parent.getId() + ",").contains("," + bo.getId() + ",")) {
            throw new ServiceException("上级分类不能选择当前分类的下级");
        }
        String ancestors = resolveAncestors(bo.getCompanyId(), bo.getParentId(), false);
        validateStatus(bo.getStatus()); validateCode(bo, bo.getId());
        MaterialCategory entity = BeanUtil.copyProperties(bo, MaterialCategory.class); entity.setAncestors(ancestors);
        if (!old.getAncestors().equals(ancestors)) updateChildrenAncestors(old.getId(), old.getAncestors(), ancestors);
        try { return categoryMapper.updateById(entity) > 0; }
        catch (DuplicateKeyException ex) { throw new ServiceException("同一公司内分类编码已存在"); }
    }

    @Override
    public Boolean changeStatus(Long id, String status) {
        requireCategory(id); validateStatus(status);
        MaterialCategory update = new MaterialCategory(); update.setId(id); update.setStatus(status);
        return categoryMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        LinkedHashSet<Long> distinct = new LinkedHashSet<>(ids); distinct.forEach(this::requireCategory);
        if (categoryMapper.existsChild(distinct)) throw new ServiceException("分类下存在未删除子分类，不能删除");
        if (materialMapper.existsByCategoryIds(distinct)) throw new ServiceException("分类下存在未删除物料，不能删除");
        return categoryMapper.deleteByIds(distinct) > 0;
    }

    @Override
    public List<MaterialCategoryOptionVo> selectOptions(Long companyId) {
        Set<Long> companyIds = accessService.accessibleMaterialCompanyIds();
        if (companyId != null) accessService.assertMaterialCompanyAccess(companyId);
        if (companyIds.isEmpty()) return List.of();
        return buildOptionTree(categoryMapper.selectOptions(companyIds, companyId));
    }

    private MaterialCategory requireCategory(Long id) {
        MaterialCategory category = categoryMapper.selectById(id);
        if (category == null) throw new ServiceException("物料分类不存在");
        accessService.assertMaterialCompanyAccess(category.getCompanyId());
        return category;
    }

    /** 校验物料引用的分类属于目标公司且处于启用状态。 */
    @Override
    public MaterialCategory requireEnabledCategory(Long id, Long companyId) {
        MaterialCategory category = categoryMapper.selectById(id);
        if (category == null) throw new ServiceException("物料分类不存在");
        if (!category.getCompanyId().equals(companyId)) throw new ServiceException("物料分类与所属公司不一致");
        accessService.assertMaterialCompanyAccess(companyId);
        if (!SystemConstants.NORMAL.equals(category.getStatus())) throw new ServiceException("物料分类已停用");
        return category;
    }

    private String resolveAncestors(Long companyId, Long parentId, boolean requireEnabled) {
        if (parentId == null || parentId == 0L) return "0";
        MaterialCategory parent = categoryMapper.selectById(parentId);
        if (parent == null) throw new ServiceException("上级物料分类不存在");
        if (!parent.getCompanyId().equals(companyId)) throw new ServiceException("上级分类与所属公司不一致");
        if (requireEnabled && !SystemConstants.NORMAL.equals(parent.getStatus())) throw new ServiceException("上级分类已停用");
        return parent.getAncestors() + StringUtils.SEPARATOR + parent.getId();
    }

    private void updateChildrenAncestors(Long id, String oldAncestors, String newAncestors) {
        List<MaterialCategory> updates = categoryMapper.lambda().findInSet(id, MaterialCategory::getAncestors).list().stream().map(child -> {
            MaterialCategory update = new MaterialCategory(); update.setId(child.getId());
            update.setAncestors(child.getAncestors().replace(oldAncestors, newAncestors)); return update;
        }).toList();
        if (!updates.isEmpty()) categoryMapper.updateBatchById(updates);
    }

    private void normalize(MaterialCategoryBo bo) {
        if (bo.getParentId() == null) bo.setParentId(0L);
        bo.setCategoryCode(StringUtils.trim(bo.getCategoryCode()).toUpperCase());
        bo.setCategoryName(StringUtils.trim(bo.getCategoryName()));
        if (bo.getStatus() == null) bo.setStatus(SystemConstants.NORMAL);
        if (bo.getSort() == null) bo.setSort(0);
    }

    private void validateStatus(String status) {
        if (!dictService.getAllDictByDictType("sys_normal_disable").containsKey(status)) throw new ServiceException("状态不合法");
    }

    private void validateCode(MaterialCategoryBo bo, Long excludeId) {
        if (categoryMapper.countByCode(bo.getCompanyId(), bo.getCategoryCode(), excludeId) > 0) throw new ServiceException("同一公司内分类编码已存在");
    }

    private List<MaterialCategoryVo> buildVoTree(List<MaterialCategoryVo> rows) {
        Map<Long, List<MaterialCategoryVo>> children = rows.stream().collect(Collectors.groupingBy(MaterialCategoryVo::getParentId));
        rows.forEach(row -> row.setChildren(children.get(row.getId())));
        Set<Long> ids = rows.stream().map(MaterialCategoryVo::getId).collect(Collectors.toSet());
        return rows.stream().filter(row -> row.getParentId() == 0L || !ids.contains(row.getParentId())).toList();
    }

    private List<MaterialCategoryOptionVo> buildOptionTree(List<MaterialCategoryOptionVo> rows) {
        Map<Long, List<MaterialCategoryOptionVo>> children = rows.stream().collect(Collectors.groupingBy(MaterialCategoryOptionVo::getParentId));
        rows.forEach(row -> row.setChildren(children.get(row.getId())));
        Set<Long> ids = rows.stream().map(MaterialCategoryOptionVo::getId).collect(Collectors.toSet());
        return rows.stream().filter(row -> row.getParentId() == 0L || !ids.contains(row.getParentId())).toList();
    }
}
