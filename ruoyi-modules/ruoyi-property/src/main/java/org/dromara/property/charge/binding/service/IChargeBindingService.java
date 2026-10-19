package org.dromara.property.charge.binding.service;

import org.dromara.property.charge.binding.domain.bo.*;
import org.dromara.property.charge.binding.domain.vo.*;
import java.util.*;

public interface IChargeBindingService {
    List<ChargeBindingVo> queryRoomBindings(Long roomId);

    ChargeBindingVo queryById(Long id);

    boolean insert(ChargeBindingBo bo);

    ChargeBindingBatchResultVo batchCreate(ChargeBindingBatchBo bo);

    boolean update(ChargeBindingBo bo);

    boolean changeStatus(Long id, String status, java.time.LocalDate effectiveEndDate);

    boolean delete(Collection<Long> ids);
}
