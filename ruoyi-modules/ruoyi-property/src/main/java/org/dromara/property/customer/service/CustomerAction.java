package org.dromara.property.customer.service;

/** 企业 Customer 主档动作及其系统功能权限。 */
public enum CustomerAction {
    LIST("property:customer:list"),
    DETAIL("property:customer:query"),
    CREATE("property:customer:add"),
    IMPORT("property:customer:import"),
    UPDATE("property:customer:edit"),
    STATUS("property:customer:edit"),
    DELETE("property:customer:remove"),
    EXPORT("property:customer:export"),
    OPTION("property:customer:query");

    private final String permission;

    CustomerAction(String permission) {
        this.permission = permission;
    }

    public String permission() {
        return permission;
    }
}
