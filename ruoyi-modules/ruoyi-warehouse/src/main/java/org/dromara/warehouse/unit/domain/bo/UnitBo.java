package org.dromara.warehouse.unit.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.warehouse.unit.domain.Unit;

import java.io.Serial;
import java.io.Serializable;

/** 计量单位业务对象。 */
@Data
@AutoMapper(target = Unit.class, reverseConvertGenerate = false)
public class UnitBo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotNull(message = "单位ID不能为空", groups = EditGroup.class) private Long id;
    @NotBlank(message = "单位编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32, message = "单位编码长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String unitCode;
    @NotBlank(message = "单位名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "单位名称长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String unitName;
    @Size(max = 32, message = "单位符号长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String symbol;
    @NotNull(message = "小数精度不能为空", groups = {AddGroup.class, EditGroup.class})
    @Min(value = 0, message = "小数精度不能小于0", groups = {AddGroup.class, EditGroup.class})
    @Max(value = 6, message = "小数精度不能大于6", groups = {AddGroup.class, EditGroup.class}) private Integer precisionScale;
    private String status;
    private Integer sort;
    @Size(max = 500, message = "备注长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String remark;
}
