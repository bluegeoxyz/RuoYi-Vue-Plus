package org.dromara.property.workspace.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.property.workspace.domain.vo.WorkspaceProjectOptionVo;
import org.dromara.property.workspace.service.PropertyWorkspaceProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 登录用户的物业工作区上下文。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/workspace")
public class PropertyWorkspaceController {

    private final PropertyWorkspaceProjectService workspaceProjectService;

    @SaCheckPermission(value = {
        "property:project:list", "property:projectMember:list",
        "property:building:list", "property:unit:list", "property:room:list",
        "property:customer:list", "property:resident:list",
        "property:customerRelation:list", "property:customerRelation:add", "property:customerRelation:edit",
        "property:customerRelation:moveIn", "property:customerRelation:moveOut",
        "property:chargeItem:list", "property:chargeRule:list",
        "property:chargeBinding:list", "property:chargeBinding:add", "property:chargeBinding:edit",
        "property:receivable:list", "property:receivable:generate",
        "property:payment:list", "property:payment:add"
    }, mode = SaMode.OR)
    @GetMapping("/projects")
    public R<List<WorkspaceProjectOptionVo>> projects() {
        return R.ok(workspaceProjectService.selectAvailableProjects());
    }
}
