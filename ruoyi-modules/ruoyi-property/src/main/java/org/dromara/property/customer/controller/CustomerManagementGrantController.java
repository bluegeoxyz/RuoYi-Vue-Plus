package org.dromara.property.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.customer.domain.bo.CustomerManagementGrantBo;
import org.dromara.property.customer.domain.vo.CustomerManagementGrantVo;
import org.dromara.property.customer.service.ICustomerManagementGrantService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Customer 企业主档授权管理接口；Service 再次强制 SuperAdmin。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/customer/admin-scope")
public class CustomerManagementGrantController {
    private final ICustomerManagementGrantService grantService;

    @SaCheckPermission("property:customerGrant:list")
    @GetMapping("/list")
    public R<PageResult<CustomerManagementGrantVo>> list(CustomerManagementGrantBo bo, PageQuery page) {
        return R.ok(grantService.queryPage(bo, page));
    }

    @SaCheckPermission("property:customerGrant:query")
    @GetMapping("/history")
    public R<List<CustomerManagementGrantVo>> history(CustomerManagementGrantBo bo) {
        return R.ok(grantService.queryHistory(bo));
    }

    @SaCheckPermission("property:customerGrant:add")
    @Log(title = "客户主档授权", businessType = BusinessType.GRANT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody CustomerManagementGrantBo bo) {
        return grantService.grant(bo) ? R.ok() : R.fail("客户主档授权新增失败");
    }

    @SaCheckPermission("property:customerGrant:edit")
    @Log(title = "客户主档授权", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public R<Void> status(@PathVariable Long id, @RequestBody CustomerManagementGrantBo bo) {
        return grantService.changeStatus(id, bo.getVersion(), bo.getStatus())
            ? R.ok() : R.fail("授权已被其他操作修改，请刷新后重试");
    }

    @SaCheckPermission("property:customerGrant:remove")
    @Log(title = "客户主档授权", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> revoke(@PathVariable Long id, @RequestParam Integer version) {
        return grantService.revoke(id, version)
            ? R.ok() : R.fail("授权已被其他操作修改，请刷新后重试");
    }
}
