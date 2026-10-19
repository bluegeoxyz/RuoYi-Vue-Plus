package org.dromara.property.project.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 有效项目成员轻量选项。 */
@Data
public class ProjectMemberOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Long employeeId;
    private String employeeNo;
    private String employeeName;
    private Long projectId;
    private String projectName;
    private String memberRole;
    private String isManager;
}
