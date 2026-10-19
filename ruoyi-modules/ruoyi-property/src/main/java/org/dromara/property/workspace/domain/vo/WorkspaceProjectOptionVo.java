package org.dromara.property.workspace.domain.vo;

import java.io.Serializable;

/** 当前登录用户可用的工作项目。项目 ID 使用字符串，避免前端整数精度丢失。 */
public record WorkspaceProjectOptionVo(
    String id,
    String projectCode,
    String projectName,
    String projectShortName
) implements Serializable {
}
