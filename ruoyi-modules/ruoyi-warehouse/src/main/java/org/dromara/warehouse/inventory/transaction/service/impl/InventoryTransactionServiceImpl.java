package org.dromara.warehouse.inventory.transaction.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.api.ProjectReferenceDTO;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.warehouse.inventory.service.IInventoryService;
import org.dromara.warehouse.inventory.transaction.domain.InventoryTransaction;
import org.dromara.warehouse.inventory.transaction.domain.InventoryTransactionType;
import org.dromara.warehouse.inventory.transaction.domain.bo.InventoryTransactionBo;
import org.dromara.warehouse.inventory.transaction.domain.command.InventoryTransactionCommand;
import org.dromara.warehouse.inventory.transaction.domain.vo.InventoryTransactionVo;
import org.dromara.warehouse.inventory.transaction.mapper.InventoryTransactionMapper;
import org.dromara.warehouse.inventory.transaction.service.IInventoryTransactionService;
import org.dromara.warehouse.support.QuantityPrecisionUtils;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 库存流水服务实现。 */
@Service
@RequiredArgsConstructor
public class InventoryTransactionServiceImpl implements IInventoryTransactionService {
    private final InventoryTransactionMapper transactionMapper;
    private final IInventoryService inventoryService;
    private final WarehouseAccessService accessService;
    private final ProjectReferenceService projectReferenceService;

    @Override
    public InventoryTransactionVo queryById(Long id) {
        InventoryTransaction transaction = transactionMapper.selectById(id);
        if (transaction == null) throw new ServiceException("库存流水不存在");
        accessService.requireWarehouse(transaction.getWarehouseId());
        InventoryTransactionVo vo = transactionMapper.selectTransactionById(id);
        if (vo == null) throw new ServiceException("库存流水不存在");
        enrichProjects(List.of(vo));
        return vo;
    }

    @Override
    public PageResult<InventoryTransactionVo> queryPageList(InventoryTransactionBo bo, PageQuery pageQuery) {
        List<Long> warehouseIds = accessService.accessibleWarehouseIds();
        if (warehouseIds.isEmpty()) return PageResult.build(List.of(), 0L);
        IPage<InventoryTransactionVo> page = transactionMapper.selectTransactionPage(pageQuery.build(), bo, warehouseIds);
        enrichProjects(page.getRecords());
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public List<InventoryTransactionVo> queryList(InventoryTransactionBo bo) {
        List<Long> warehouseIds = accessService.accessibleWarehouseIds();
        if (warehouseIds.isEmpty()) return List.of();
        List<InventoryTransactionVo> rows = transactionMapper.selectTransactionList(bo, warehouseIds);
        enrichProjects(rows);
        return rows;
    }

    /** 必须由已开启事务的正式库存业务调用，确保余额变化和流水写入原子提交。 */
    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public Long record(InventoryTransactionCommand command) {
        validate(command);
        inventoryService.validateRelations(command.getWarehouseId(), command.getLocationId(), command.getMaterialId());
        Warehouse warehouse = accessService.requireWarehouse(command.getWarehouseId());
        InventoryTransaction entity = BeanUtil.copyProperties(command, InventoryTransaction.class);
        entity.setTransactionNo(IdGeneratorUtil.nextIdWithPrefix("IT"));
        entity.setCompanyId(warehouse.getCompanyId());
        entity.setProjectId(warehouse.getProjectId());
        entity.setOccurredAt(command.getOccurredAt() == null ? LocalDateTime.now() : command.getOccurredAt());
        entity.setOperatorId(command.getOperatorId() == null ? LoginHelper.getUserId() : command.getOperatorId());
        try {
            transactionMapper.insert(entity);
            return entity.getId();
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("库存流水号冲突，请重试");
        }
    }

    void validate(InventoryTransactionCommand command) {
        if (command == null) throw new ServiceException("库存流水参数不能为空");
        BigDecimal before = required(command.getQuantityBefore(), "变更前数量");
        BigDecimal change = required(command.getQuantityChange(), "变更数量");
        BigDecimal after = required(command.getQuantityAfter(), "变更后数量");
        BigDecimal lockedBefore = required(command.getLockedBefore(), "变更前锁定数量");
        BigDecimal lockedChange = required(command.getLockedChange(), "锁定变化数量");
        BigDecimal lockedAfter = required(command.getLockedAfter(), "变更后锁定数量");
        if (before.signum() < 0 || after.signum() < 0) throw new ServiceException("库存变更前后数量不能为负数");
        if (change.signum() == 0) throw new ServiceException("库存变化数量不能为0");
        if (before.add(change).compareTo(after) != 0) throw new ServiceException("库存变更前、变化量与变更后数量不一致");
        if (lockedBefore.signum() < 0 || lockedAfter.signum() < 0) throw new ServiceException("锁定数量不能为负数");
        if (lockedBefore.add(lockedChange).compareTo(lockedAfter) != 0) throw new ServiceException("锁定数量变更关系不一致");
        if (lockedChange.signum() != 0) throw new ServiceException("Phase 1 库存业务不支持锁定数量变化");
        InventoryTransactionType type;
        try { type = InventoryTransactionType.require(command.getTransactionType()); }
        catch (RuntimeException ex) { throw new ServiceException("库存流水类型不合法"); }
        if (!type.accepts(change.signum())) throw new ServiceException("库存流水类型与数量变化方向不一致");
    }

    private BigDecimal required(BigDecimal value, String label) {
        if (value == null) throw new ServiceException(label + "不能为空");
        if (QuantityPrecisionUtils.exceedsPrecision(value, 6)) throw new ServiceException(label + "小数位不能超过6位");
        return value;
    }

    private void enrichProjects(List<InventoryTransactionVo> rows) {
        List<Long> ids = rows.stream().map(InventoryTransactionVo::getProjectId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return;
        Map<Long, ProjectReferenceDTO> projects = projectReferenceService.queryByIds(ids).stream()
            .collect(Collectors.toMap(ProjectReferenceDTO::id, Function.identity()));
        rows.forEach(row -> { ProjectReferenceDTO project = projects.get(row.getProjectId()); if (project != null) row.setProjectName(project.projectName()); });
    }
}
