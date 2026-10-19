package org.dromara.common.excel.importer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 一个文件对应一个事务的原子导入结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult implements Serializable {
    private int totalRows;
    private int successRows;
    private int failedRows;
    private List<ImportError> errors = new ArrayList<>();

    public static ImportResult success(int totalRows) {
        return new ImportResult(totalRows, totalRows, 0, new ArrayList<>());
    }

    public static ImportResult failure(int totalRows, List<ImportError> errors) {
        return new ImportResult(totalRows, 0, totalRows, new ArrayList<>(errors));
    }
}
