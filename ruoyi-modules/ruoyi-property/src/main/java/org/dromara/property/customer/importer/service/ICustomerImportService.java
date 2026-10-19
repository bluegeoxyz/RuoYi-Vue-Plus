package org.dromara.property.customer.importer.service;

import org.dromara.common.excel.importer.ImportResult;
import org.dromara.property.customer.importer.domain.CustomerImportRow;
import java.util.List;

/** Customer 原子导入服务。 */
public interface ICustomerImportService { ImportResult importRows(List<CustomerImportRow> rows); }
