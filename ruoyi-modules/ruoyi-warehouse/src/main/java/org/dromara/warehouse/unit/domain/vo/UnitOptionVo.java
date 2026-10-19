package org.dromara.warehouse.unit.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.warehouse.unit.domain.Unit;

import java.io.Serial;
import java.io.Serializable;

/** 启用计量单位选项。 */
@Data
@AutoMapper(target = Unit.class)
public class UnitOptionVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private String unitCode;
    private String unitName;
    private String symbol;
    private Integer precisionScale;
}
