package org.dromara.property.charge.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.charge.item.domain.ChargeItem;
import org.dromara.property.charge.item.domain.bo.ChargeItemBo;
import org.dromara.property.charge.item.domain.vo.ChargeItemOptionVo;
import org.dromara.property.charge.item.domain.vo.ChargeItemVo;
import org.dromara.property.charge.item.mapper.ChargeItemMapper;
import org.dromara.property.charge.item.service.IChargeItemService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.scope.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 收费项目服务；详情与写操作始终按数据库记录中的项目归属授权。 */
@Service
@RequiredArgsConstructor
public class ChargeItemServiceImpl implements IChargeItemService {
    private static final Set<String> TYPES = Set.of("PERIODIC", "METER", "DEPOSIT", "TEMPORARY", "PREPAYMENT", "STORED_VALUE", "RESERVATION");
    private static final Set<String> CATEGORIES = Set.of("PROPERTY_FEE", "PARKING_FEE", "WATER_FEE", "ELECTRICITY_FEE", "RENT", "DEPOSIT", "SERVICE_FEE", "OTHER");
    private static final Set<String> OBJECT_TYPES = Set.of("ROOM", "PARKING_SPACE");
    private static final Set<String> STATUSES = Set.of("0", "1");

    private final ChargeItemMapper chargeItemMapper;
    private final ProjectMapper projectMapper;
    private final PropertyProjectScopeService scopeService;

    public ChargeItemVo queryById(Long id) {
        ChargeItem current = require(id);
        access(current.getProjectId(), ProjectBusinessAction.READ);
        ChargeItemVo vo = chargeItemMapper.selectVoById(id);
        enrichProjectNames(List.of(vo));
        return vo;
    }

