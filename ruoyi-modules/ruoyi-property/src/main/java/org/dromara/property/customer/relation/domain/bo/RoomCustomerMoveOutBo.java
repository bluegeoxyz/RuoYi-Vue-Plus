package org.dromara.property.customer.relation.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RoomCustomerMoveOutBo {
    @NotNull private Long id;
    @NotNull private LocalDate moveOutDate;
}
