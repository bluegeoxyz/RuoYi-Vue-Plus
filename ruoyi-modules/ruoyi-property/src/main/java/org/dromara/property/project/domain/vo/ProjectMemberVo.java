package org.dromara.property.project.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.property.project.domain.ProjectMember;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 物业项目成员关系视图。 */
@Data
@AutoMapper(target = ProjectMember.class)
public class ProjectMemberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long projectId;
    private String projectCode;
    private String projectName;
    private Long employeeId;
    private String employeeNo;
    private String employeeName;
    private String deptName;
    private String postName;
    private String memberRole;
    private String isManager;
    private LocalDate startDate;
    private LocalDate endDate;
    private String memberStatus;
    private Integer sort;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}
