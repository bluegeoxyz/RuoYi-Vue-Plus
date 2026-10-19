package org.dromara.property.scope;

import java.util.Set;

/** 已解析的项目范围；空集合绝不表示全量。 */
public record PropertyProjectScope(Type type, Set<Long> projectIds) {
    public enum Type { NONE, EXPLICIT_IDS, ALL }

    public static PropertyProjectScope none() {
        return new PropertyProjectScope(Type.NONE, Set.of());
    }

    public static PropertyProjectScope all() {
        return new PropertyProjectScope(Type.ALL, Set.of());
    }

    public static PropertyProjectScope explicit(Set<Long> projectIds) {
        return projectIds == null || projectIds.isEmpty() ? none() : new PropertyProjectScope(Type.EXPLICIT_IDS, Set.copyOf(projectIds));
    }

    public boolean contains(Long projectId) {
        return type == Type.ALL || type == Type.EXPLICIT_IDS && projectIds.contains(projectId);
    }
}
