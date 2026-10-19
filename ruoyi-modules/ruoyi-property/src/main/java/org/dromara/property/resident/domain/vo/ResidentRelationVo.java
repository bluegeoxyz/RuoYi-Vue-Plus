package org.dromara.property.resident.domain.vo;
import lombok.Data;
import java.io.*; import java.time.LocalDate;
/** 住户视图的一条房屋关系。 */
@Data public class ResidentRelationVo implements Serializable {
 @Serial private static final long serialVersionUID=1L;
 private Long customerId; private String customerNo; private String customerType; private String customerStatus; private String customerLifecycleStatus;
 private String maskedCustomerName; private String maskedMobile; private Long projectId; private String projectName;
 private Long buildingId; private String buildingName; private String buildingCode; private Long unitId; private String unitName; private String unitCode;
 private Long roomId; private String roomCode; private String roomNo; private Long relationId; private String relationType; private String relationStatus; private String isPrimary;
 private LocalDate startDate; private LocalDate endDate; private LocalDate moveInDate; private LocalDate moveOutDate;
}
