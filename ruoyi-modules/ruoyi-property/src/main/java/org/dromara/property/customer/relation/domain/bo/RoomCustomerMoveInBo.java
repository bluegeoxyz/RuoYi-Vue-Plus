package org.dromara.property.customer.relation.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RoomCustomerMoveInBo {
    @NotNull private Long id;
    @NotNull private LocalDate moveInDate;
}
