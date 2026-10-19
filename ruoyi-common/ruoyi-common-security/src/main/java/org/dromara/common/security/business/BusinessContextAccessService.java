package org.dromara.common.security.business;

/**
 * 业务上下文能力授权服务。
 *
 * <p>该服务不查询引用数据，也不代替项目、公司、仓库或具体资源范围校验。</p>
 */
public interface BusinessContextAccessService {

    /**
     * 要求当前登录用户有权在指定上下文读取指定引用类型。
     *
     * @param context 受控业务上下文
     * @param referenceType 受控引用类型
     */
    void requireAccess(BusinessContext context, ReferenceType referenceType);
}
