package org.dromara.property.project.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 物业项目成员关系实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_project_member")
public class ProjectMember extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    private Long projectId;
    private Long employeeId;
    private String memberRole;
    private String isManager;
    private LocalDate startDate;
    private LocalDate endDate;
    private String memberStatus;
    private Integer sort;
    private String status;
    private String remark;

    @TableLogic
    private String delFlag;
}
