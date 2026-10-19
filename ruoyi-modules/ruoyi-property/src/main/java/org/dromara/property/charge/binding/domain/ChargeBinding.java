package org.dromara.property.charge.binding.domain;
import com.baomidou.mybatisplus.annotation.*; import lombok.*; import org.dromara.common.mybatis.core.domain.BaseEntity; import java.time.LocalDate;
@Data @EqualsAndHashCode(callSuper=true) @TableName("pm_charge_binding")
public class ChargeBinding extends BaseEntity {
 @TableId private Long id; private Long projectId; private String objectType; private Long objectId; private Long chargeItemId; private Long chargeRuleId;
 private LocalDate effectiveStartDate; private LocalDate effectiveEndDate; private String status; private Integer sort; private String remark; @TableLogic private String delFlag;
}
