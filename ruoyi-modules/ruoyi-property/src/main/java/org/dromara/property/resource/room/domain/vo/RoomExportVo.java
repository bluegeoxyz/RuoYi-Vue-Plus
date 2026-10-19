package org.dromara.property.resource.room.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.math.BigDecimal;

/** 房屋业务导出对象，不包含内部 ID。 */
@Data
@ExcelIgnoreUnannotated
public class RoomExportVo {
    @ExcelProperty("房屋编码") private String roomCode;
    @ExcelProperty("房号") private String roomNo;
    @ExcelProperty("楼层") private Integer floorNo;
    @ExcelProperty(value = "房屋类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_room_type") private String roomType;
    @ExcelProperty(value = "产权属性", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_property_type") private String propertyType;
    @ExcelProperty("建筑面积") private BigDecimal buildingArea;
    @ExcelProperty("使用面积") private BigDecimal usableArea;
    @ExcelProperty(value = "交付状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_delivery_status") private String deliveryStatus;
    @ExcelProperty(value = "入住状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_occupancy_status") private String occupancyStatus;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable") private String status;
    @ExcelProperty("备注") private String remark;
}
