package org.dromara.property.customer.domain.vo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.io.Serial;
import java.io.Serializable;

/** Customer 默认脱敏导出对象。 */
@Data
@ExcelIgnoreUnannotated
public class CustomerMaskedExportVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("客户编号")
    private String customerNo;
    @ExcelProperty(value = "客户类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_customer_type")
    private String customerType;
    @ExcelProperty("客户姓名")
    private String customerName;
    @ExcelProperty(value = "性别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_user_gender")
    private String gender;
    @ExcelProperty("手机号码（脱敏）")
    private String mobile;
    @ExcelProperty("联系电话（脱敏）")
    private String phone;
    @ExcelProperty("邮箱（脱敏）")
    private String email;
    @ExcelProperty(value = "证件类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "zbm_id_type")
    private String idType;
    @ExcelProperty("证件号码（脱敏）")
    private String idNo;
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;
    @ExcelProperty(value = "生命周期状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "pm_customer_lifecycle")
    private String lifecycleStatus;
}
