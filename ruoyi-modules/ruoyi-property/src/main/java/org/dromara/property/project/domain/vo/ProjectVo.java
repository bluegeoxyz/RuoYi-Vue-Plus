package org.dromara.property.project.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.property.project.domain.Project;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 物业项目视图对象。 */
@Data
@AutoMapper(target = Project.class)
public class ProjectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String projectCode;
    private String projectName;
    private String projectShortName;
    private String projectType;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    private String address;
    private String servicePhone;
    private Long managerEmployeeId;
    private String managerEmployeeName;
    private String managerEmployeeNo;
    private LocalDate deliveryDate;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer sort;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
