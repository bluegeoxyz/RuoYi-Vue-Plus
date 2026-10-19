package org.dromara.property.project.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物业项目实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_project")
public class Project extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
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
    private LocalDate deliveryDate;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer sort;
    private String status;
    private String remark;

    @TableLogic
    private String delFlag;
}
