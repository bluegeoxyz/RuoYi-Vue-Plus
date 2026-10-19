package org.dromara.common.excel.importer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 原子导入错误。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportError implements Serializable {
    private Integer rowNumber;
    private String field;
    private String message;
}
