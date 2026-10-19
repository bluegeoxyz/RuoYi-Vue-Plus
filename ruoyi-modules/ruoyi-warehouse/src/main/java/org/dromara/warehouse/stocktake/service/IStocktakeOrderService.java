package org.dromara.warehouse.stocktake.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.warehouse.stocktake.domain.bo.StocktakeOrderBo;
import org.dromara.warehouse.stocktake.domain.vo.StocktakeOrderVo;
import java.util.*;

public interface IStocktakeOrderService {
  StocktakeOrderVo queryById(Long id);

  PageResult<StocktakeOrderVo> queryPageList(StocktakeOrderBo bo, PageQuery pq);

  List<StocktakeOrderVo> queryList(StocktakeOrderBo bo);

  Long create(StocktakeOrderBo bo);

  void update(StocktakeOrderBo bo);

  void confirm(Long id);

  void complete(Long id);

  void cancel(Long id);

  void deleteDrafts(Collection<Long> ids);
}
