package org.dromara.property.charge.binding.domain.vo;
import lombok.*; import java.util.List;
/** 批量收费设置结果。 */
@Data @AllArgsConstructor public class ChargeBindingBatchResultVo { private int totalCount; private int successCount; private int failedCount; private List<ChargeBindingBatchFailureVo> failures; }
