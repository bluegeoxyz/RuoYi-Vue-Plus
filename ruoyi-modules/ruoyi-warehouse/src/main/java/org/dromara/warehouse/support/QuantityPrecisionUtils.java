package org.dromara.warehouse.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 仓库业务数量精度工具。 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuantityPrecisionUtils {

    /**
     * 判断数量的有效小数位是否超过允许精度，尾随零不计入有效小数位。
     *
     * @param quantity 数量
     * @param precisionScale 允许精度；为空时按不可拆分单位的精度 0 处理
     * @return 是否超过允许精度
     */
    public static boolean exceedsPrecision(BigDecimal quantity, Integer precisionScale) {
        if (quantity == null) {
            return false;
        }
        int allowedScale = precisionScale == null ? 0 : precisionScale;
        return effectiveScale(quantity) > allowedScale;
    }

    /**
     * 返回数量的有效小数位数，忽略数据库 DECIMAL 或输入值携带的尾随零。
     *
     * @param quantity 数量
     * @return 有效小数位数
     */
    public static int effectiveScale(BigDecimal quantity) {
        return Math.max(0, quantity.stripTrailingZeros().scale());
    }
}
