package org.dromara.property.charge.binding.domain.vo;
import lombok.*;
/** 单房间批量收费设置失败信息。 */
@Data @AllArgsConstructor public class ChargeBindingBatchFailureVo { private Long roomId; private String roomName; private String reasonCode; private String reasonMessage; }
