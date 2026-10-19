package org.dromara.warehouse.context.domain.vo;

import java.io.Serializable;

/** 仓库业务上下文中的精简公司选项。 */
public record ContextCompanyOptionVo(Long deptId, String deptName) implements Serializable {
}
