package org.dromara.warehouse.inventory.transaction.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.inventory.transaction.domain.bo.InventoryTransactionBo;
import org.dromara.warehouse.inventory.transaction.domain.command.InventoryTransactionCommand;
import org.dromara.warehouse.inventory.transaction.domain.vo.InventoryTransactionVo;

import java.util.List;

/** 库存流水只读查询及内部写入服务。 */
public interface IInventoryTransactionService {
    InventoryTransactionVo queryById(Long id);
    PageResult<InventoryTransactionVo> queryPageList(InventoryTransactionBo bo, PageQuery pageQuery);
    List<InventoryTransactionVo> queryList(InventoryTransactionBo bo);
    Long record(InventoryTransactionCommand command);
}
