# ruoyi-hr

企业 HR 业务模块，负责员工档案、组织扩展和人事基础能力。

- Java 根包：`org.dromara.hr`
- 依赖方向：`ruoyi-hr -> ruoyi-system`
- 禁止依赖 `ruoyi-property`
- `sys_user` 表示登录身份，`sys_dept` 表示企业部门，`sys_post` 表示企业岗位
- 员工主数据归属 `hr_employee`，不向 `sys_user` 追加员工专属字段

顶层业务包按需使用 `employee`、`organization`、`personnel`、`common`。跨模块员工查询能力应在 `employee.service` 中通过 Service、QueryService 或 Facade 暴露；没有真实调用前不创建空接口。
