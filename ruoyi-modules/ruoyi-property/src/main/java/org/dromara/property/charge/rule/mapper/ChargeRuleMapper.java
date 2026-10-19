package org.dromara.property.charge.rule.mapper;

import org.apache.ibatis.annotations.*;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.charge.rule.domain.ChargeRule;
import org.dromara.property.charge.rule.domain.vo.ChargeRuleVo;

public interface ChargeRuleMapper extends BaseMapperPlus<ChargeRule, ChargeRuleVo> {
    @Select("SELECT * FROM pm_charge_rule WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    ChargeRule selectActiveByIdForUpdate(@Param("id") Long id);

    default long countActiveCode(Long projectId, String ruleCode, Long excludeId) {
        return lambda().eq(ChargeRule::getProjectId, projectId)
                .eq(ChargeRule::getRuleCode, ruleCode)
                .ne(excludeId != null, ChargeRule::getId, excludeId)
                .count();
    }

    @Select(
            "SELECT EXISTS(SELECT 1 FROM pm_charge_binding WHERE charge_rule_id=#{ruleId} AND del_flag='0')")
    boolean existsActiveChargeBinding(@Param("ruleId") Long ruleId);

    @Select(
            "SELECT EXISTS(SELECT 1 FROM pm_charge_binding WHERE charge_rule_id=#{ruleId} AND del_flag='0' AND (effective_start_date>#{endDate} OR effective_end_date IS NULL OR effective_end_date>#{endDate}))")
    boolean existsBindingBeyond(
            @Param("ruleId") Long ruleId, @Param("endDate") java.time.LocalDate endDate);
}
