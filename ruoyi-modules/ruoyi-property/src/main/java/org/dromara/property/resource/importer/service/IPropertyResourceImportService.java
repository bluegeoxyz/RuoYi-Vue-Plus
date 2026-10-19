package org.dromara.property.resource.importer.service;

import org.dromara.common.excel.importer.ImportResult;
import org.dromara.property.resource.importer.domain.PropertyResourceImportRow;

import java.util.List;

/** 项目房屋树形原子导入服务。 */
public interface IPropertyResourceImportService {
    ImportResult importRows(Long projectId, List<PropertyResourceImportRow> rows);
}
