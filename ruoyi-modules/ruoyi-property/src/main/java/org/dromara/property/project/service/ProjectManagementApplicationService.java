package org.dromara.property.project.service;

import org.dromara.property.project.domain.bo.CreateManagedProjectCommand;

public interface ProjectManagementApplicationService {
    boolean createManagedProject(CreateManagedProjectCommand command);
}
