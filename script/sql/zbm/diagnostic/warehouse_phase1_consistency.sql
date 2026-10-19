/*
 * Warehouse Phase 1 库存一致性诊断。
 * 所有查询均应返回 0 行；只读，可在发布验收或故障排查时执行。
 */

/* 当前余额必须等于同仓库、库位、物料维度的流水累计。 */
SELECT i.warehouse_id,
       i.location_id,
       i.material_id,
       i.quantity AS inventory_quantity,
       COALESCE(SUM(t.quantity_change), 0) AS transaction_quantity
FROM wm_inventory i
LEFT JOIN wm_inventory_transaction t
       ON t.warehouse_id = i.warehouse_id
      AND t.location_id = i.location_id
      AND t.material_id = i.material_id
GROUP BY i.warehouse_id, i.location_id, i.material_id, i.quantity
HAVING i.quantity <> COALESCE(SUM(t.quantity_change), 0);

/* 不允许存在只有流水、没有余额的数据维度。 */
SELECT t.warehouse_id,
       t.location_id,
       t.material_id,
       SUM(t.quantity_change) AS transaction_quantity
FROM wm_inventory_transaction t
LEFT JOIN wm_inventory i
       ON i.warehouse_id = t.warehouse_id
      AND i.location_id = t.location_id
      AND i.material_id = t.material_id
WHERE i.id IS NULL
GROUP BY t.warehouse_id, t.location_id, t.material_id;

/* 每条流水必须满足 before + change = after。 */
SELECT id, transaction_no, quantity_before, quantity_change, quantity_after
FROM wm_inventory_transaction
WHERE quantity_before + quantity_change <> quantity_after;

/* 正式库存流水的方向必须与业务类型一致。 */
SELECT id, transaction_no, transaction_type, quantity_change
FROM wm_inventory_transaction
WHERE (transaction_type IN ('INBOUND', 'TRANSFER_IN', 'STOCKTAKE_GAIN') AND quantity_change <= 0)
   OR (transaction_type IN ('OUTBOUND', 'TRANSFER_OUT', 'STOCKTAKE_LOSS') AND quantity_change >= 0);

/* 正式库存变化必须携带正确来源。 */
SELECT id, transaction_no, transaction_type, source_type, source_id, source_no
FROM wm_inventory_transaction
WHERE source_id IS NULL
   OR source_no IS NULL
   OR (transaction_type = 'INBOUND' AND source_type <> 'INBOUND_ORDER')
   OR (transaction_type = 'OUTBOUND' AND source_type <> 'OUTBOUND_ORDER')
   OR (transaction_type IN ('TRANSFER_IN', 'TRANSFER_OUT') AND source_type <> 'TRANSFER_ORDER')
   OR (transaction_type IN ('STOCKTAKE_GAIN', 'STOCKTAKE_LOSS') AND source_type <> 'STOCKTAKE_ORDER');
