package org.dromara.property.project.domain.bo;

import lombok.Data;
import java.util.List;

/** 创建项目及可选首批成员的聚合命令。 */
@Data public class CreateManagedProjectCommand {
    private ProjectBo project;
    private List<ProjectMemberBo> initialMembers;
    private Long managerEmployeeId;
}
