package org.dromara.property.resource.unit.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.domain.vo.BuildingUnitVo;

import java.util.List;

/**
 * 楼栋单元数据层。
 */
public interface BuildingUnitMapper extends BaseMapperPlus<BuildingUnit, BuildingUnitVo> {
    @Select("SELECT * FROM pm_unit WHERE id = #{id} AND del_flag = '0' FOR UPDATE")
    BuildingUnit selectActiveByIdForUpdate(@Param("id") Long id);

    @Select("SELECT * FROM pm_unit WHERE building_id=#{buildingId} AND unit_code=#{code} AND del_flag='0' FOR UPDATE")
    BuildingUnit selectActiveByBuildingAndCodeForUpdate(@Param("buildingId") Long buildingId, @Param("code") String code);

    @Select("SELECT EXISTS(SELECT 1 FROM pm_room WHERE unit_id = #{unitId} AND del_flag = '0')")
    boolean existsActiveRoom(@Param("unitId") Long unitId);

    /**
     * 锁定该单元的房屋索引范围；即使当前没有房屋，也通过 next-key lock 阻止并发插入。
     */
    @Select("SELECT id FROM pm_room FORCE INDEX (idx_pm_room_unit) WHERE unit_id = #{unitId} FOR UPDATE")
    List<Long> selectRoomIdsForUpdate(@Param("unitId") Long unitId);

    default long countActiveCode(Long buildingId, String code, Long excludeId) {
        return lambda().eq(BuildingUnit::getBuildingId, buildingId)
            .eq(BuildingUnit::getUnitCode, code)
            .ne(excludeId != null, BuildingUnit::getId, excludeId)
            .count();
    }
}
