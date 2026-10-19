package org.dromara.property.project.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_project_management_grant")
public class ProjectManagementGrant extends BaseEntity {
    @TableId
    private Long id;
    private String principalType;
    private Long principalId;
    private String scopeType;
    private Long projectId;
    private String status;
    @Version
    private Integer version;
    @TableLogic
    private String delFlag;
}
