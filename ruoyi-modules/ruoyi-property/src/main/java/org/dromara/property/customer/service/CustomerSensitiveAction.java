package org.dromara.property.customer.service;

/** Customer 敏感资料能力，查看与导出严格分离。 */
public enum CustomerSensitiveAction {
    PII_VIEW("property:customer:pii:view"),
    PII_EXPORT("property:customer:pii:export");

    private final String permission;

    CustomerSensitiveAction(String permission) {
        this.permission = permission;
    }

    public String permission() {
        return permission;
    }
}
