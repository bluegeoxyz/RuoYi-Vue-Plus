package org.dromara.property.project.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.property.project.domain.Project;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** 物业项目业务对象。 */
@Data
@AutoMapper(target = Project.class, reverseConvertGenerate = false)
public class ProjectBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "项目ID不能为空", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "项目编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32, message = "项目编码长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String projectCode;

    @NotBlank(message = "项目名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 128, message = "项目名称长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String projectName;

    @Size(max = 64, message = "项目简称长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String projectShortName;

    private String projectType;
    private String provinceCode;
    private String cityCode;
    private String districtCode;

    @Size(max = 255, message = "详细地址长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String address;

    @Size(max = 32, message = "服务电话长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String servicePhone;
    private Long managerEmployeeId;
    private LocalDate deliveryDate;

    @DecimalMin(value = "-180", message = "经度不能小于-180", groups = {AddGroup.class, EditGroup.class})
    @DecimalMax(value = "180", message = "经度不能大于180", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal longitude;

    @DecimalMin(value = "-90", message = "纬度不能小于-90", groups = {AddGroup.class, EditGroup.class})
    @DecimalMax(value = "90", message = "纬度不能大于90", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal latitude;

    private Integer sort;
    private String status;

    @Size(max = 500, message = "备注长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String remark;
    private Map<String, Object> params = new HashMap<>();
}
