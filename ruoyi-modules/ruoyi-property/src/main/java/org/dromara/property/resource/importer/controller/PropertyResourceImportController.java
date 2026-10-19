package org.dromara.property.resource.importer.controller;

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
import org.dromara.property.resource.importer.domain.PropertyResourceImportRow;
import org.dromara.property.resource.importer.service.IPropertyResourceImportService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 项目资源原子导入接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/resource/tree-import")
public class PropertyResourceImportController {
    private final IPropertyResourceImportService importService;

    /** 导入当前项目的楼栋、单元和房屋树。 */
    @SaCheckPermission("property:resource:treeImport")
    @Log(title = "项目房屋资源", businessType = BusinessType.IMPORT)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ImportResult> importTree(@RequestParam Long projectId, @RequestPart("file") MultipartFile file) throws IOException {
        ExcelResult<PropertyResourceImportRow> parsed = ExcelBuilder.read(file.getInputStream(), PropertyResourceImportRow.class)
            .failFast(false).autoTrim(true).autoStrip(true).doRead();
        if (!parsed.getErrorList().isEmpty()) {
            List<ImportError> errors = parsed.getErrorList().stream().map(message -> new ImportError(null, "file", message)).toList();
            int total = parsed.getList().size() + parsed.getErrorList().size();
            return R.ok(ImportResult.failure(total, errors));
        }
        return R.ok(importService.importRows(projectId, parsed.getList()));
    }

    /** 下载带示例数据、字段说明及原子性提示的导入模板。 */
    @SaCheckPermission("property:resource:treeImport")
    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        List<PropertyResourceImportRow> examples = new ArrayList<>();
        PropertyResourceImportRow row = new PropertyResourceImportRow();
        row.setBuildingCode("1#"); row.setBuildingName("1号楼"); row.setBuildingType("住宅");
        row.setAboveGroundFloors(18); row.setUndergroundFloors(1); row.setBuildingSort(1);
        row.setUnitCode("1"); row.setUnitName("1单元"); row.setFloorCount(18); row.setUnitSort(1);
        row.setRoomCode("1-1-101"); row.setRoomNo("101"); row.setFloorNo(1); row.setRoomType("住宅");
        row.setPropertyType("私有产权"); row.setBuildingArea(new BigDecimal("89.50")); row.setUsableArea(new BigDecimal("72.00"));
        row.setDeliveryStatus("已交付"); row.setOccupancyStatus("空置"); row.setRoomSort(101); row.setStatus("正常");
        examples.add(row);
        ExcelBuilder.of(examples, PropertyResourceImportRow.class)
            .sheetName("房屋资源-任一行失败则全部不导入").toResponse(response);
    }
}
