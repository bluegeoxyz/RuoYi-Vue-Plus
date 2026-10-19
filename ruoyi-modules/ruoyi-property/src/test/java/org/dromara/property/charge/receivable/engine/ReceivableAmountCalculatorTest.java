package org.dromara.property.charge.receivable.engine;
import org.junit.jupiter.api.Test; import java.math.BigDecimal; import static org.junit.jupiter.api.Assertions.*;
class ReceivableAmountCalculatorTest { private final ReceivableAmountCalculator c=new ReceivableAmountCalculator(new ChargePeriodCalculator());
 @Test void areaSupportsYearAndQuarter(){assertEquals(new BigDecimal("2400.00"),c.calculate("AREA",bd("100"),bd("2"),null,"YEAR","MONTH",365,365));assertEquals(new BigDecimal("600.00"),c.calculate("AREA",bd("100"),bd("2"),null,"QUARTER","MONTH",90,90));}
 @Test void fixedSupportsCycleRatio(){assertEquals(new BigDecimal("1200.00"),c.calculate("FIXED",null,null,bd("100"),"YEAR","MONTH",365,365));assertEquals(new BigDecimal("100.00"),c.calculate("FIXED",null,null,bd("1200"),"MONTH","YEAR",31,31));}
 @Test void partialCycleProratesByClosedIntervalDays(){assertEquals(new BigDecimal("140.00"),c.calculate("FIXED",null,null,bd("200"),"MONTH","MONTH",21,30));}
 private BigDecimal bd(String value){return new BigDecimal(value);}
}
