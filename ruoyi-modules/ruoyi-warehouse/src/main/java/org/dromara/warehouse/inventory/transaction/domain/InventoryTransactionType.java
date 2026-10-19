package org.dromara.warehouse.inventory.transaction.domain;

import java.util.Set;

/** Phase 1 库存流水类型及数量方向。 */
public enum InventoryTransactionType {
    INBOUND(1), OUTBOUND(-1), TRANSFER_IN(1), TRANSFER_OUT(-1), STOCKTAKE_GAIN(1), STOCKTAKE_LOSS(-1);

    private final int direction;

    InventoryTransactionType(int direction) { this.direction = direction; }

    public boolean accepts(int sign) { return sign == direction; }

    public static InventoryTransactionType require(String value) {
        return Set.of(values()).stream().filter(type -> type.name().equals(value)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("库存流水类型不合法"));
    }
}
