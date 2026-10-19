package org.dromara.property.resource.unit.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.property.resource.unit.domain.BuildingUnit;

import java.time.LocalDateTime;

/**
 * 楼栋单元展示对象。
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BuildingUnit.class)
public class BuildingUnitVo {
    private Long id;
    private Long projectId;
    private Long buildingId;
    @ExcelProperty("单元编码")
    private String unitCode;
    @ExcelProperty("单元名称")
    private String unitName;
    @ExcelProperty("楼层数")
    private Integer floorCount;
    private Integer sort;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
