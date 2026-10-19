package org.dromara.property.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Customer 默认详情视图，所有 PII 均由后端脱敏。 */
@Data
public class CustomerDetailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String customerNo;
    private String customerType;
    private String customerName;
    private String gender;
    private String mobile;
    private String phone;
    private String email;
    private String idType;
    private String idNo;
    private String status;
    private String lifecycleStatus;
}