    public PageResult<ChargeItemVo> queryPage(ChargeItemBo bo, PageQuery pageQuery) {
        PropertyProjectScope scope = scope(ProjectBusinessAction.READ);
        if (scope.type() == PropertyProjectScope.Type.NONE) return PageResult.build(List.of(), 0L);
        Page<ChargeItemVo> page = chargeItemMapper.selectVoPage(pageQuery.build(), query(bo, scope));
        enrichProjectNames(page.getRecords());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    public List<ChargeItemVo> queryExport(ChargeItemBo bo) {
        PropertyProjectScope scope = scope(ProjectBusinessAction.EXPORT);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        List<ChargeItemVo> rows = chargeItemMapper.selectVoList(query(bo, scope));
        enrichProjectNames(rows);
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean insert(ChargeItemBo bo) {
        access(bo.getProjectId(), ProjectBusinessAction.WRITE);
        Project project = projectMapper.selectById(bo.getProjectId());
        if (project == null || !"0".equals(project.getStatus())) throw new ServiceException("项目不存在或已停用");
        validateFields(bo.getChargeType(), bo.getChargeCategory(), bo.getChargeObjectType(), bo.getStatus());
        validateCode(bo.getProjectId(), bo.getItemCode());
        ChargeItem item = BeanUtil.copyProperties(bo, ChargeItem.class);
        if (item.getStatus() == null) item.setStatus("0");
        if (item.getSort() == null) item.setSort(0);
        try {
            return chargeItemMapper.insert(item) > 0;
        } catch (DuplicateKeyException e) {
            throw new ServiceException("同一项目下收费项目编码已存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean update(ChargeItemBo bo) {
        ChargeItem current = locked(bo.getId());
        access(current.getProjectId(), ProjectBusinessAction.WRITE);
        if (bo.getProjectId() != null && !bo.getProjectId().equals(current.getProjectId())) throw new ServiceException("所属项目不允许修改");
        if (bo.getItemCode() != null && !bo.getItemCode().equals(current.getItemCode())) throw new ServiceException("收费项目编码不允许修改");
        validateFields(bo.getChargeType(), bo.getChargeCategory(), bo.getChargeObjectType(), bo.getStatus());
        ChargeItem update = BeanUtil.copyProperties(bo, ChargeItem.class);
        update.setProjectId(current.getProjectId());
        update.setItemCode(current.getItemCode());
        return chargeItemMapper.updateById(update) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, String status) {
        ChargeItem current = locked(id);
        access(current.getProjectId(), ProjectBusinessAction.WRITE);
        validateStatus(status);
        ChargeItem update = new ChargeItem();
        update.setId(id);
        update.setStatus(status);
        return chargeItemMapper.updateById(update) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Collection<Long> ids) {
        for (Long id : ids) {
            ChargeItem current = locked(id);
            access(current.getProjectId(), ProjectBusinessAction.WRITE);
            checkChargeItemCanDelete(current);
        }
        return chargeItemMapper.deleteByIds(ids) > 0;
    }

    public List<ChargeItemOptionVo> options(ChargeItemBo bo) {
        PropertyProjectScope scope = scope(ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        List<ChargeItem> rows = chargeItemMapper.selectList(QueryBuilder.lambda(ChargeItem.class)
            .select(ChargeItem::getId, ChargeItem::getProjectId, ChargeItem::getItemCode, ChargeItem::getItemName,
                ChargeItem::getChargeType, ChargeItem::getChargeCategory, ChargeItem::getChargeObjectType)
            .eqIfPresent(ChargeItem::getProjectId, bo.getProjectId()).eqIfText(ChargeItem::getChargeType, bo.getChargeType())
            .eqIfText(ChargeItem::getChargeCategory, bo.getChargeCategory()).eqIfText(ChargeItem::getChargeObjectType, bo.getChargeObjectType())
            .eq(ChargeItem::getStatus, "0").in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS, ChargeItem::getProjectId, scope.projectIds())
            .orderByAsc(ChargeItem::getSort).orderByDesc(ChargeItem::getCreateTime).build());
        return BeanUtil.copyToList(rows, ChargeItemOptionVo.class);
    }

    public List<ProjectOptionVo> projectOptions() {
        PropertyProjectScope scope = scope(ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) return List.of();
        if (scope.type() == PropertyProjectScope.Type.ALL) return projectMapper.selectOptions();
        return projectMapper.selectVoList(QueryBuilder.lambda(Project.class)
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getProjectShortName)
            .in(Project::getId, scope.projectIds()).eq(Project::getStatus, "0")
            .orderByAsc(Project::getSort).orderByAsc(Project::getId).build(), ProjectOptionVo.class);
    }

    private LambdaQueryWrapper<ChargeItem> query(ChargeItemBo bo, PropertyProjectScope scope) {
        return QueryBuilder.lambda(ChargeItem.class).eqIfPresent(ChargeItem::getProjectId, bo.getProjectId())
            .likeIfText(ChargeItem::getItemCode, bo.getItemCode()).likeIfText(ChargeItem::getItemName, bo.getItemName())
            .eqIfText(ChargeItem::getChargeType, bo.getChargeType()).eqIfText(ChargeItem::getChargeCategory, bo.getChargeCategory())
            .eqIfText(ChargeItem::getChargeObjectType, bo.getChargeObjectType()).eqIfText(ChargeItem::getStatus, bo.getStatus())
            .in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS, ChargeItem::getProjectId, scope.projectIds())
            .orderByAsc(ChargeItem::getSort).orderByDesc(ChargeItem::getCreateTime).build();
    }

    private ChargeItem require(Long id) {
        ChargeItem item = chargeItemMapper.selectById(id);
        if (item == null) throw new ServiceException("收费项目不存在");
        return item;
    }

    private ChargeItem locked(Long id) {
        ChargeItem item = chargeItemMapper.selectActiveByIdForUpdate(id);
        if (item == null) throw new ServiceException("收费项目不存在");
        return item;
    }

    private void validateCode(Long projectId, String itemCode) {
        if (chargeItemMapper.countActiveCode(projectId, itemCode, null) > 0) throw new ServiceException("同一项目下收费项目编码已存在");
    }

    private void validateFields(String type, String category, String objectType, String status) {
        if (!TYPES.contains(type)) throw new ServiceException("收费类型不是有效值");
        if (!CATEGORIES.contains(category)) throw new ServiceException("收费分类不是有效值");
        if (!OBJECT_TYPES.contains(objectType)) throw new ServiceException("收费载体只允许房屋或车位");
        validateStatus(status == null ? "0" : status);
    }

    private void validateStatus(String status) {
        if (!STATUSES.contains(status)) throw new ServiceException("状态不是有效值");
    }

    /** 已被有效计费规则引用的收费项目不可删除。 */
    protected void checkChargeItemCanDelete(ChargeItem item) {
        if (chargeItemMapper.existsActiveChargeRule(item.getId())) throw new ServiceException("收费项目已被计费规则引用，不能删除");
        if (chargeItemMapper.existsActiveChargeBinding(item.getId())) throw new ServiceException("收费项目已被房屋收费设置引用，不能删除");
    }

    private void enrichProjectNames(Collection<ChargeItemVo> rows) {
        Set<Long> ids = rows.stream().map(ChargeItemVo::getProjectId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return;
        Map<Long, Project> projects = projectMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(Project::getId, Function.identity()));
        rows.forEach(row -> { Project project = projects.get(row.getProjectId()); if (project != null) row.setProjectName(project.getProjectName()); });
    }

    private PropertyProjectScope scope(ProjectBusinessAction action) {
        return scopeService.resolveBusinessScope(PropertyBusinessDomain.FINANCE, action);
    }

    private void access(Long projectId, ProjectBusinessAction action) {
        scopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.FINANCE, action);
    }
}
