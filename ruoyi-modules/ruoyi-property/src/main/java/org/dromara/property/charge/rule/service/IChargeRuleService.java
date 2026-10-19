package org.dromara.property.charge.rule.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.charge.rule.domain.bo.ChargeRuleBo;
import org.dromara.property.charge.rule.domain.vo.*;
import java.util.*;

public interface IChargeRuleService {
    ChargeRuleVo queryById(Long id);

    PageResult<ChargeRuleVo> queryPage(ChargeRuleBo bo, PageQuery pq);

    List<ChargeRuleExportVo> queryExport(ChargeRuleBo bo);

    List<ChargeRuleOptionVo> options(ChargeRuleBo bo);

    boolean insert(ChargeRuleBo bo);

    boolean update(ChargeRuleBo bo);

    boolean changeStatus(Long id, String status);

    boolean delete(Collection<Long> ids);
}
