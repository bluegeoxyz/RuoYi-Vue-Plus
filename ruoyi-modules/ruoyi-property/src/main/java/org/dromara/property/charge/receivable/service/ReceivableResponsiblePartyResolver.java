package org.dromara.property.charge.receivable.service;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.charge.receivable.mapper.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReceivableResponsiblePartyResolver {
    private final ReceivableGenerationMapper mapper;

    public ResponsibleCustomerSnapshot resolve(Long roomId, LocalDate periodStart) {
        List<ResponsibleCustomerSnapshot> owners = mapper.selectOwnersOnDate(roomId, periodStart);
        if (owners.isEmpty()) throw new ServiceException("收费周期开始日不存在有效业主");
        if (owners.size() != 1) throw new ServiceException("收费周期开始日责任主体不唯一");
        return owners.get(0);
    }
}
