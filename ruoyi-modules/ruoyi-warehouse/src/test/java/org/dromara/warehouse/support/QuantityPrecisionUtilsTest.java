package org.dromara.warehouse.support;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuantityPrecisionUtilsTest {

    @Test
    void integerRepresentationsPassZeroPrecision() {
        assertFalse(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10"), 0));
        assertFalse(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10.0"), 0));
        assertFalse(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10.000000"), 0));
    }

    @Test
    void nonZeroFractionFailsZeroPrecision() {
        assertTrue(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10.50"), 0));
    }

    @Test
    void configuredFractionPrecisionUsesEffectiveScale() {
        assertFalse(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10.50"), 2));
        assertFalse(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10.500000"), 2));
        assertTrue(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10.555"), 2));
    }

    @Test
    void nullPrecisionDefaultsToZero() {
        assertFalse(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10.000000"), null));
        assertTrue(QuantityPrecisionUtils.exceedsPrecision(new BigDecimal("10.1"), null));
    }
}
