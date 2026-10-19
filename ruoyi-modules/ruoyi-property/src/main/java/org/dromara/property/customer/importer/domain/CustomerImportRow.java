package org.dromara.property.customer.importer.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.importer.ImportRow;

import java.time.LocalDate;

/** Customer Excel 导入行，不暴露编号、生命周期或任何安全元数据。 */
@Data
public class CustomerImportRow implements ImportRow {
    @ExcelIgnore private Integer rowNumber;
    @NotBlank(message="客户类型不能为空") @Size(max=32) @ExcelProperty("客户类型") private String customerType;
    @NotBlank(message="客户名称不能为空") @Size(max=128) @ExcelProperty("客户名称") private String customerName;
    @Size(max=1) @ExcelProperty("性别") private String gender;
    @ExcelProperty("出生日期") private LocalDate birthDate;
    @Size(max=32) @ExcelProperty("手机号") private String mobile;
    @Size(max=32) @ExcelProperty("电话") private String phone;
    @Email(message="邮箱格式不正确") @Size(max=128) @ExcelProperty("邮箱") private String email;
    @Size(max=32) @ExcelProperty("证件类型") private String idType;
    @Size(max=128) @ExcelProperty("证件号码") private String idNo;
    @Size(max=500) @ExcelProperty("备注") private String remark;
}
