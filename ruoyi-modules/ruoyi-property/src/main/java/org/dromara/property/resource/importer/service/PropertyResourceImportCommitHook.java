package org.dromara.property.resource.importer.service;

import org.dromara.property.resource.importer.domain.PropertyResourceImportRow;

/**
 * 原子导入提交观察点，默认无实现；仅用于并发验收在 INSERT 前精确制造竞态。
 */
public interface PropertyResourceImportCommitHook {
    default void beforeCommit(Long projectId) {
    }

    default void beforeRoomInsert(Long projectId, PropertyResourceImportRow row) {
    }
}
