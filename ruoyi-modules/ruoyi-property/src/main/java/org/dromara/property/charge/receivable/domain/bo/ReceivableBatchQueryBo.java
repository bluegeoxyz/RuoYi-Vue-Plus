package org.dromara.property.charge.receivable.domain.bo;
import lombok.Data; import java.time.LocalDate;
@Data public class ReceivableBatchQueryBo { private Long projectId; private LocalDate generateMonth; private String status; }
