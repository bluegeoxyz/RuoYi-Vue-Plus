package org.dromara.hr.employee.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.hr.employee.domain.bo.EmployeeBo;
import org.dromara.hr.employee.domain.bo.EmployeeBindUserBo;
import org.dromara.hr.employee.domain.bo.EmployeeUserOptionQuery;
import org.dromara.hr.employee.domain.vo.EmployeeExportVo;
import org.dromara.hr.employee.domain.vo.EmployeeOptionVo;
import org.dromara.hr.employee.domain.vo.EmployeeVo;
import org.dromara.hr.employee.domain.vo.EmployeeUserOptionVo;
import org.dromara.hr.employee.service.IEmployeeService;
import org.dromara.system.domain.vo.SysPostVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** 员工档案管理。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hr/employee")
public class EmployeeController extends BaseController {

    private final IEmployeeService employeeService;

    @SaCheckPermission("hr:employee:list")
    @GetMapping("/list")
    public R<PageResult<EmployeeVo>> list(@Validated(QueryGroup.class) EmployeeBo bo, PageQuery pageQuery) {
        return R.ok(employeeService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("hr:employee:export")
    @Log(title = "员工档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@Validated(QueryGroup.class) EmployeeBo bo, HttpServletResponse response) {
        List<EmployeeExportVo> list = employeeService.queryExportList(bo);
        ExcelBuilder.of(list, EmployeeExportVo.class).sheetName("员工档案").toResponse(response);
    }

    @SaCheckPermission("hr:employee:query")
    @GetMapping("/{id}")
    public R<EmployeeVo> getInfo(@NotNull(message = "员工ID不能为空") @PathVariable Long id) {
        return R.ok(employeeService.queryById(id));
    }

    @SaCheckPermission("hr:employee:add")
    @Log(title = "员工档案", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody EmployeeBo bo) {
        return toAjax(employeeService.insertByBo(bo));
    }

    @SaCheckPermission("hr:employee:edit")
    @Log(title = "员工档案", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody EmployeeBo bo) {
        return toAjax(employeeService.updateByBo(bo));
    }

    @SaCheckPermission("hr:employee:edit")
    @Log(title = "员工档案状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody EmployeeBo bo) {
        if (bo.getId() == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }
        return toAjax(employeeService.changeStatus(bo.getId(), bo.getStatus()));
    }

    @SaCheckPermission("hr:employee:bindUser")
    @Log(title = "员工账号绑定", businessType = BusinessType.GRANT)
    @RepeatSubmit
    @PutMapping("/{employeeId}/bind-user")
    public R<Void> bindUser(@NotNull(message = "员工ID不能为空") @PathVariable Long employeeId,
                            @Validated @RequestBody EmployeeBindUserBo bo) {
        return toAjax(employeeService.bindUser(employeeId, bo.getUserId()));
    }

    @SaCheckPermission("hr:employee:bindUser")
    @Log(title = "员工账号解绑", businessType = BusinessType.UPDATE)
    @DeleteMapping("/{employeeId}/unbind-user")
    public R<Void> unbindUser(@NotNull(message = "员工ID不能为空") @PathVariable Long employeeId) {
        return toAjax(employeeService.unbindUser(employeeId));
    }

    @SaCheckPermission("hr:employee:bindUser")
    @GetMapping("/user-options")
    public R<PageResult<EmployeeUserOptionVo>> userOptions(EmployeeUserOptionQuery query, PageQuery pageQuery) {
        return R.ok(employeeService.selectUserOptions(query, pageQuery));
    }

    @SaCheckPermission("hr:employee:remove")
    @Log(title = "员工档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "员工ID不能为空") @PathVariable Long[] ids) {
        return toAjax(employeeService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @SaCheckPermission("hr:employee:list")
    @GetMapping("/options")
    public R<List<EmployeeOptionVo>> options() {
        return R.ok(employeeService.selectEmployeeOptions());
    }

    @SaCheckPermission("hr:employee:list")
    @GetMapping("/deptTree")
    public R<List<Tree<Long>>> deptTree() {
        return R.ok(employeeService.selectDeptTree());
    }

    @SaCheckPermission("hr:employee:list")
    @GetMapping("/postOptions")
    public R<List<SysPostVo>> postOptions(@NotNull(message = "部门ID不能为空") @RequestParam Long deptId) {
        return R.ok(employeeService.selectPostOptions(deptId));
    }
}
