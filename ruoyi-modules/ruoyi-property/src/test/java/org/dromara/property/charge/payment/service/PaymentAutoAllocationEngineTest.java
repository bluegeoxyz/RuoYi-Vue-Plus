package org.dromara.property.charge.payment.service;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.charge.receivable.domain.Receivable;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class PaymentAutoAllocationEngineTest {
 private final PaymentAutoAllocationEngine engine=new PaymentAutoAllocationEngine();
 @Test void allocatesOldestDebtFirstAndSupportsPartial(){Receivable newer=row(2,"2400","0","UNPAID","2026-01-31","2026-01-01"),older=row(1,"2400","0","UNPAID","2025-01-31","2025-01-01");var p=engine.calculate(List.of(newer,older),bd("3000"));assertEquals(2,p.size());assertEquals(1L,p.get(0).receivable().getId());assertEquals(bd("2400"),p.get(0).amount());assertEquals(bd("600"),p.get(1).amount());}
 @Test void tieBreaksByPeriodThenId(){var a=row(3,"100","0","UNPAID","2025-02-01","2025-01-02");var b=row(2,"100","0","UNPAID","2025-02-01","2025-01-01");var c=row(1,"100","0","UNPAID","2025-02-01","2025-01-01");var p=engine.calculate(List.of(a,b,c),bd("250"));assertEquals(List.of(1L,2L,3L),p.stream().map(x->x.receivable().getId()).toList());}
 @Test void rejectsOverpaymentAndInconsistentState(){assertThrows(ServiceException.class,()->engine.calculate(List.of(row(1,"100","0","UNPAID","2025-01-01","2025-01-01")),bd("101")));assertThrows(ServiceException.class,()->engine.calculate(List.of(row(1,"100","10","UNPAID","2025-01-01","2025-01-01")),bd("10")));}
 @Test void rejectsRoomWithoutOutstandingDebt(){assertThrows(ServiceException.class,()->engine.calculate(List.of(row(1,"100","100","PAID","2025-01-01","2025-01-01")),bd("1")));}
 private Receivable row(long id,String amount,String paid,String status,String due,String start){Receivable r=new Receivable();r.setId(id);r.setReceivableNo("R"+id);r.setReceivableAmount(bd(amount));r.setPaidAmount(bd(paid));r.setPaymentStatus(status);r.setDueDate(LocalDate.parse(due));r.setPeriodStartDate(LocalDate.parse(start));return r;}private BigDecimal bd(String v){return new BigDecimal(v);}
}
