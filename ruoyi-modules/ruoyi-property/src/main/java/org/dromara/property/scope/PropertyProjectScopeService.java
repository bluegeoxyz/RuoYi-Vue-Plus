package org.dromara.property.scope;

/** 项目业务与管理范围的唯一服务边界。 */
public interface PropertyProjectScopeService {
    PropertyProjectScope resolveBusinessScope(PropertyBusinessDomain domain, ProjectBusinessAction action);
    PropertyProjectScope resolveManagementScope(ProjectManagementAction action);
    void assertBusinessAccess(Long projectId, PropertyBusinessDomain domain, ProjectBusinessAction action);
    void assertManagementAccess(Long projectId, ProjectManagementAction action);
    void assertCanCreateProject();
}
