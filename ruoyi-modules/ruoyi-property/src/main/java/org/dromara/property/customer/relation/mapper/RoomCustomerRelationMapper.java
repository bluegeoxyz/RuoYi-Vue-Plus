package org.dromara.property.customer.relation.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.customer.relation.domain.RoomCustomerRelation;
import org.dromara.property.customer.relation.domain.bo.RoomCustomerRelationBo;
import org.dromara.property.customer.relation.domain.vo.RoomCustomerRelationVo;

import java.util.Collection;
import java.util.List;

public interface RoomCustomerRelationMapper extends BaseMapperPlus<RoomCustomerRelation, RoomCustomerRelationVo> {
    String DISPLAY_SELECT_PREFIX = "SELECT r.*, b.building_code, b.building_name, u.unit_code, u.unit_name, " +
        "rm.room_code, rm.room_no, c.customer_no, " +
        "CASE WHEN c.customer_name IS NULL THEN NULL WHEN CHAR_LENGTH(c.customer_name)";
    String DISPLAY_SELECT_SUFFIX = "1 THEN '*' " +
        "ELSE CONCAT(LEFT(c.customer_name,1),REPEAT('*',LEAST(CHAR_LENGTH(c.customer_name)-1,3))) END customer_name, " +
        "CASE WHEN c.mobile IS NULL OR c.mobile='' THEN NULL WHEN CHAR_LENGTH(c.mobile)>=7 " +
        "THEN CONCAT(LEFT(c.mobile,3),'****',RIGHT(c.mobile,4)) ELSE REPEAT('*',CHAR_LENGTH(c.mobile)) END mobile " +
        "FROM pm_room_customer_rel r " +
        "LEFT JOIN pm_room rm ON rm.id=r.room_id AND rm.del_flag='0' " +
        "LEFT JOIN pm_building b ON b.id=rm.building_id AND b.del_flag='0' " +
        "LEFT JOIN pm_unit u ON u.id=rm.unit_id AND u.del_flag='0' " +
        "LEFT JOIN pm_customer c ON c.id=r.customer_id AND c.del_flag='0' ";
    String DISPLAY_SELECT = DISPLAY_SELECT_PREFIX + "<= " + DISPLAY_SELECT_SUFFIX;
    /** Dynamic SQL is parsed as XML, so the less-than sign must be escaped in the script variant. */
    String DISPLAY_SELECT_SCRIPT = DISPLAY_SELECT_PREFIX + "&lt;= " + DISPLAY_SELECT_SUFFIX;

    @Select("<script>" + DISPLAY_SELECT_SCRIPT +
        "WHERE r.del_flag='0' " +
        "<if test='bo.projectId != null'>AND r.project_id=#{bo.projectId} </if>" +
        "<if test='bo.roomId != null'>AND r.room_id=#{bo.roomId} </if>" +
        "<if test='bo.customerId != null'>AND r.customer_id=#{bo.customerId} </if>" +
        "<if test='bo.relationType != null and bo.relationType != \"\"'>AND r.relation_type=#{bo.relationType} </if>" +
        "<if test='bo.relationStatus != null and bo.relationStatus != \"\"'>AND r.relation_status=#{bo.relationStatus} </if>" +
        "<if test='bo.status != null and bo.status != \"\"'>AND r.status=#{bo.status} </if>" +
        "<if test='projectIds != null'>AND r.project_id IN <foreach collection='projectIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> </if>" +
        "ORDER BY r.create_time DESC</script>")
    Page<RoomCustomerRelationVo> selectDisplayPage(Page<RoomCustomerRelationVo> page,
                                                    @Param("bo") RoomCustomerRelationBo bo,
                                                    @Param("projectIds") Collection<Long> projectIds);

    @Select("<script>" + DISPLAY_SELECT_SCRIPT +
        "WHERE r.del_flag='0' " +
        "<if test='bo.projectId != null'>AND r.project_id=#{bo.projectId} </if>" +
        "<if test='bo.roomId != null'>AND r.room_id=#{bo.roomId} </if>" +
        "<if test='bo.customerId != null'>AND r.customer_id=#{bo.customerId} </if>" +
        "<if test='bo.relationType != null and bo.relationType != \"\"'>AND r.relation_type=#{bo.relationType} </if>" +
        "<if test='bo.relationStatus != null and bo.relationStatus != \"\"'>AND r.relation_status=#{bo.relationStatus} </if>" +
        "<if test='bo.status != null and bo.status != \"\"'>AND r.status=#{bo.status} </if>" +
        "<if test='projectIds != null'>AND r.project_id IN <foreach collection='projectIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> </if>" +
        "ORDER BY r.create_time DESC</script>")
    List<RoomCustomerRelationVo> selectDisplayList(@Param("bo") RoomCustomerRelationBo bo,
                                                    @Param("projectIds") Collection<Long> projectIds);

    @Select(DISPLAY_SELECT + "WHERE r.id=#{id} AND r.del_flag='0'")
    RoomCustomerRelationVo selectDisplayById(@Param("id") Long id);

    @Select("SELECT * FROM pm_room_customer_rel WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    RoomCustomerRelation selectActiveByIdForUpdate(@Param("id") Long id);

    @Select("SELECT id FROM pm_room_customer_rel FORCE INDEX (idx_pm_room_customer_relation) " +
        "WHERE room_id=#{roomId} AND customer_id=#{customerId} AND relation_type=#{relationType} " +
        "AND relation_status IN ('PENDING','ACTIVE') AND status='0' AND del_flag='0' FOR UPDATE")
    List<Long> selectEffectiveDuplicatesForUpdate(@Param("roomId") Long roomId,
                                                   @Param("customerId") Long customerId,
                                                   @Param("relationType") String relationType);

    /** 所有正常、未删除的 ACTIVE 关系均按现有生命周期语义贡献入住状态。 */
    @Select("SELECT id FROM pm_room_customer_rel FORCE INDEX (idx_pm_room_customer_room) WHERE room_id=#{roomId} " +
        "AND relation_status='ACTIVE' AND status='0' AND del_flag='0' FOR UPDATE")
    List<Long> selectActiveOccupancyRelationIdsForUpdate(@Param("roomId") Long roomId);
}
