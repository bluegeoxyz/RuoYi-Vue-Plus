package org.dromara.common.security.business;

/**
 * 业务上下文权限组合模式。
 */
public enum PermissionMode {

    /** 满足任意一个权限即可。 */
    ANY,

    /** 必须满足全部权限。 */
    ALL
}
