package org.dromara.property.customer.relation.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDate;

/** 房屋与企业 Customer 的项目业务关系。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_room_customer_rel")
public class RoomCustomerRelation extends BaseEntity {
    @TableId private Long id;
    private Long projectId;
    private Long roomId;
    private Long customerId;
    private String relationType;
    private String isPrimary;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate moveInDate;
    private LocalDate moveOutDate;
    private String relationStatus;
    private String status;
    private String remark;
    @TableLogic private String delFlag;
}
