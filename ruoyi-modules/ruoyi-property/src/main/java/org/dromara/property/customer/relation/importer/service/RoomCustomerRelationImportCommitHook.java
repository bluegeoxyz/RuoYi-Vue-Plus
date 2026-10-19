package org.dromara.property.customer.relation.importer.service;

import org.dromara.property.customer.relation.importer.domain.RoomCustomerRelationImportRow;

/** 测试与观测用提交阶段扩展点；生产环境默认无实现。 */
public interface RoomCustomerRelationImportCommitHook {
    default void beforeCommit(Long projectId) { }
    default void beforeInsert(int index, RoomCustomerRelationImportRow row) { }
}
