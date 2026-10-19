package org.dromara.property.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Customer 新增后的最小响应。 */
@Data
public class CustomerCreatedVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String customerNo;
    private String customerName;
    private String status;
    private String lifecycleStatus;
    private Integer version;
}
