package org.dromara.property.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Customer 列表最小化脱敏视图。 */
@Data
public class CustomerListVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String customerNo;
    private String customerType;
    private String customerName;
    private String mobile;
    private String status;
    private String lifecycleStatus;
    private Integer version;
}
