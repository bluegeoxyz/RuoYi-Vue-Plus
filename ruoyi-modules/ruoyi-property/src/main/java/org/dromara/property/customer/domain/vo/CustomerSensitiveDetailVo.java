package org.dromara.property.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** 经企业授权、PII_VIEW 和审计后才能返回的完整敏感详情。 */
@Data
public class CustomerSensitiveDetailVo implements Serializable {
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
    private LocalDate birthDate;
    private String status;
    private String lifecycleStatus;
}
