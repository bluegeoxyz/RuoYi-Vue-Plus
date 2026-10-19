package org.dromara.property.resource.room.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.*;
import org.dromara.property.resource.room.domain.bo.RoomBo;
import org.dromara.property.resource.room.domain.vo.RoomVo;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.room.service.IRoomService;
import org.dromara.property.customer.relation.mapper.RoomCustomerRelationMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements IRoomService {
    private final RoomMapper roomMapper;
    private final ProjectMapper projectMapper;
    private final BuildingMapper buildingMapper;
    private final BuildingUnitMapper unitMapper;
    private final PropertyProjectScopeService scopeService;
    private final RoomCustomerRelationMapper relationMapper;

    public RoomVo queryById(Long id) {
        Room r = require(id);
        access(r.getProjectId(), ProjectBusinessAction.READ);
        return roomMapper.selectVoById(id);
    }

    public PageResult<RoomVo> queryPage(RoomBo bo, PageQuery pq) {
        var s = scope(ProjectBusinessAction.READ);
        if (s.type() == PropertyProjectScope.Type.NONE) return PageResult.build(List.of(), 0L);
        Page<RoomVo> p = roomMapper.selectVoPage(pq.build(), query(bo, s));
        return PageResult.build(p.getRecords(), p.getTotal());
    }

    public List<RoomVo> queryExport(RoomBo bo) {
        var s = scope(ProjectBusinessAction.EXPORT);
        return s.type() == PropertyProjectScope.Type.NONE ? List.of() : roomMapper.selectVoList(query(bo, s));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean insert(RoomBo bo) {
        access(bo.getProjectId(), ProjectBusinessAction.WRITE);
        Project p = projectMapper.selectById(bo.getProjectId());
        if (p == null || !"0".equals(p.getStatus())) throw new ServiceException("项目不存在或已停用");
        Building b = buildingMapper.selectActiveByIdForUpdate(bo.getBuildingId());
        if (b == null || !"0".equals(b.getStatus()) || !bo.getProjectId().equals(b.getProjectId()))
            throw new ServiceException("楼栋不存在、已停用或不属于目标项目");
        BuildingUnit u = unitMapper.selectActiveByIdForUpdate(bo.getUnitId());
        if (u == null || !"0".equals(u.getStatus()) || !bo.getProjectId().equals(u.getProjectId()) || !bo.getBuildingId().equals(u.getBuildingId()))
            throw new ServiceException("单元不存在、已停用或归属不一致");
        code(bo.getProjectId(), bo.getRoomCode(), null);
        try {
            Room room = BeanUtil.copyProperties(bo, Room.class);
            room.setOccupancyStatus("VACANT");
            return roomMapper.insert(room) > 0;
        } catch (DuplicateKeyException e) {
            throw new ServiceException("同一项目下房屋编码已存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean update(RoomBo bo) {
        Room c = roomMapper.selectActiveByIdForUpdate(bo.getId());
        if (c == null) throw new ServiceException("房屋不存在");
        access(c.getProjectId(), ProjectBusinessAction.WRITE);
        if (bo.getProjectId() != null && !bo.getProjectId().equals(c.getProjectId()) || bo.getBuildingId() != null && !bo.getBuildingId().equals(c.getBuildingId()) || bo.getUnitId() != null && !bo.getUnitId().equals(c.getUnitId()))
            throw new ServiceException("房屋不允许通过普通编辑变更项目、楼栋或单元");
        code(c.getProjectId(), bo.getRoomCode(), c.getId());
        Room r = BeanUtil.copyProperties(bo, Room.class);
        r.setProjectId(c.getProjectId());
        r.setBuildingId(c.getBuildingId());
        r.setUnitId(c.getUnitId());
        r.setOccupancyStatus(relationMapper.selectActiveOccupancyRelationIdsForUpdate(c.getId()).isEmpty() ? "VACANT" : "OCCUPIED");
        try {
            return roomMapper.updateById(r) > 0;
        } catch (DuplicateKeyException e) {
            throw new ServiceException("同一项目下房屋编码已存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, String status) {
        Room c = require(id);
        access(c.getProjectId(), ProjectBusinessAction.WRITE);
        Room r = new Room();
        r.setId(id);
        r.setStatus(status);
        return roomMapper.updateById(r) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Collection<Long> ids) {
        for (Long id : ids) {
            Room r = roomMapper.selectActiveByIdForUpdate(id);
            if (r == null) throw new ServiceException("房屋不存在");
            access(r.getProjectId(), ProjectBusinessAction.WRITE);
            if (!roomMapper.selectEffectiveCustomerRelationIdsForUpdate(id).isEmpty())
                throw new ServiceException("房屋存在有效客户关系，不能删除");
        }
        return roomMapper.deleteByIds(ids) > 0;
    }

    public List<RoomVo> options(RoomBo bo) {
        var s = scope(ProjectBusinessAction.OPTION);
        if (s.type() == PropertyProjectScope.Type.NONE) return List.of();
        return roomMapper.selectVoList(QueryBuilder.lambda(Room.class).select(Room::getId, Room::getProjectId, Room::getBuildingId, Room::getUnitId, Room::getRoomCode, Room::getRoomNo).eqIfPresent(Room::getProjectId, bo.getProjectId()).eqIfPresent(Room::getBuildingId, bo.getBuildingId()).eqIfPresent(Room::getUnitId, bo.getUnitId()).eq(Room::getStatus, "0").in(s.type() == PropertyProjectScope.Type.EXPLICIT_IDS, Room::getProjectId, s.projectIds()).orderByAsc(Room::getSort).build());
    }

    private LambdaQueryWrapper<Room> query(RoomBo b, PropertyProjectScope s) {
        return QueryBuilder.lambda(Room.class).eqIfPresent(Room::getProjectId, b.getProjectId()).eqIfPresent(Room::getBuildingId, b.getBuildingId()).eqIfPresent(Room::getUnitId, b.getUnitId()).likeIfText(Room::getRoomCode, b.getRoomCode()).likeIfText(Room::getRoomNo, b.getRoomNo()).eqIfText(Room::getStatus, b.getStatus()).in(s.type() == PropertyProjectScope.Type.EXPLICIT_IDS, Room::getProjectId, s.projectIds()).orderByAsc(Room::getSort).build();
    }

    private Room require(Long id) {
        Room r = roomMapper.selectById(id);
        if (r == null) throw new ServiceException("房屋不存在");
        return r;
    }

    private void code(Long p, String c, Long x) {
        if (roomMapper.countActiveCode(p, c, x) > 0) throw new ServiceException("同一项目下房屋编码已存在");
    }

    private PropertyProjectScope scope(ProjectBusinessAction a) {
        return scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, a);
    }

    private void access(Long p, ProjectBusinessAction a) {
        scopeService.assertBusinessAccess(p, PropertyBusinessDomain.RESOURCE, a);
    }
}
