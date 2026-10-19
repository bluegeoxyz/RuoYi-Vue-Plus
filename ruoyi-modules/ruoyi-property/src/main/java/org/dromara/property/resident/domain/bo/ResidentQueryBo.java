package org.dromara.property.resident.domain.bo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
/** 当前项目住户关系查询条件。 */
@Data public class ResidentQueryBo {
 @NotNull(message="项目不能为空") private Long projectId;
 private String keyword; private String relationType; private String relationStatus;
 private Long buildingId; private Long unitId; private Long roomId;
 private LocalDate moveInDateFrom; private LocalDate moveInDateTo;
 private LocalDate moveOutDateFrom; private LocalDate moveOutDateTo;
}
