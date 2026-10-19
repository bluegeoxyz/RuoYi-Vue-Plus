package org.dromara.property.charge.receivable.service.impl;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.charge.binding.domain.ChargeBinding;
import org.dromara.property.charge.receivable.domain.*;
import org.dromara.property.charge.receivable.domain.bo.ReceivableGenerateBo;
import org.dromara.property.charge.receivable.domain.vo.ReceivableGenerationResultVo;
import org.dromara.property.charge.receivable.mapper.*;
import org.dromara.property.charge.receivable.service.IReceivableGenerationService;
import org.dromara.property.scope.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReceivableGenerationServiceImpl implements IReceivableGenerationService {
  public static final int MAX_FUTURE_MONTHS = 3;
  private final PropertyProjectScopeService scopeService;
  private final ReceivableGenerationMapper generationMapper;
  private final ReceivableBatchMapper batchMapper;
  private final ReceivableBindingGenerationWorker worker;

  @Override
  public ReceivableGenerationResultVo generate(ReceivableGenerateBo bo) {
    YearMonth month = parse(bo.getGenerateMonth());
    if (month.isAfter(YearMonth.now().plusMonths(MAX_FUTURE_MONTHS)))
      throw new ServiceException("生成月份不能超过未来3个月");
    scopeService.assertBusinessAccess(
        bo.getProjectId(), PropertyBusinessDomain.FINANCE, ProjectBusinessAction.WRITE);
    ReceivableBatch batch = start(bo.getProjectId(), month);
    Counters counters = new Counters();
    List<String> errors = new ArrayList<>();
    try {
      List<ChargeBinding> bindings = generationMapper.selectProjectRoomBindings(bo.getProjectId());
      counters.scanned = bindings.size();
      for (ChargeBinding b : bindings) {
        BindingGenerationOutcome out;
        try {
          out = worker.generate(b.getId(), bo.getProjectId(), batch.getId(), month);
        } catch (Exception e) {
          out = BindingGenerationOutcome.failed(safeMessage(e));
        }
        switch (out.type()) {
          case GENERATED -> counters.generated++;
          case SKIPPED_EXISTING -> counters.existing++;
          case SKIPPED_NOT_DUE -> counters.notDue++;
          case FAILED -> {
            counters.failed++;
            if (errors.size() < 10) errors.add("binding " + b.getId() + ": " + out.message());
          }
        }
      }
      finish(batch, counters, counters.failed > 0 ? "PARTIAL_SUCCESS" : "SUCCESS", summary(errors));
    } catch (Exception e) {
      finish(batch, counters, "FAILED", safeMessage(e));
    }
    return result(batch, counters);
  }

  private ReceivableBatch start(Long projectId, YearMonth month) {
    ReceivableBatch b = new ReceivableBatch();
    long id = IdUtil.getSnowflakeNextId();
    b.setId(id);
    b.setProjectId(projectId);
    b.setBatchNo("RGB" + month.toString().replace("-", "") + id);
    b.setGenerateMonth(month.atDay(1));
    b.setStatus("RUNNING");
    b.setScannedCount(0);
    b.setGeneratedCount(0);
    b.setSkippedExistingCount(0);
    b.setSkippedNotDueCount(0);
    b.setFailedCount(0);
    batchMapper.insert(b);
    batchMapper.markStarted(id);
    return b;
  }

  private void finish(ReceivableBatch b, Counters c, String status, String error) {
    b.setStatus(status);
    b.setScannedCount(c.scanned);
    b.setGeneratedCount(c.generated);
    b.setSkippedExistingCount(c.existing);
    b.setSkippedNotDueCount(c.notDue);
    b.setFailedCount(c.failed);
    b.setErrorMessage(error);
    batchMapper.finish(b);
  }

  private ReceivableGenerationResultVo result(ReceivableBatch b, Counters c) {
    return ReceivableGenerationResultVo.builder()
        .batchId(b.getId())
        .batchNo(b.getBatchNo())
        .projectId(b.getProjectId())
        .generateMonth(YearMonth.from(b.getGenerateMonth()).toString())
        .scannedCount(c.scanned)
        .generatedCount(c.generated)
        .skippedExistingCount(c.existing)
        .skippedNotDueCount(c.notDue)
        .failedCount(c.failed)
        .status(b.getStatus())
        .errorMessage(b.getErrorMessage())
        .build();
  }

  private YearMonth parse(String value) {
    try {
      return YearMonth.parse(value);
    } catch (Exception e) {
      throw new ServiceException("生成月份格式必须为yyyy-MM");
    }
  }

  private String safeMessage(Throwable e) {
    return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
  }

  private String summary(List<String> errors) {
    if (errors.isEmpty()) return null;
    String s = String.join("；", errors);
    return s.length() > 1000 ? s.substring(0, 1000) : s;
  }

  private static class Counters {
    int scanned, generated, existing, notDue, failed;
  }
}
