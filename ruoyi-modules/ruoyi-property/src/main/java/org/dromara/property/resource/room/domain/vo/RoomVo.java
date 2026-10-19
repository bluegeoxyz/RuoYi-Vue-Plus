package org.dromara.property.resource.room.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.property.resource.room.domain.Room;

@Data
@EqualsAndHashCode(callSuper = false)
@AutoMapper(target = Room.class)
public class RoomVo extends Room {
}
