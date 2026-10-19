package org.dromara.property.resource.building.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.property.resource.building.domain.Building;

/**
 * 楼栋展示对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AutoMapper(target = Building.class)
public class BuildingVo extends Building {
}
