package org.dromara.warehouse.zone.controller;

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
import org.dromara.warehouse.zone.domain.bo.WarehouseZoneBo;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneOptionVo;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneVo;
import org.dromara.warehouse.zone.service.IWarehouseZoneService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 库区管理。 */
@Validated @RestController @RequiredArgsConstructor @RequestMapping("/warehouse/zone")
public class WarehouseZoneController extends BaseController {
    private final IWarehouseZoneService zoneService;
    private final WarehouseEditInitializationService editInitializationService;
    @SaCheckPermission("warehouse:zone:list") @GetMapping("/list")
    public R<PageResult<WarehouseZoneVo>> list(WarehouseZoneBo bo, PageQuery pageQuery) { return R.ok(zoneService.queryPageList(bo, pageQuery)); }
    @SaCheckPermission("warehouse:zone:query") @GetMapping("/{id}")
    public R<WarehouseZoneVo> getInfo(@NotNull(message = "库区ID不能为空") @PathVariable Long id) { return R.ok(zoneService.queryById(id)); }
    @SaCheckPermission("warehouse:zone:edit") @GetMapping("/{id}/context/edit")
    public R<WarehouseEditInitializationVo.ZoneEdit> editContext(@NotNull(message = "库区ID不能为空") @PathVariable Long id) { return R.ok(editInitializationService.zone(id)); }
    @SaCheckPermission("warehouse:zone:add") @Log(title = "库区", businessType = BusinessType.INSERT) @RepeatSubmit @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WarehouseZoneBo bo) { return toAjax(zoneService.insertByBo(bo)); }
    @SaCheckPermission("warehouse:zone:edit") @Log(title = "库区", businessType = BusinessType.UPDATE) @RepeatSubmit @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody WarehouseZoneBo bo) { return toAjax(zoneService.updateByBo(bo)); }
    @SaCheckPermission("warehouse:zone:edit") @Log(title = "库区状态", businessType = BusinessType.UPDATE) @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody WarehouseZoneBo bo) { return toAjax(zoneService.changeStatus(bo.getId(), bo.getStatus())); }
    @SaCheckPermission("warehouse:zone:remove") @Log(title = "库区", businessType = BusinessType.DELETE) @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "库区ID不能为空") @PathVariable Long[] ids) { return toAjax(zoneService.deleteWithValidByIds(List.of(ids))); }
    @SaCheckPermission("warehouse:zone:list") @GetMapping("/options")
    public R<List<WarehouseZoneOptionVo>> options(@NotNull(message = "仓库ID不能为空") @RequestParam Long warehouseId) { return R.ok(zoneService.selectOptions(warehouseId)); }
}
