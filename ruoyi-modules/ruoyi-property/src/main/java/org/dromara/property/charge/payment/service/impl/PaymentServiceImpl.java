package org.dromara.property.charge.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.charge.payment.domain.*;
import org.dromara.property.charge.payment.domain.bo.*;
import org.dromara.property.charge.payment.domain.vo.*;
import org.dromara.property.charge.payment.mapper.*;
import org.dromara.property.charge.payment.service.*;
import org.dromara.property.charge.receivable.domain.Receivable;
import org.dromara.property.charge.receivable.mapper.ReceivableMapper;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
  private static final Set<String> METHODS =
      Set.of("CASH", "WECHAT", "ALIPAY", "BANK_TRANSFER", "POS", "OTHER");
  private final PaymentMapper paymentMapper;
  private final PaymentAllocationMapper allocationMapper;
  private final PaymentReversalMapper reversalMapper;
  private final ReceivableMapper receivableMapper;
  private final RoomMapper roomMapper;
  private final BuildingMapper buildingMapper;
  private final BuildingUnitMapper unitMapper;
  private final CustomerMapper customerMapper;
  private final ProjectMapper projectMapper;
  private final PropertyProjectScopeService scopeService;
  private final PaymentAutoAllocationEngine allocationEngine;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PaymentVo create(PaymentCreateBo bo) {
    validateRequest(bo);
    Room room = roomMapper.selectById(bo.getRoomId());
    if (room == null) throw new ServiceException("房屋不存在");
    if (!Objects.equals(room.getProjectId(), bo.getProjectId()))
      throw new ServiceException("房屋不属于当前项目");
    access(room.getProjectId(), ProjectBusinessAction.WRITE);
    Payer payer = resolvePayer(bo);
    List<Receivable> locked =
        receivableMapper.selectPayableByRoomForUpdate(room.getProjectId(), room.getId());
    List<PaymentAutoAllocationEngine.AllocationPlan> plans =
        allocationEngine.calculate(locked, money(bo.getPaymentAmount()));
    LocalDateTime allocatedAt = LocalDateTime.now();
    long paymentId = IdUtil.getSnowflakeNextId();
    Payment payment = new Payment();
    payment.setId(paymentId);
    payment.setProjectId(room.getProjectId());
    payment.setPaymentNo("PAY" + paymentId);
    payment.setRoomId(room.getId());
    payment.setRoomFullNameSnapshot(roomName(room));
    payment.setPayerCustomerId(payer.id());
    payment.setPayerNameSnapshot(payer.name());
    payment.setPayerTypeSnapshot(payer.type());
    payment.setPaymentAmount(money(bo.getPaymentAmount()));
    payment.setAllocatedAmount(BigDecimal.ZERO.setScale(2));
    payment.setPaymentMethod(bo.getPaymentMethod());
    payment.setTransactionNo(trim(bo.getTransactionNo()));
    payment.setPaymentTime(bo.getPaymentTime());
    payment.setStatus("VALID");
    payment.setRemark(trim(bo.getRemark()));
    if (paymentMapper.insert(payment) != 1) throw new ServiceException("创建收款失败");
    BigDecimal allocated = BigDecimal.ZERO;
    for (var plan : plans) {
      Receivable row = plan.receivable();
      BigDecimal newPaid = money(row.getPaidAmount().add(plan.amount()));
      String status = newPaid.compareTo(row.getReceivableAmount()) == 0 ? "PAID" : "PARTIAL";
      PaymentAllocation a = new PaymentAllocation();
      a.setId(IdUtil.getSnowflakeNextId());
      a.setProjectId(room.getProjectId());
      a.setPaymentId(paymentId);
      a.setReceivableId(row.getId());
      a.setAllocationAmount(money(plan.amount()));
      a.setAllocationTime(allocatedAt);
      if (allocationMapper.insert(a) != 1) throw new ServiceException("保存核销明细失败");
      if (receivableMapper.updatePaymentState(row.getId(), newPaid, status) != 1)
        throw new ServiceException("更新应收失败");
      allocated = allocated.add(plan.amount());
    }
    allocated = money(allocated);
    if (allocated.compareTo(payment.getPaymentAmount()) != 0)
      throw new ServiceException("收款未能全部核销");
    if (paymentMapper.updateAllocatedAmount(paymentId, allocated) != 1)
      throw new ServiceException("更新收款核销金额失败");
    return queryPersisted(paymentId, ProjectBusinessAction.READ, false);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PaymentReverseResultVo reverse(Long id, PaymentReverseBo bo) {
    String reason = bo == null ? null : trim(bo.getReason());
    if (reason == null || reason.length() < 2 || reason.length() > 500)
      throw new ServiceException("冲销原因长度必须在2到500字符之间");
    Payment payment = paymentMapper.selectActiveByIdForUpdate(id);
    if (payment == null) throw new ServiceException("收款记录不存在");
    access(payment.getProjectId(), ProjectBusinessAction.WRITE);
    if ("REVERSED".equals(payment.getStatus())) throw new ServiceException("该收款已冲销，不能重复操作");
    if (!"VALID".equals(payment.getStatus())) throw new ServiceException("收款状态异常，不能冲销");
    BigDecimal paymentAmount = money(payment.getPaymentAmount()),
        allocatedAmount = money(payment.getAllocatedAmount());
    if (allocatedAmount.compareTo(paymentAmount) != 0) throw new ServiceException("收款未全部核销，不能冲销");
    if (reversalMapper.selectByPaymentId(id) != null) throw new ServiceException("该收款已存在冲销记录");
    List<PaymentAllocation> allocations = allocationMapper.selectByPaymentIdForUpdate(id);
    if (allocations.isEmpty()) throw new ServiceException("收款核销明细缺失，不能冲销");
    BigDecimal allocationSum =
        money(
            allocations.stream()
                .map(PaymentAllocation::getAllocationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    if (allocationSum.compareTo(allocatedAmount) != 0
        || allocationSum.compareTo(paymentAmount) != 0)
      throw new ServiceException("收款核销金额不一致，不能冲销");
    if (allocations.stream()
        .anyMatch(a -> !Objects.equals(payment.getProjectId(), a.getProjectId())))
      throw new ServiceException("收款核销项目数据异常");
    List<Long> receivableIds =
        allocations.stream().map(PaymentAllocation::getReceivableId).distinct().sorted().toList();
    List<Receivable> receivables = receivableMapper.selectByIdsForUpdate(receivableIds);
    if (receivables.size() != receivableIds.size()) throw new ServiceException("关联应收数据缺失，不能冲销");
    Map<Long, Receivable> receivableMap = new HashMap<>();
    receivables.forEach(r -> receivableMap.put(r.getId(), r));
    Map<Long, BigDecimal> reversalByReceivable = new LinkedHashMap<>();
    for (PaymentAllocation allocation : allocations) {
      Receivable receivable = receivableMap.get(allocation.getReceivableId());
      if (receivable == null
          || !Objects.equals(payment.getProjectId(), receivable.getProjectId())
          || !Objects.equals(payment.getRoomId(), receivable.getRoomId()))
        throw new ServiceException("收款、核销与应收归属数据异常");
      if ("CANCELLED".equals(receivable.getPaymentStatus()))
        throw new ServiceException("关联应收已作废，财务数据异常");
      reversalByReceivable.merge(
          receivable.getId(), money(allocation.getAllocationAmount()), BigDecimal::add);
    }
    List<PaymentReverseReceivableVo> details = new ArrayList<>();
    for (Long receivableId : receivableIds) {
      Receivable receivable = receivableMap.get(receivableId);
      BigDecimal reversed = money(reversalByReceivable.get(receivableId));
      BigDecimal newPaid = money(receivable.getPaidAmount().subtract(reversed));
      if (newPaid.signum() < 0) throw new ServiceException("应收已收金额小于核销金额，财务数据异常");
      String status = paymentStatus(newPaid, receivable.getReceivableAmount());
      if (receivableMapper.updatePaymentState(receivableId, newPaid, status) != 1)
        throw new ServiceException("回退应收核销金额失败");
      details.add(
          PaymentReverseReceivableVo.builder()
              .receivableId(receivableId)
              .receivableNo(receivable.getReceivableNo())
              .reversedAllocationAmount(reversed)
              .paidAmountAfter(newPaid)
              .outstandingAmountAfter(money(receivable.getReceivableAmount().subtract(newPaid)))
              .paymentStatusAfter(status)
              .build());
    }
    if (paymentMapper.markReversed(id) != 1) throw new ServiceException("更新收款冲销状态失败");
    LocalDateTime reversalTime = LocalDateTime.now();
    long reversalId = IdUtil.getSnowflakeNextId();
    PaymentReversal reversal = new PaymentReversal();
    reversal.setId(reversalId);
    reversal.setProjectId(payment.getProjectId());
    reversal.setReversalNo("REV" + reversalId);
    reversal.setPaymentId(id);
    reversal.setReversalAmount(paymentAmount);
    reversal.setReversalTime(reversalTime);
    reversal.setReason(reason);
    if (reversalMapper.insert(reversal) != 1) throw new ServiceException("保存收款冲销记录失败");
    return PaymentReverseResultVo.builder()
        .paymentId(id)
        .paymentNo(payment.getPaymentNo())
        .reversalId(reversalId)
        .reversalNo(reversal.getReversalNo())
        .reversalAmount(paymentAmount)
        .reversalTime(reversalTime)
        .reason(reversal.getReason())
        .paymentStatus("REVERSED")
        .affectedReceivableCount(details.size())
        .receivables(details)
        .build();
  }

  @Override
  public PaymentVo queryById(Long id) {
    return queryPersisted(id, ProjectBusinessAction.READ, true);
  }

  @Override
  public PageResult<PaymentVo> queryPage(PaymentQueryBo bo, PageQuery pq) {
    requireProject(bo.getProjectId(), ProjectBusinessAction.READ);
    Page<Payment> p = paymentMapper.selectPage(pq.build(), query(bo));
    List<PaymentVo> rows = BeanUtil.copyToList(p.getRecords(), PaymentVo.class);
    enrich(rows);
    return PageResult.build(rows, p.getTotal());
  }

  @Override
  public List<PaymentExportVo> export(PaymentQueryBo bo) {
    requireProject(bo.getProjectId(), ProjectBusinessAction.EXPORT);
    List<PaymentVo> rows =
        BeanUtil.copyToList(paymentMapper.selectList(query(bo)), PaymentVo.class);
    enrich(rows);
    return BeanUtil.copyToList(rows, PaymentExportVo.class);
  }

  @Override
  public List<PaymentRoomOptionVo> roomOptions(Long projectId) {
    requireProject(projectId, ProjectBusinessAction.WRITE);
    return paymentMapper.selectCollectableRoomOptions(projectId);
  }

  private PaymentVo queryPersisted(Long id, ProjectBusinessAction action, boolean checkAccess) {
    Payment payment = paymentMapper.selectActiveById(id);
    if (payment == null) throw new ServiceException("收款记录不存在");
    if (checkAccess) access(payment.getProjectId(), action);
    PaymentVo vo = BeanUtil.copyProperties(payment, PaymentVo.class);
    enrich(List.of(vo));
    List<PaymentAllocation> allocations =
        allocationMapper.selectList(
            new LambdaQueryWrapper<PaymentAllocation>()
                .eq(PaymentAllocation::getPaymentId, id)
                .orderByAsc(PaymentAllocation::getAllocationTime)
                .orderByAsc(PaymentAllocation::getId));
    if (!allocations.isEmpty()) {
      Map<Long, Receivable> rows = new HashMap<>();
      receivableMapper
          .selectBatchIds(allocations.stream().map(PaymentAllocation::getReceivableId).toList())
          .forEach(r -> rows.put(r.getId(), r));
      vo.setAllocations(
          allocations.stream().map(a -> allocationVo(a, rows.get(a.getReceivableId()))).toList());
    }
    PaymentReversal reversal = reversalMapper.selectByPaymentId(id);
    if (reversal != null)
      vo.setReversal(BeanUtil.copyProperties(reversal, PaymentReversalVo.class));
    return vo;
  }

  private PaymentAllocationVo allocationVo(PaymentAllocation a, Receivable r) {
    PaymentAllocationVo v = BeanUtil.copyProperties(a, PaymentAllocationVo.class);
    if (r != null) {
      v.setReceivableNo(r.getReceivableNo());
      v.setChargeItemNameSnapshot(r.getChargeItemNameSnapshot());
      v.setResponsibleCustomerNameSnapshot(r.getResponsibleCustomerNameSnapshot());
      v.setPeriodStart(r.getPeriodStartDate());
      v.setPeriodEnd(r.getPeriodEndDate());
      v.setReceivablePaidAmount(r.getPaidAmount());
      v.setReceivableOutstandingAmount(money(r.getReceivableAmount().subtract(r.getPaidAmount())));
      v.setPaymentStatus(r.getPaymentStatus());
    }
    return v;
  }

  private LambdaQueryWrapper<Payment> query(PaymentQueryBo bo) {
    return new LambdaQueryWrapper<Payment>()
        .eq(Payment::getProjectId, bo.getProjectId())
        .eq(bo.getRoomId() != null, Payment::getRoomId, bo.getRoomId())
        .like(has(bo.getPaymentNo()), Payment::getPaymentNo, bo.getPaymentNo())
        .like(has(bo.getRoomKeyword()), Payment::getRoomFullNameSnapshot, bo.getRoomKeyword())
        .like(has(bo.getPayerKeyword()), Payment::getPayerNameSnapshot, bo.getPayerKeyword())
        .eq(has(bo.getPaymentMethod()), Payment::getPaymentMethod, bo.getPaymentMethod())
        .eq(has(bo.getStatus()), Payment::getStatus, bo.getStatus())
        .ge(bo.getPaymentTimeFrom() != null, Payment::getPaymentTime, bo.getPaymentTimeFrom())
        .le(bo.getPaymentTimeTo() != null, Payment::getPaymentTime, bo.getPaymentTimeTo())
        .orderByDesc(Payment::getPaymentTime)
        .orderByDesc(Payment::getId);
  }

  private void enrich(Collection<PaymentVo> rows) {
    Set<Long> ids = new HashSet<>();
    rows.forEach(v -> ids.add(v.getProjectId()));
    Map<Long, String> names =
        ids.isEmpty()
            ? Map.of()
            : projectMapper.selectBatchIds(ids).stream()
                .collect(
                    java.util.stream.Collectors.toMap(Project::getId, Project::getProjectName));
    rows.forEach(v -> v.setProjectName(names.get(v.getProjectId())));
  }

  private Payer resolvePayer(PaymentCreateBo bo) {
    if (bo.getPayerCustomerId() == null) {
      String name = trim(bo.getPayerName());
      if (name == null) throw new ServiceException("未绑定客户时付款人名称不能为空");
      return new Payer(null, name, trim(bo.getPayerType()));
    }
    Customer c = customerMapper.selectById(bo.getPayerCustomerId());
    if (c == null || !"0".equals(c.getStatus()) || !"ACTIVE".equals(c.getLifecycleStatus()))
      throw new ServiceException("付款客户不存在或状态不可用");
    return new Payer(c.getId(), c.getCustomerName(), c.getCustomerType());
  }

  private String roomName(Room room) {
    Building b = buildingMapper.selectById(room.getBuildingId());
    BuildingUnit u = room.getUnitId() == null ? null : unitMapper.selectById(room.getUnitId());
    return name(b == null ? null : b.getBuildingName(), b == null ? null : b.getBuildingCode())
        + name(u == null ? null : u.getUnitName(), u == null ? null : u.getUnitCode())
        + name(room.getRoomNo(), room.getRoomCode());
  }

  private String name(String value, String code) {
    String n = trim(value);
    return n != null ? n : Objects.requireNonNullElse(trim(code), "");
  }

  private void validateRequest(PaymentCreateBo bo) {
    if (bo.getProjectId() == null || bo.getRoomId() == null)
      throw new ServiceException("所属项目和房屋不能为空");
    if (bo.getPaymentAmount() == null || bo.getPaymentAmount().signum() <= 0)
      throw new ServiceException("收款金额必须大于0");
    if (bo.getPaymentAmount().scale() > 2) throw new ServiceException("收款金额最多保留2位小数");
    if (!METHODS.contains(bo.getPaymentMethod())) throw new ServiceException("收款方式不是有效值");
    if (bo.getPaymentTime() == null) throw new ServiceException("收款时间不能为空");
    if (bo.getPaymentTime().isAfter(LocalDateTime.now()))
      throw new ServiceException("收款时间不能晚于当前时间");
  }

  private String paymentStatus(BigDecimal paid, BigDecimal receivable) {
    int compared = paid.compareTo(money(receivable));
    if (compared > 0) throw new ServiceException("应收已收金额超过应收金额，财务数据异常");
    if (paid.signum() == 0) return "UNPAID";
    return compared == 0 ? "PAID" : "PARTIAL";
  }

  private BigDecimal money(BigDecimal v) {
    if (v == null) throw new ServiceException("财务金额数据缺失");
    return v.setScale(2, RoundingMode.UNNECESSARY);
  }

  private boolean has(String v) {
    return v != null && !v.isBlank();
  }

  private String trim(String v) {
    return v == null || v.isBlank() ? null : v.trim();
  }

  private void requireProject(Long id, ProjectBusinessAction action) {
    if (id == null) throw new ServiceException("所属项目不能为空");
    access(id, action);
  }

  private void access(Long id, ProjectBusinessAction action) {
    scopeService.assertBusinessAccess(id, PropertyBusinessDomain.FINANCE, action);
  }

  private record Payer(Long id, String name, String type) {}
}
