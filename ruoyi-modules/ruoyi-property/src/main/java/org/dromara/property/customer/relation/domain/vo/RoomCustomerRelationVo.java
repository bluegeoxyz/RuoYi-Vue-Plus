package org.dromara.property.customer.relation.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.*;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.property.customer.relation.domain.RoomCustomerRelation;
import java.time.*;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = RoomCustomerRelation.class)
public class RoomCustomerRelationVo {
    private Long id;
    private Long projectId;
    private Long roomId;
    private Long customerId;
    @ExcelProperty("楼栋编码") private String buildingCode;
    @ExcelProperty("楼栋名称") private String buildingName;
    @ExcelProperty("单元编码") private String unitCode;
    @ExcelProperty("单元名称") private String unitName;
    @ExcelProperty("房屋编码") private String roomCode;
    @ExcelProperty("房号") private String roomNo;
    @ExcelProperty("客户编号") private String customerNo;
    @ExcelProperty("客户名称") private String customerName;
    @ExcelProperty("联系电话") private String mobile;
    @ExcelProperty(value = "关系类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_relation_type") private String relationType;
    @ExcelProperty(value = "主要关系人", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=否,1=是") private String isPrimary;
    @ExcelProperty("开始日期") private LocalDate startDate;
    @ExcelProperty("结束日期") private LocalDate endDate;
    @ExcelProperty("迁入日期") private LocalDate moveInDate;
    @ExcelProperty("迁出日期") private LocalDate moveOutDate;
    @ExcelProperty(value = "关系状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_relation_status") private String relationStatus;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable") private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
