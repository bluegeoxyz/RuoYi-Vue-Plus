package org.dromara.property.resident.domain.vo;
import lombok.Data; import java.io.*; import java.util.List;
/** Customer 基本资料及当前用户可见的住房关系聚合详情。 */
@Data public class ResidentDetailVo implements Serializable {
 @Serial private static final long serialVersionUID=1L;
 private Long customerId; private String customerNo; private String customerType; private String customerName; private String mobile; private String status; private String lifecycleStatus;
 private List<ResidentRelationVo> currentRelations=List.of(); private List<ResidentRelationVo> pendingRelations=List.of(); private List<ResidentRelationVo> historyRelations=List.of();
}
