package org.dromara.property.project.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.property.project.domain.ProjectMember;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 物业项目成员关系业务对象。
 */
@Data
@AutoMapper(target = ProjectMember.class, reverseConvertGenerate = false)
public class ProjectMemberBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "成员关系ID不能为空", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "项目不能为空", groups = AddGroup.class)
    private Long projectId;

    @NotNull(message = "员工不能为空", groups = AddGroup.class)
    private Long employeeId;

    @NotBlank(message = "项目角色不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32, message = "项目角色长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String memberRole;

    private String isManager;

    @NotNull(message = "开始日期不能为空", groups = {AddGroup.class, EditGroup.class})
    private LocalDate startDate;

    private LocalDate endDate;
    private String memberStatus;
    private Integer sort;
    private String status;

    @Size(max = 500, message = "备注长度不能超过{max}个字符", groups = {AddGroup.class, EditGroup.class})
    private String remark;

    private Map<String, Object> params = new HashMap<>();
}
