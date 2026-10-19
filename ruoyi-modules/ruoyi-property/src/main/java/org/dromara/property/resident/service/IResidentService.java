package org.dromara.property.resident.service;
import org.dromara.common.core.domain.PageResult; import org.dromara.common.mybatis.core.page.PageQuery; import org.dromara.property.resident.domain.bo.ResidentQueryBo; import org.dromara.property.resident.domain.vo.*; import java.util.List;
/** 项目住户查询服务。 */ public interface IResidentService { PageResult<ResidentRelationVo> queryPage(ResidentQueryBo bo,PageQuery pageQuery); ResidentDetailVo queryDetail(Long customerId,Long projectId); List<ResidentExportVo> queryExport(ResidentQueryBo bo); }
