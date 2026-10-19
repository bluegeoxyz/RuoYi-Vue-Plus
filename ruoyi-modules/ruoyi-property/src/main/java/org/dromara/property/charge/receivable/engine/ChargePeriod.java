package org.dromara.property.charge.receivable.engine;
import java.time.LocalDate;
public record ChargePeriod(LocalDate theoreticalStart,LocalDate theoreticalEnd,LocalDate actualStart,LocalDate actualEnd,int theoreticalDays,int actualDays) {}
