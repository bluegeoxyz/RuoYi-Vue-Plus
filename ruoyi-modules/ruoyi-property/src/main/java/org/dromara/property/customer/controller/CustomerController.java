package org.dromara.property.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.core.utils.file.FileUtils;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.property.customer.domain.bo.CustomerQueryBo;
import org.dromara.property.customer.domain.bo.CustomerCreateBo;
import org.dromara.property.customer.domain.bo.CustomerLifecycleBo;
import org.dromara.property.customer.domain.bo.CustomerUpdateBo;
import org.dromara.property.customer.domain.vo.CustomerCreatedVo;
import org.dromara.property.customer.domain.vo.CustomerDetailVo;
import org.dromara.property.customer.domain.vo.CustomerEditVo;
import org.dromara.property.customer.domain.vo.CustomerListVo;
import org.dromara.property.customer.domain.vo.CustomerMaskedExportVo;
import org.dromara.property.customer.domain.vo.CustomerSensitiveExportVo;
import org.dromara.property.customer.domain.vo.CustomerSensitiveDetailVo;
import org.dromara.property.customer.service.ICustomerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/** Customer 企业主档安全接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/property/customer")
public class CustomerController extends BaseController {
    private final ICustomerService customerService;

    @SaCheckPermission("property:customer:list")
    @GetMapping("/list")
    public R<PageResult<CustomerListVo>> list(@Valid CustomerQueryBo bo, PageQuery pageQuery) {
        return R.ok(customerService.queryPage(bo, pageQuery));
    }

    @SaCheckPermission("property:customer:export")
    @Log(title = "客户主档脱敏导出", businessType = BusinessType.EXPORT,
        isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/export")
    public void export(@Valid CustomerQueryBo bo, HttpServletResponse response) throws IOException {
        List<CustomerMaskedExportVo> rows = customerService.queryMaskedExport(bo);
        writeCompletedWorkbook(response, createWorkbook(rows, CustomerMaskedExportVo.class, "客户档案"), "客户档案");
    }

    @SaCheckPermission("property:customer:export")
    @Log(title = "客户主档敏感导出", businessType = BusinessType.EXPORT,
        isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/export-sensitive")
    public void exportSensitive(@Valid CustomerQueryBo bo,
                                @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
                                HttpServletResponse response) throws IOException {
        List<CustomerSensitiveExportVo> rows = customerService.querySensitiveExport(bo, traceId);
        byte[] workbook;
        try {
            workbook = createWorkbook(rows, CustomerSensitiveExportVo.class, "客户敏感档案");
        } catch (RuntimeException e) {
            customerService.auditSensitiveExportFailure(rows.size(), traceId);
            throw e;
        }
        writeCompletedWorkbook(response, workbook, "客户敏感档案");
    }

    private <T> byte[] createWorkbook(List<T> rows, Class<T> type, String sheetName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ExcelBuilder.of(rows, type).sheetName(sheetName).toStream(output);
        return output.toByteArray();
    }

    private void writeCompletedWorkbook(HttpServletResponse response, byte[] workbook, String fileName) throws IOException {
        FileUtils.setAttachmentResponseHeader(response, UUID.randomUUID().toString().replace("-", "")
            + "_" + fileName + ".xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        response.setContentLength(workbook.length);
        response.getOutputStream().write(workbook);
    }

    @SaCheckPermission("property:customer:query")
    @GetMapping("/{id}")
    public R<CustomerDetailVo> getInfo(@NotNull(message = "客户ID不能为空") @PathVariable Long id) {
        return R.ok(customerService.queryById(id));
    }

    @SaCheckPermission("property:customer:edit")
    @GetMapping("/{id}/edit")
    public R<CustomerEditVo> getEditInfo(@NotNull(message = "客户ID不能为空") @PathVariable Long id) {
        return R.ok(customerService.queryForEdit(id));
    }

    @SaCheckPermission({"property:customer:query", "property:customer:pii:view"})
    @GetMapping("/{id}/sensitive")
    public R<CustomerSensitiveDetailVo> getSensitiveInfo(
        @NotNull(message = "客户ID不能为空") @PathVariable Long id,
        @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {
        return R.ok(customerService.querySensitiveById(id, traceId));
    }

    @SaCheckPermission("property:customer:add")
    @Log(title = "客户主档", businessType = BusinessType.INSERT,
        isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping
    public R<CustomerCreatedVo> add(@Valid @RequestBody CustomerCreateBo bo) {
        return R.ok(customerService.create(bo));
    }

    @SaCheckPermission("property:customer:edit")
    @Log(title = "客户主档", businessType = BusinessType.UPDATE,
        isSaveRequestData = false, isSaveResponseData = false)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody CustomerUpdateBo bo) {
        return toAjax(customerService.update(bo));
    }

    @SaCheckPermission("property:customer:edit")
    @Log(title = "客户生命周期", businessType = BusinessType.UPDATE,
        isSaveRequestData = false, isSaveResponseData = false)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@Valid @RequestBody CustomerLifecycleBo bo) {
        return toAjax(customerService.changeLifecycle(bo));
    }

    @SaCheckPermission("property:customer:remove")
    @Log(title = "客户主档", businessType = BusinessType.DELETE,
        isSaveRequestData = false, isSaveResponseData = false)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "客户ID不能为空") @PathVariable Long id,
                          @NotNull(message = "版本号不能为空") @RequestParam Integer version) {
        return toAjax(customerService.delete(id, version));
    }
}
