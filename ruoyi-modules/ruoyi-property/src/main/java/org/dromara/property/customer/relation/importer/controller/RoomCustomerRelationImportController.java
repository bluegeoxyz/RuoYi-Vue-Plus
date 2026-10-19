package org.dromara.property.customer.relation.importer.controller;

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
import org.dromara.property.customer.relation.importer.domain.RoomCustomerRelationImportRow;
import org.dromara.property.customer.relation.importer.service.IRoomCustomerRelationImportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** 房屋客户关系 Excel 原子导入接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/customer-relation/import")
class RelationImportController {
    private final IRoomCustomerRelationImportService importService;

    @SaCheckPermission("property:customer-relation:import")
    @Log(title = "房屋客户关系导入", businessType = BusinessType.IMPORT,
        isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ImportResult> run(@RequestParam("projectId") Long projectId,
                               @RequestPart("file") MultipartFile file) throws IOException {
        ExcelResult<RoomCustomerRelationImportRow> parsed;
        try {
            parsed = ExcelBuilder.read(file.getInputStream(), RoomCustomerRelationImportRow.class)
                .failFast(false).autoTrim(true).autoStrip(true).doRead();
        } catch (RuntimeException e) {
            return R.ok(ImportResult.failure(0, List.of(new ImportError(null, "file", "Excel 文件格式不正确"))));
        }
        if (!parsed.getErrorList().isEmpty()) {
            int total = parsed.getList().size() + parsed.getErrorList().size();
            return R.ok(ImportResult.failure(total,
                parsed.getErrorList().stream().map(x -> new ImportError(null, "file", "Excel 文件格式或表头不正确")).toList()));
        }
        return R.ok(importService.importRows(projectId, parsed.getList()));
    }

    @SaCheckPermission("property:customer-relation:import")
    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        RoomCustomerRelationImportRow row = new RoomCustomerRelationImportRow();
        row.setBuildingCode("1#");
        row.setUnitCode("1");
        row.setRoomCode("101");
        row.setCustomerNo("C210000000001");
        row.setRelationType("产权人/业主");
        row.setIsPrimary("是");
        row.setStartDate(LocalDate.of(2026, 1, 1));
        row.setRemark("示例数据；任意一行失败则全部不导入");
        ExcelBuilder.of(List.of(row), RoomCustomerRelationImportRow.class)
            .sheetName("关系导入-任一行失败则全部不导入").toResponse(response);
    }
}
