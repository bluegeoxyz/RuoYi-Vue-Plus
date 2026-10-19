package org.dromara.property.project.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.property.project.domain.ProjectManagementGrant;

import java.time.LocalDateTime;

@Data @AutoMapper(target = ProjectManagementGrant.class)
public class ProjectManagementGrantVo {
    private Long id; private String principalType; private Long principalId; private String scopeType;
    private Long projectId; private String status; private Integer version; private String delFlag;
    private Long createBy; private LocalDateTime createTime; private Long updateBy; private LocalDateTime updateTime; private String remark;
}
