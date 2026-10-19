package org.dromara.property.api;

import java.util.Set;

/**
 * 跨模块项目访问范围，空集合不表示全量。
 */
public record ProjectAccessScope(Type type, Set<Long> projectIds) {

    /** 范围类型。 */
    public enum Type { NONE, EXPLICIT_IDS, ALL }

    public ProjectAccessScope {
        projectIds = projectIds == null ? Set.of() : Set.copyOf(projectIds);
    }

    public boolean contains(Long projectId) {
        return type == Type.ALL || type == Type.EXPLICIT_IDS && projectIds.contains(projectId);
    }
}
