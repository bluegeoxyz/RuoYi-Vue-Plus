package org.dromara.common.security.business;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;

/**
 * 基于 Sa-Token 当前会话权限的业务上下文授权实现。
 */
@Service
public class BusinessContextAccessServiceImpl implements BusinessContextAccessService {

    @Override
    public void requireAccess(BusinessContext context, ReferenceType referenceType) {
        StpUtil.checkLogin();
        BusinessContextPolicy policy = BusinessContextPolicy.find(context)
            .orElseThrow(() -> denied("未声明的业务上下文"));
        if (referenceType == null || !policy.referenceTypes().contains(referenceType)) {
            throw denied("业务上下文未授权该引用类型");
        }
        boolean permitted = switch (policy.mode()) {
            case ANY -> policy.permissions().stream().anyMatch(StpUtil::hasPermission);
            case ALL -> policy.permissions().stream().allMatch(StpUtil::hasPermission);
        };
        if (!permitted) {
            throw denied("缺少业务上下文权限");
        }
    }

    private static NotPermissionException denied(String message) {
        return new NotPermissionException(message);
    }
}
