package org.dromara.property.charge.receivable.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.charge.receivable.domain.*;
import org.dromara.property.charge.receivable.domain.bo.*;
import org.dromara.property.charge.receivable.domain.vo.*;
import org.dromara.property.charge.receivable.mapper.*;
import org.dromara.property.charge.receivable.service.IReceivableService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.scope.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceivableServiceImpl implements IReceivableService {
  private final ReceivableMapper mapper;
  private final ReceivableBatchMapper batchMapper;
  private final ProjectMapper projectMapper;
  private final PropertyProjectScopeService scopeService;

  public PageResult<ReceivableVo> queryPage(ReceivableQueryBo bo, PageQuery pq) {
    requireProject(bo.getProjectId(), ProjectBusinessAction.READ);
    Page<Receivable> p = mapper.selectPage(pq.build(), query(bo));
    List<ReceivableVo> rows = BeanUtil.copyToList(p.getRecords(), ReceivableVo.class);
    enrich(rows);
    return PageResult.build(rows, p.getTotal());
  }

  public ReceivableVo queryById(Long id) {
    Receivable row = require(id);
    access(row.getProjectId(), ProjectBusinessAction.READ);
    ReceivableVo vo = BeanUtil.copyProperties(row, ReceivableVo.class);
    enrich(List.of(vo));
    return vo;
  }

  public ReceivableSummaryVo summary(ReceivableQueryBo bo) {
    requireProject(bo.getProjectId(), ProjectBusinessAction.READ);
    BigDecimal receivable = BigDecimal.ZERO,
        paid = BigDecimal.ZERO,
        outstanding = BigDecimal.ZERO,
        overdue = BigDecimal.ZERO;
    for (Receivable r : mapper.selectList(query(bo))) {
      if ("CANCELLED".equals(r.getPaymentStatus())) continue;
      BigDecimal remain = r.getReceivableAmount().subtract(r.getPaidAmount());
      receivable = receivable.add(r.getReceivableAmount());
      paid = paid.add(r.getPaidAmount());
      outstanding = outstanding.add(remain);
      if (isOverdue(r, LocalDate.now())) overdue = overdue.add(remain);
    }
    return new ReceivableSummaryVo(
        money(receivable), money(paid), money(outstanding), money(overdue));
  }

  public List<ReceivableExportVo> export(ReceivableQueryBo bo) {
    requireProject(bo.getProjectId(), ProjectBusinessAction.EXPORT);
    List<ReceivableVo> rows = BeanUtil.copyToList(mapper.selectList(query(bo)), ReceivableVo.class);
    enrich(rows);
    return rows.stream()
        .map(
            v -> {
              ReceivableExportVo e = BeanUtil.copyProperties(v, ReceivableExportVo.class);
              e.setOverdueLabel(Boolean.TRUE.equals(v.getOverdue()) ? "逾期" : "未逾期");
              return e;
            })
        .toList();
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean cancel(Long id, ReceivableCancelBo bo) {
    Receivable old = mapper.selectActiveByIdForUpdate(id);
    if (old == null) throw new ServiceException("应收不存在");
    access(old.getProjectId(), ProjectBusinessAction.WRITE);
    if ("CANCELLED".equals(old.getPaymentStatus())) throw new ServiceException("应收已作废，不能重复作废");
    if (!"UNPAID".equals(old.getPaymentStatus()) || old.getPaidAmount().signum() != 0)
      throw new ServiceException("已发生资金核销的应收不能直接作废");
    Receivable update = new Receivable();
    update.setId(id);
    update.setPaymentStatus("CANCELLED");
    update.setCancelReason(bo.getCancelReason().trim());
    update.setCancelledBy(LoginHelper.getUserId());
    update.setCancelledAt(LocalDateTime.now());
    return mapper.updateById(update) > 0;
  }

  public PageResult<ReceivableBatchVo> queryBatchPage(ReceivableBatchQueryBo bo, PageQuery pq) {
    requireProject(bo.getProjectId(), ProjectBusinessAction.READ);
    LambdaQueryWrapper<ReceivableBatch> q =
        new LambdaQueryWrapper<ReceivableBatch>()
            .eq(ReceivableBatch::getProjectId, bo.getProjectId())
            .eq(
                bo.getGenerateMonth() != null,
                ReceivableBatch::getGenerateMonth,
                bo.getGenerateMonth())
            .eq(
                bo.getStatus() != null && !bo.getStatus().isBlank(),
                ReceivableBatch::getStatus,
                bo.getStatus())
            .orderByDesc(ReceivableBatch::getCreateTime);
    Page<ReceivableBatch> p = batchMapper.selectPage(pq.build(), q);
    return PageResult.build(
        BeanUtil.copyToList(p.getRecords(), ReceivableBatchVo.class), p.getTotal());
  }

  public ReceivableBatchVo queryBatchById(Long id) {
    ReceivableBatch b = batchMapper.selectById(id);
    if (b == null) throw new ServiceException("生成批次不存在");
    access(b.getProjectId(), ProjectBusinessAction.READ);
    return BeanUtil.copyProperties(b, ReceivableBatchVo.class);
  }

  private LambdaQueryWrapper<Receivable> query(ReceivableQueryBo b) {
    LambdaQueryWrapper<Receivable> q = new LambdaQueryWrapper<>();
    q.eq(Receivable::getProjectId, b.getProjectId())
        .eq(b.getGenerateMonth() != null, Receivable::getGenerateMonth, b.getGenerateMonth())
        .like(
            b.getReceivableNo() != null && !b.getReceivableNo().isBlank(),
            Receivable::getReceivableNo,
            b.getReceivableNo())
        .like(
            b.getRoomKeyword() != null && !b.getRoomKeyword().isBlank(),
            Receivable::getRoomFullNameSnapshot,
            b.getRoomKeyword())
        .eq(b.getRoomId() != null, Receivable::getRoomId, b.getRoomId())
        .eq(b.getChargeItemId() != null, Receivable::getChargeItemId, b.getChargeItemId())
        .like(
            b.getResponsibleCustomerKeyword() != null
                && !b.getResponsibleCustomerKeyword().isBlank(),
            Receivable::getResponsibleCustomerNameSnapshot,
            b.getResponsibleCustomerKeyword())
        .eq(
            b.getPaymentStatus() != null && !b.getPaymentStatus().isBlank(),
            Receivable::getPaymentStatus,
            b.getPaymentStatus())
        .ge(b.getPeriodStartFrom() != null, Receivable::getPeriodStartDate, b.getPeriodStartFrom())
        .le(b.getPeriodStartTo() != null, Receivable::getPeriodStartDate, b.getPeriodStartTo())
        .ge(b.getDueDateFrom() != null, Receivable::getDueDate, b.getDueDateFrom())
        .le(b.getDueDateTo() != null, Receivable::getDueDate, b.getDueDateTo());
    if (Boolean.TRUE.equals(b.getOverdueOnly()))
      q.ne(Receivable::getPaymentStatus, "CANCELLED")
          .apply("receivable_amount > paid_amount")
          .lt(Receivable::getDueDate, LocalDate.now());
    return q.orderByDesc(Receivable::getPeriodStartDate).orderByDesc(Receivable::getId);
  }

  private void enrich(Collection<ReceivableVo> rows) {
    Set<Long> batchIds = rows.stream().map(ReceivableVo::getBatchId).collect(Collectors.toSet());
    Map<Long, String> batches =
        batchIds.isEmpty()
            ? Map.of()
            : batchMapper.selectBatchIds(batchIds).stream()
                .collect(Collectors.toMap(ReceivableBatch::getId, ReceivableBatch::getBatchNo));
    Set<Long> projects = rows.stream().map(ReceivableVo::getProjectId).collect(Collectors.toSet());
    Map<Long, String> names =
        projects.isEmpty()
            ? Map.of()
            : projectMapper.selectBatchIds(projects).stream()
                .collect(Collectors.toMap(Project::getId, Project::getProjectName));
    LocalDate today = LocalDate.now();
    rows.forEach(
        v -> {
          v.setProjectName(names.get(v.getProjectId()));
          v.setBatchNo(batches.get(v.getBatchId()));
          v.setOutstandingAmount(money(v.getReceivableAmount().subtract(v.getPaidAmount())));
          v.setOverdue(
              today.isAfter(v.getDueDate())
                  && v.getOutstandingAmount().signum() > 0
                  && !"CANCELLED".equals(v.getPaymentStatus()));
        });
  }

  private boolean isOverdue(Receivable r, LocalDate today) {
    return today.isAfter(r.getDueDate())
        && r.getReceivableAmount().compareTo(r.getPaidAmount()) > 0
        && !"CANCELLED".equals(r.getPaymentStatus());
  }

  private BigDecimal money(BigDecimal v) {
    return v.setScale(2, java.math.RoundingMode.HALF_UP);
  }

  private Receivable require(Long id) {
    Receivable r = mapper.selectById(id);
    if (r == null) throw new ServiceException("应收不存在");
    return r;
  }

  private void requireProject(Long projectId, ProjectBusinessAction action) {
    if (projectId == null) throw new ServiceException("所属项目不能为空");
    access(projectId, action);
  }

  private void access(Long p, ProjectBusinessAction a) {
    scopeService.assertBusinessAccess(p, PropertyBusinessDomain.FINANCE, a);
  }
}
