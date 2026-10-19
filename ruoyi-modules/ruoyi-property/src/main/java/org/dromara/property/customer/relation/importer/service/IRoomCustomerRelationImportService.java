package org.dromara.property.customer.relation.importer.service;

import org.dromara.common.excel.importer.ImportResult;
import org.dromara.property.customer.relation.importer.domain.RoomCustomerRelationImportRow;

import java.util.List;

/** 房屋客户关系原子导入服务。 */
public interface IRoomCustomerRelationImportService {
    ImportResult importRows(Long projectId, List<RoomCustomerRelationImportRow> rows);
}
