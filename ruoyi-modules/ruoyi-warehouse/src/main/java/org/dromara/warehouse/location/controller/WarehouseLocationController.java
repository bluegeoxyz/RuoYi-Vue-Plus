package org.dromara.warehouse.location.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.warehouse.context.domain.vo.WarehouseEditInitializationVo;
import org.dromara.warehouse.context.service.WarehouseEditInitializationService;
import org.dromara.warehouse.location.domain.bo.WarehouseLocationBo;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationOptionVo;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationVo;
import org.dromara.warehouse.location.service.IWarehouseLocationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 库位管理。 */
@Validated @RestController @RequiredArgsConstructor @RequestMapping("/warehouse/location")
public class WarehouseLocationController extends BaseController {
    private final IWarehouseLocationService locationService;
    private final WarehouseEditInitializationService editInitializationService;
    @SaCheckPermission("warehouse:location:list") @GetMapping("/list")
    public R<PageResult<WarehouseLocationVo>> list(WarehouseLocationBo bo, PageQuery pageQuery) { return R.ok(locationService.queryPageList(bo, pageQuery)); }
    @SaCheckPermission("warehouse:location:query") @GetMapping("/{id}")
    public R<WarehouseLocationVo> getInfo(@NotNull(message = "库位ID不能为空") @PathVariable Long id) { return R.ok(locationService.queryById(id)); }
    @SaCheckPermission("warehouse:location:edit") @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.LocationEdit> editContext(@NotNull(message = "库位ID不能为空") @PathVariable Long id) { return R.ok(editInitializationService.location(id)); }
    @SaCheckPermission("warehouse:location:add") @Log(title = "库位", businessType = BusinessType.INSERT) @RepeatSubmit @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WarehouseLocationBo bo) { return toAjax(locationService.insertByBo(bo)); }
    @SaCheckPermission("warehouse:location:edit") @Log(title = "库位", businessType = BusinessType.UPDATE) @RepeatSubmit @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody WarehouseLocationBo bo) { return toAjax(locationService.updateByBo(bo)); }
    @SaCheckPermission("warehouse:location:edit") @Log(title = "库位状态", businessType = BusinessType.UPDATE) @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody WarehouseLocationBo bo) { return toAjax(locationService.changeStatus(bo.getId(), bo.getStatus())); }
    @SaCheckPermission("warehouse:location:remove") @Log(title = "库位", businessType = BusinessType.DELETE) @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "库位ID不能为空") @PathVariable Long[] ids) { return toAjax(locationService.deleteWithValidByIds(List.of(ids))); }
    @SaCheckPermission("warehouse:location:list") @GetMapping("/options")
    public R<List<WarehouseLocationOptionVo>> options(@NotNull(message = "仓库ID不能为空") @RequestParam Long warehouseId,
                                                      @RequestParam(required = false) Long zoneId) { return R.ok(locationService.selectOptions(warehouseId, zoneId)); }
}
