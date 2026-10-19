package org.dromara.property.customer.importer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.core.ExcelResult;
import org.dromara.common.excel.importer.ImportError;
import org.dromara.common.excel.importer.ImportResult;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.property.customer.importer.domain.CustomerImportRow;
import org.dromara.property.customer.importer.service.ICustomerImportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** Customer Excel 原子导入接口。 */
@RestController @RequiredArgsConstructor @RequestMapping("/property/customer/import")
public class CustomerImportController {
    private final ICustomerImportService importService;
    @SaCheckPermission("property:customer:import") @Log(title="客户主档导入",businessType=BusinessType.IMPORT,isSaveRequestData=false,isSaveResponseData=false)
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ImportResult> importCustomers(@RequestPart("file") MultipartFile file)throws IOException{
        ExcelResult<CustomerImportRow> parsed;
        try{parsed=ExcelBuilder.read(file.getInputStream(),CustomerImportRow.class).failFast(false).autoTrim(true).autoStrip(true).doRead();}
        catch(RuntimeException e){return R.ok(ImportResult.failure(0,List.of(new ImportError(null,"file","Excel 文件格式不正确"))));}
        if(!parsed.getErrorList().isEmpty())return R.ok(ImportResult.failure(parsed.getList().size()+parsed.getErrorList().size(),parsed.getErrorList().stream().map(x->new ImportError(null,"file","Excel 文件格式或表头不正确")).toList()));
        return R.ok(importService.importRows(parsed.getList()));
    }
    @SaCheckPermission("property:customer:import") @GetMapping("/template")
    public void template(HttpServletResponse response){CustomerImportRow r=new CustomerImportRow();r.setCustomerType("个人");r.setCustomerName("示例客户");r.setGender("男");r.setBirthDate(LocalDate.of(1990,1,1));r.setMobile("13800000000");r.setEmail("demo@example.invalid");r.setRemark("虚构示例；证件信息请按实际数据填写");ExcelBuilder.of(List.of(r),CustomerImportRow.class).sheetName("客户导入-任一行失败则全部不导入").toResponse(response);}
}
