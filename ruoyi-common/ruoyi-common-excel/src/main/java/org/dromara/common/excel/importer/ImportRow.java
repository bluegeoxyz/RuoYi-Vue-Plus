package org.dromara.common.excel.importer;

/** 带 Excel 原始行号的导入行。 */
public interface ImportRow {
    Integer getRowNumber();
    void setRowNumber(Integer rowNumber);
}
