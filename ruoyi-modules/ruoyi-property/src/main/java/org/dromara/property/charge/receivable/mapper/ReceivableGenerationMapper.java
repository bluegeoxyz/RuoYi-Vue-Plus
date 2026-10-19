package org.dromara.property.charge.receivable.mapper;

import org.apache.ibatis.annotations.*;
import org.dromara.property.charge.binding.domain.ChargeBinding;
import java.time.LocalDate;
import java.util.List;

public interface ReceivableGenerationMapper {
    @Select(
            "SELECT * FROM pm_charge_binding WHERE project_id=#{projectId} AND object_type='ROOM' AND status='0' AND del_flag='0' ORDER BY id")
    List<ChargeBinding> selectProjectRoomBindings(@Param("projectId") Long projectId);

    @Select(
            "SELECT b.id binding_id,b.project_id,b.object_type,b.object_id room_id,b.charge_item_id,b.charge_rule_id,b.effective_start_date binding_start,b.effective_end_date binding_end,"
                    + "r.project_id room_project_id,r.usable_area,r.room_code,r.room_no,bu.building_code,bu.building_name,u.unit_code,u.unit_name,"
                    + "i.project_id item_project_id,i.item_code,i.item_name,cr.project_id rule_project_id,cr.charge_item_id rule_item_id,cr.rule_code,cr.rule_name,cr.billing_method,cr.billing_cycle,cr.price_cycle,cr.cycle_mode,cr.unit_price,cr.fixed_amount,cr.effective_start_date rule_start,cr.effective_end_date rule_end,cr.due_date_mode,cr.due_date_offset_days "
                    + "FROM pm_charge_binding b JOIN pm_room r ON r.id=b.object_id AND r.status='0' AND r.del_flag='0' JOIN pm_building bu ON bu.id=r.building_id AND bu.status='0' AND bu.del_flag='0' LEFT JOIN pm_unit u ON u.id=r.unit_id AND u.status='0' AND u.del_flag='0' JOIN pm_charge_item i ON i.id=b.charge_item_id AND i.status='0' AND i.del_flag='0' JOIN pm_charge_rule cr ON cr.id=b.charge_rule_id AND cr.status='0' AND cr.del_flag='0' WHERE b.id=#{bindingId} AND b.status='0' AND b.del_flag='0'")
    ReceivableGenerationContext selectContext(@Param("bindingId") Long bindingId);

    @Select(
            "SELECT c.id customer_id,c.customer_name,c.customer_type FROM pm_room_customer_rel rel JOIN pm_customer c ON c.id=rel.customer_id AND c.del_flag='0' WHERE rel.room_id=#{roomId} AND rel.relation_type='OWNER' AND rel.status='0' AND rel.del_flag='0' AND rel.start_date<=#{date} AND (rel.end_date IS NULL OR rel.end_date>=#{date}) ORDER BY rel.id")
    List<ResponsibleCustomerSnapshot> selectOwnersOnDate(
            @Param("roomId") Long roomId, @Param("date") LocalDate date);
}
