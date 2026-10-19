package org.dromara.hr.employee.service;

import java.util.Collection;
import java.util.Optional;

/** 员工删除前的跨模块业务引用检查扩展点。 */
public interface EmployeeReferenceChecker {

    /**
     * 检查员工是否被外部业务引用。
     *
     * @param employeeIds 待删除员工ID
     * @return 存在引用时返回阻止删除的原因
     */
    Optional<String> findDeleteBlockReason(Collection<Long> employeeIds);
}
