package org.dromara.property.customer.relation.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 项目关系维护使用的最小脱敏 Customer 候选。 */
@Data
@AllArgsConstructor
public class CustomerReferenceOptionVo {
    private Long id;
    private String customerNo;
    private String customerType;
    private String customerName;
    private String mobile;
}
