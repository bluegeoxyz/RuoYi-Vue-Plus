package org.dromara.property.customer.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** Customer 企业主档查询条件。 */
@Data
public class CustomerQueryBo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 32)
    private String customerNo;
    @Size(max = 32)
    private String customerType;
    @Size(max = 128)
    private String customerName;
    @Size(max = 32)
    private String mobile;
    @Size(max = 1)
    private String status;
    @Size(max = 32)
    private String lifecycleStatus;
}
