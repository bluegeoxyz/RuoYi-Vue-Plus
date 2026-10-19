package org.dromara.property.customer.relation.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.property.customer.relation.domain.RoomCustomerRelation;

import java.time.LocalDate;

@Data
@AutoMapper(target = RoomCustomerRelation.class, reverseConvertGenerate = false)
public class RoomCustomerRelationBo {
    @NotNull(groups = EditGroup.class) private Long id;
    @NotNull(groups = AddGroup.class) private Long projectId;
    @NotNull(groups = AddGroup.class) private Long roomId;
    @NotNull(groups = AddGroup.class) private Long customerId;
    @NotBlank(groups = AddGroup.class)
    @Pattern(regexp = "OWNER|TENANT|FAMILY|RESIDENT|CONTACT|OTHER")
    private String relationType;
    @Pattern(regexp = "0|1") private String isPrimary;
    @NotNull(groups = AddGroup.class) private LocalDate startDate;
    @Size(max = 1) private String status;
    @Size(max = 500) private String remark;
    private String relationStatus;
}
