package org.dromara.hr.employee.service;

import cn.hutool.core.lang.tree.Tree;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.hr.employee.domain.bo.EmployeeBo;
import org.dromara.hr.employee.domain.bo.EmployeeUserOptionQuery;
import org.dromara.hr.employee.domain.vo.EmployeeExportVo;
import org.dromara.hr.employee.domain.vo.EmployeeOptionVo;
import org.dromara.hr.employee.domain.vo.EmployeeVo;
import org.dromara.hr.employee.domain.vo.EmployeeUserOptionVo;
import org.dromara.system.domain.vo.SysPostVo;

import java.util.Collection;
import java.util.List;

/** 员工档案服务。 */
public interface IEmployeeService {
    EmployeeVo queryById(Long id);
    PageResult<EmployeeVo> queryPageList(EmployeeBo bo, PageQuery pageQuery);
    List<EmployeeExportVo> queryExportList(EmployeeBo bo);
    Boolean insertByBo(EmployeeBo bo);
    Boolean updateByBo(EmployeeBo bo);
    Boolean changeStatus(Long id, String status);
    Boolean bindUser(Long employeeId, Long userId);
    Boolean unbindUser(Long employeeId);
    PageResult<EmployeeUserOptionVo> selectUserOptions(EmployeeUserOptionQuery query, PageQuery pageQuery);
    Boolean deleteWithValidByIds(Collection<Long> ids);
    List<EmployeeOptionVo> selectEmployeeOptions();
    List<EmployeeOptionVo> selectEmployeeOptionsByIds(Collection<Long> ids);
    /** 按账号查询有效、启用的员工 ID；未绑定或无效时返回 null。 */
    Long findActiveEmployeeIdByUserId(Long userId);
    List<Tree<Long>> selectDeptTree();
    List<SysPostVo> selectPostOptions(Long deptId);
}
