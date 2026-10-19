-- TASK-015 房屋入住状态一致性审计（只读）。
-- 判定口径：未删除且启用的 ACTIVE 房屋-客户关系均计入住，不区分 relation_type。

-- 异常 A：存在 ACTIVE 关系，但房屋不是 OCCUPIED。
SELECT r.id,
       r.project_id,
       r.room_code,
       r.occupancy_status,
       COUNT(rc.id) AS active_relation_count
FROM pm_room r
JOIN pm_room_customer_rel rc
  ON rc.room_id = r.id
 AND rc.relation_status = 'ACTIVE'
 AND rc.status = '0'
 AND rc.del_flag = '0'
WHERE r.del_flag = '0'
  AND r.occupancy_status <> 'OCCUPIED'
GROUP BY r.id, r.project_id, r.room_code, r.occupancy_status;

-- 异常 B：不存在 ACTIVE 关系，但房屋不是 VACANT。
SELECT r.id,
       r.project_id,
       r.room_code,
       r.occupancy_status
FROM pm_room r
WHERE r.del_flag = '0'
  AND r.occupancy_status <> 'VACANT'
  AND NOT EXISTS (
      SELECT 1
      FROM pm_room_customer_rel rc
      WHERE rc.room_id = r.id
        AND rc.relation_status = 'ACTIVE'
        AND rc.status = '0'
        AND rc.del_flag = '0'
  );
