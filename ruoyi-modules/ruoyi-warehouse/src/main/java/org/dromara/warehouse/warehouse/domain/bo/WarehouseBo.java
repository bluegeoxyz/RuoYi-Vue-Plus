package org.dromara.warehouse.warehouse.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.warehouse.warehouse.domain.Warehouse;

import java.io.Serial;
import java.io.Serializable;

/** 仓库业务对象。 */
@Data
@AutoMapper(target = Warehouse.class, reverseConvertGenerate = false)
public class WarehouseBo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotNull(message = "仓库ID不能为空", groups = EditGroup.class)
    private Long id;
    @NotNull(message = "所属公司不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long companyId;
    private Long projectId;
    @NotBlank(message = "归属类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String warehouseScope;
    @NotBlank(message = "仓库编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "仓库编码长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String warehouseCode;
    @NotBlank(message = "仓库名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 128, message = "仓库名称长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String warehouseName;
    private Long managerId;
    @Size(max = 32, message = "联系电话长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String contactPhone;
    @Size(max = 255, message = "地址长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String address;
    private String status;
    private Integer sort;
    @Size(max = 500, message = "备注长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String remark;
}
