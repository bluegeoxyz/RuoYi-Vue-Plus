package org.dromara.warehouse.unit.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.warehouse.unit.domain.Unit;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 计量单位视图对象。 */
@Data
@AutoMapper(target = Unit.class)
public class UnitVo implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long id;
    private String unitCode;
    private String unitName;
    private String symbol;
    private Integer precisionScale;
    private String status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
