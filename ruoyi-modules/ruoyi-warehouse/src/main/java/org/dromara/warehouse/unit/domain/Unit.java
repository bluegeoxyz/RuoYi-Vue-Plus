package org.dromara.warehouse.unit.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/** 计量单位实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wm_unit")
public class Unit extends BaseEntity {
    @Serial private static final long serialVersionUID = 1L;
    @TableId private Long id;
    private String unitCode;
    private String unitName;
    private String symbol;
    private Integer precisionScale;
    private String status;
    private Integer sort;
    private String remark;
    @TableLogic private String delFlag;
}
