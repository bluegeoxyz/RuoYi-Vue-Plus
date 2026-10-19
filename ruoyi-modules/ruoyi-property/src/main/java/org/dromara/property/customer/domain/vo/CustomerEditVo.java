package org.dromara.property.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Customer 普通编辑视图，不包含完整联系方式及强身份字段。 */
@Data
public class CustomerEditVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String customerNo;
    private String customerType;
    private String customerName;
    private String gender;
    private LocalDate birthDate;
    private String remark;
    private Integer version;
}
