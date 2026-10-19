package org.dromara.property.charge.receivable.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.charge.payment.domain.*;
import org.dromara.property.charge.payment.mapper.*;
import org.dromara.property.charge.receivable.domain.Receivable;
import org.dromara.property.charge.receivable.domain.vo.ReceivablePaymentVo;
import org.dromara.property.charge.receivable.mapper.ReceivableMapper;
import org.dromara.property.scope.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReceivablePaymentHistoryService {
  private final ReceivableMapper receivableMapper;
  private final PaymentAllocationMapper allocationMapper;
  private final PaymentMapper paymentMapper;
  private final PropertyProjectScopeService scopeService;

  public List<ReceivablePaymentVo> query(Long receivableId) {
    Receivable r = receivableMapper.selectById(receivableId);
    if (r == null) throw new ServiceException("应收不存在");
    scopeService.assertBusinessAccess(
        r.getProjectId(), PropertyBusinessDomain.FINANCE, ProjectBusinessAction.READ);
    List<PaymentAllocation> rows =
        allocationMapper.selectList(
            new LambdaQueryWrapper<PaymentAllocation>()
                .eq(PaymentAllocation::getReceivableId, receivableId)
                .orderByAsc(PaymentAllocation::getAllocationTime)
                .orderByAsc(PaymentAllocation::getId));
    if (rows.isEmpty()) return List.of();
    Map<Long, Payment> payments = new HashMap<>();
    paymentMapper
        .selectBatchIds(rows.stream().map(PaymentAllocation::getPaymentId).toList())
        .forEach(p -> payments.put(p.getId(), p));
    return rows.stream().map(a -> toVo(a, payments.get(a.getPaymentId()))).toList();
  }

  private ReceivablePaymentVo toVo(PaymentAllocation a, Payment p) {
    ReceivablePaymentVo v = new ReceivablePaymentVo();
    v.setPaymentId(a.getPaymentId());
    v.setAllocationAmount(a.getAllocationAmount());
    if (p != null) {
      v.setPaymentNo(p.getPaymentNo());
      v.setPayerNameSnapshot(p.getPayerNameSnapshot());
      v.setPayerTypeSnapshot(p.getPayerTypeSnapshot());
      v.setPaymentTime(p.getPaymentTime());
      v.setPaymentMethod(p.getPaymentMethod());
      v.setPaymentStatus(p.getStatus());
    }
    return v;
  }
}
