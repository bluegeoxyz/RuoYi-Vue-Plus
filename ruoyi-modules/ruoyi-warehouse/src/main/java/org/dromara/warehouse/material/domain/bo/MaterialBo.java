package org.dromara.warehouse.material.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.warehouse.material.domain.Material;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 物料业务对象。 */
@Data
@AutoMapper(target = Material.class, reverseConvertGenerate = false)
public class MaterialBo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotNull(message = "物料ID不能为空", groups = EditGroup.class) private Long id;
    @NotNull(message = "所属公司不能为空", groups = {AddGroup.class, EditGroup.class}) private Long companyId;
    @NotBlank(message = "物料编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "物料编码长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String materialCode;
    @NotBlank(message = "物料名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 128, message = "物料名称长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String materialName;
    @NotNull(message = "物料分类不能为空", groups = {AddGroup.class, EditGroup.class}) private Long categoryId;
    @NotNull(message = "基础单位不能为空", groups = {AddGroup.class, EditGroup.class}) private Long baseUnitId;
    @Size(max = 255, message = "规格长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String specification;
    @Size(max = 128, message = "型号长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String model;
    @Size(max = 128, message = "条码长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String barcode;
    @DecimalMin(value = "0", message = "安全库存不能小于0", groups = {AddGroup.class, EditGroup.class}) private BigDecimal safetyStock;
    private String status;
    private Integer sort;
    @Size(max = 500, message = "备注长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class}) private String remark;
}
