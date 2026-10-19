package org.dromara.property.project.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.property.project.domain.ProjectManagementGrant;

@Data
@AutoMapper(target = ProjectManagementGrant.class, reverseConvertGenerate = false)
public class ProjectManagementGrantBo {
    private Long id;
    @NotBlank
    private String principalType;
    @NotNull
    private Long principalId;
    @NotBlank
    private String scopeType;
    private Long projectId;
    private Integer version;
    private String status;
    private String remark;
}
