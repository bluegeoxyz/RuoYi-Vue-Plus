package org.dromara.property.resident.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper; import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*; import org.dromara.property.customer.relation.domain.RoomCustomerRelation; import org.dromara.property.resident.domain.bo.ResidentQueryBo; import org.dromara.property.resident.domain.vo.ResidentRelationVo;
import java.util.*;
/** 住户关系视图联表查询；一条 Relation 对应一行。 */
public interface ResidentMapper extends BaseMapper<RoomCustomerRelation> {
 String SELECT="SELECT c.id customer_id,c.customer_no,c.customer_type,c.status customer_status,c.lifecycle_status customer_lifecycle_status,"+
  "CASE WHEN c.customer_name IS NULL THEN NULL WHEN c.customer_type='COMPANY' THEN c.customer_name WHEN CHAR_LENGTH(c.customer_name)&lt;=1 THEN '*' ELSE CONCAT(LEFT(c.customer_name,1),REPEAT('*',LEAST(CHAR_LENGTH(c.customer_name)-1,3))) END masked_customer_name,"+
  "CASE WHEN c.mobile IS NULL OR c.mobile='' THEN NULL WHEN CHAR_LENGTH(c.mobile)&gt;=7 THEN CONCAT(LEFT(c.mobile,3),'****',RIGHT(c.mobile,4)) ELSE REPEAT('*',CHAR_LENGTH(c.mobile)) END masked_mobile,"+
  "r.project_id,p.project_name,rm.building_id,b.building_name,b.building_code,rm.unit_id,u.unit_name,u.unit_code,r.room_id,rm.room_code,rm.room_no,r.id relation_id,r.relation_type,r.relation_status,r.is_primary,r.start_date,r.end_date,r.move_in_date,r.move_out_date "+
  "FROM pm_room_customer_rel r JOIN pm_customer c ON c.id=r.customer_id AND c.del_flag='0' JOIN pm_room rm ON rm.id=r.room_id AND rm.del_flag='0' JOIN pm_building b ON b.id=rm.building_id AND b.del_flag='0' JOIN pm_unit u ON u.id=rm.unit_id AND u.del_flag='0' JOIN pm_project p ON p.id=r.project_id AND p.del_flag='0' ";
 String FILTER="WHERE r.del_flag='0' AND r.status='0' "+
  "<if test='bo.projectId != null'>AND r.project_id=#{bo.projectId} </if><if test='bo.keyword != null and bo.keyword != \"\"'>AND (c.customer_no LIKE CONCAT('%',#{bo.keyword},'%') OR c.customer_name LIKE CONCAT('%',#{bo.keyword},'%') OR rm.room_code LIKE CONCAT('%',#{bo.keyword},'%') OR rm.room_no LIKE CONCAT('%',#{bo.keyword},'%')) </if>"+
  "<if test='bo.relationType != null and bo.relationType != \"\"'>AND r.relation_type=#{bo.relationType} </if><if test='bo.relationStatus == \"EFFECTIVE\"'>AND r.relation_status IN ('ACTIVE','PENDING') </if><if test='bo.relationStatus != null and bo.relationStatus != \"\" and bo.relationStatus != \"EFFECTIVE\"'>AND r.relation_status=#{bo.relationStatus} </if>"+
  "<if test='bo.buildingId != null'>AND rm.building_id=#{bo.buildingId} </if><if test='bo.unitId != null'>AND rm.unit_id=#{bo.unitId} </if><if test='bo.roomId != null'>AND r.room_id=#{bo.roomId} </if>"+
  "<if test='bo.moveInDateFrom != null'>AND r.move_in_date&gt;=#{bo.moveInDateFrom} </if><if test='bo.moveInDateTo != null'>AND r.move_in_date&lt;=#{bo.moveInDateTo} </if><if test='bo.moveOutDateFrom != null'>AND r.move_out_date&gt;=#{bo.moveOutDateFrom} </if><if test='bo.moveOutDateTo != null'>AND r.move_out_date&lt;=#{bo.moveOutDateTo} </if>"+
  "<if test='projectIds != null'>AND r.project_id IN <foreach collection='projectIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> </if>";
 @Select("<script>"+SELECT+FILTER+"ORDER BY r.create_time DESC,r.id DESC</script>") Page<ResidentRelationVo> selectPage(Page<ResidentRelationVo> page,@Param("bo") ResidentQueryBo bo,@Param("projectIds") Collection<Long> projectIds);
 @Select("<script>"+SELECT+FILTER+"ORDER BY r.create_time DESC,r.id DESC</script>") List<ResidentRelationVo> selectList(@Param("bo") ResidentQueryBo bo,@Param("projectIds") Collection<Long> projectIds);
 @Select("<script>"+SELECT+"WHERE r.del_flag='0' AND r.status='0' AND r.customer_id=#{customerId} <if test='projectId != null'>AND r.project_id=#{projectId} </if><if test='projectIds != null'>AND r.project_id IN <foreach collection='projectIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> </if>ORDER BY r.start_date DESC,r.id DESC</script>")
 List<ResidentRelationVo> selectCustomerRelations(@Param("customerId") Long customerId,@Param("projectId") Long projectId,@Param("projectIds") Collection<Long> projectIds);
}
