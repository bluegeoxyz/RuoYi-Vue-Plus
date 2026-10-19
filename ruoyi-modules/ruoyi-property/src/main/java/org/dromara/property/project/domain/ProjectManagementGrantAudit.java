package org.dromara.property.project.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_project_management_grant_audit")
public class ProjectManagementGrantAudit {
    @TableId
    private Long id;
    private Long grantId;
    private Long operatorId;
    private String action;
    private String principalType;
    private Long principalId;
    private String scopeType;
    private Long projectId;
    private String beforeStatus;
    private String afterStatus;
    private String result;
    private String traceId;
    private LocalDateTime createTime;
    private String remark;
}
