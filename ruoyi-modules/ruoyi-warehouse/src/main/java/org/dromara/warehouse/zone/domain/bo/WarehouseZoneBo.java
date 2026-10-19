package org.dromara.warehouse.zone.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.warehouse.zone.domain.WarehouseZone;

import java.io.Serial;
import java.io.Serializable;

/** 库区业务对象。 */
@Data
@AutoMapper(target = WarehouseZone.class, reverseConvertGenerate = false)
public class WarehouseZoneBo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotNull(message = "库区ID不能为空", groups = EditGroup.class)
    private Long id;
    @NotNull(message = "仓库不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long warehouseId;
    @NotBlank(message = "库区编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "库区编码长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String zoneCode;
    @NotBlank(message = "库区名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 128, message = "库区名称长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String zoneName;
    private String status;
    private Integer sort;
    @Size(max = 500, message = "备注长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String remark;
}
