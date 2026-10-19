package org.dromara.property.resource.room.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.*;
import org.dromara.common.core.validate.*;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.property.resource.room.domain.bo.RoomBo;
import org.dromara.property.resource.room.domain.vo.RoomVo;
import org.dromara.property.resource.room.domain.vo.RoomExportVo;
import org.dromara.property.resource.room.service.IRoomService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/property/resource/room")
public class RoomController extends BaseController {
    private final IRoomService service;

    @SaCheckPermission("property:room:list")
    @GetMapping("/list")
    public R<PageResult<RoomVo>> list(RoomBo bo, PageQuery pq) {
        return R.ok(service.queryPage(bo, pq));
    }

    @SaCheckPermission("property:room:export")
    @Log(title = "房屋", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(RoomBo bo, HttpServletResponse response) {
        ExcelBuilder.of(BeanUtil.copyToList(service.queryExport(bo), RoomExportVo.class), RoomExportVo.class)
            .sheetName("房屋").toResponse(response);
    }

    @SaCheckPermission("property:room:query")
    @GetMapping("/{id}")
    public R<RoomVo> get(@PathVariable @NotNull Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("property:room:add")
    @Log(title = "房屋", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody RoomBo bo) {
        return toAjax(service.insert(bo));
    }

    @SaCheckPermission("property:room:edit")
    @Log(title = "房屋", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody RoomBo bo) {
        return toAjax(service.update(bo));
    }

    @SaCheckPermission("property:room:edit")
    @Log(title = "房屋状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> status(@RequestBody RoomBo bo) {
        return toAjax(service.changeStatus(bo.getId(), bo.getStatus()));
    }

    @SaCheckPermission("property:room:remove")
    @Log(title = "房屋", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable @NotEmpty Long[] ids) {
        return toAjax(service.delete(List.of(ids)));
    }

    @SaCheckPermission("property:room:list")
    @GetMapping("/options")
    public R<List<RoomVo>> options(RoomBo bo) {
        return R.ok(service.options(bo));
    }
}
