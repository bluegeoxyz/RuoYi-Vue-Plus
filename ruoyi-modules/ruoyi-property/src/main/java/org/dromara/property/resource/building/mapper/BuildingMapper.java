package org.dromara.property.resource.building.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.domain.vo.BuildingVo;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 楼栋数据层。
 */
public interface BuildingMapper extends BaseMapperPlus<Building, BuildingVo> {
    @Select("SELECT * FROM pm_building WHERE id=#{id} AND del_flag='0' FOR UPDATE")
    Building selectActiveByIdForUpdate(@Param("id") Long id);

    @Select("SELECT * FROM pm_building WHERE project_id=#{projectId} AND building_code=#{code} AND del_flag='0' FOR UPDATE")
    Building selectActiveByProjectAndCodeForUpdate(@Param("projectId") Long projectId, @Param("code") String code);

    /**
     * 锁定楼栋下的单元索引范围，阻止并发新增单元。
     */
    @Select("SELECT id FROM pm_unit FORCE INDEX (idx_pm_unit_building) WHERE building_id = #{buildingId} FOR UPDATE")
    List<Long> selectUnitIdsForUpdate(@Param("buildingId") Long buildingId);

    /**
     * 锁定楼栋下的房屋索引范围，阻止并发新增房屋。
     */
    @Select("SELECT id FROM pm_room FORCE INDEX (idx_pm_room_building) WHERE building_id = #{buildingId} FOR UPDATE")
    List<Long> selectRoomIdsForUpdate(@Param("buildingId") Long buildingId);

    @Select("SELECT EXISTS(SELECT 1 FROM pm_unit WHERE building_id = #{buildingId} AND del_flag = '0') OR EXISTS(SELECT 1 FROM pm_room WHERE building_id = #{buildingId} AND del_flag = '0')")
    boolean existsActiveChildren(Long buildingId);

    default long countActiveCode(Long projectId, String code, Long excludeId) {
        return lambda().eq(Building::getProjectId, projectId).eq(Building::getBuildingCode, code)
            .ne(excludeId != null, Building::getId, excludeId).count();
    }
}
