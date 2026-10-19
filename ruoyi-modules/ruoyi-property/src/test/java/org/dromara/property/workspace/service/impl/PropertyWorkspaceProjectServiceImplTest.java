package org.dromara.property.workspace.service.impl;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.scope.ProjectBusinessAction;
import org.dromara.property.scope.PropertyBusinessDomain;
import org.dromara.property.scope.PropertyProjectScope;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PropertyWorkspaceProjectServiceImplTest {

    private final PropertyProjectScopeService scopeService = mock(PropertyProjectScopeService.class);
    private final ProjectMapper projectMapper = mock(ProjectMapper.class);
    private final PropertyWorkspaceProjectServiceImpl service =
        new PropertyWorkspaceProjectServiceImpl(scopeService, projectMapper);

    @Test
    void noBusinessPermissionShouldBeDenied() {
        try (MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class);
             MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(false);
            stp.when(() -> StpUtil.hasPermission(any(String.class))).thenReturn(false);

            assertThrows(NotPermissionException.class, service::selectAvailableProjects);
        }
    }

    @Test
    void explicitFinanceScopeShouldReturnMinimalOptionsWithStringIds() {
        Project project = new Project();
        project.setId(9007199254740993L);
        project.setProjectCode("TEST-A");
        project.setProjectName("测试项目A");
        project.setProjectShortName("项目A");
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.FINANCE, ProjectBusinessAction.OPTION))
            .thenReturn(PropertyProjectScope.explicit(Set.of(project.getId())));
        when(projectMapper.selectWorkspaceProjects(Set.of(project.getId()))).thenReturn(List.of(project));

        try (MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class);
             MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(false);
            stp.when(() -> StpUtil.hasPermission(any(String.class)))
                .thenAnswer(invocation -> "property:payment:add".equals(invocation.getArgument(0)));

            var result = service.selectAvailableProjects();

            assertEquals(1, result.size());
            assertEquals("9007199254740993", result.getFirst().id());
            verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.FINANCE, ProjectBusinessAction.OPTION);
        }
    }

    @Test
    void emptyAuthorizedScopeShouldReturnEmptyList() {
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.OPTION))
            .thenReturn(PropertyProjectScope.none());
        try (MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class);
             MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            login.when(LoginHelper::isSuperAdmin).thenReturn(false);
            stp.when(() -> StpUtil.hasPermission(any(String.class)))
                .thenAnswer(invocation -> "property:customerRelation:list".equals(invocation.getArgument(0)));

            assertEquals(List.of(), service.selectAvailableProjects());
        }
    }
}
