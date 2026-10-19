package org.dromara.property.customer.importer.service;

import org.dromara.property.customer.domain.Customer;

/** Customer 导入提交观察点，仅供真实并发验收精确制造竞态。 */
public interface CustomerImportCommitHook {
    default void beforeCommit() { }
    default void beforeInsert(int index, Customer customer) { }
}
