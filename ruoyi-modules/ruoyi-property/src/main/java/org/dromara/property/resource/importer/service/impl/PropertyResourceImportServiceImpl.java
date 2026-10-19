package org.dromara.property.resource.importer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.excel.importer.ImportError;
import org.dromara.common.excel.importer.ImportResult;
import org.dromara.property.dict.PropertyDictTypes;
import org.dromara.property.dict.PropertyDictionaryService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.importer.domain.PropertyResourceImportRow;
import org.dromara.property.resource.importer.service.IPropertyResourceImportService;
import org.dromara.property.resource.importer.service.PropertyResourceImportCommitHook;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.ProjectBusinessAction;
import org.dromara.property.scope.PropertyBusinessDomain;
import org.dromara.property.scope.PropertyFunctionalPermissionService;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 房屋资源树形导入。校验阶段只读，提交阶段以一个事务写入整份文件。 */
@Service
@RequiredArgsConstructor
public class PropertyResourceImportServiceImpl implements IPropertyResourceImportService {
    private static final int MAX_ROWS = 2000;
    private final ProjectMapper projectMapper;
    private final BuildingMapper buildingMapper;
    private final BuildingUnitMapper unitMapper;
    private final RoomMapper roomMapper;
    private final PropertyProjectScopeService scopeService;
    private final PropertyFunctionalPermissionService functionalPermissionService;
    private final PropertyDictionaryService dictionaryService;
    private final TransactionTemplate transactionTemplate;
    private final List<PropertyResourceImportCommitHook> commitHooks;

    @Override
    public ImportResult importRows(Long projectId, List<PropertyResourceImportRow> sourceRows) {
        List<PropertyResourceImportRow> rows = sourceRows == null ? List.of() : sourceRows;
        for (int i = 0; i < rows.size(); i++) rows.get(i).setRowNumber(i + 2);
        List<ImportError> errors = validate(projectId, rows);
        if (!errors.isEmpty()) return ImportResult.failure(rows.size(), errors);
        try {
            commitHooks.forEach(hook -> hook.beforeCommit(projectId));
            transactionTemplate.executeWithoutResult(status -> commit(projectId, rows));
            return ImportResult.success(rows.size());
        } catch (RuntimeException e) {
            return ImportResult.failure(rows.size(), List.of(error(null, "transaction",
                "提交阶段发生并发或业务冲突：" + rootMessage(e) + "，本次写入 0 条。")));
        }
    }

    private List<ImportError> validate(Long projectId, List<PropertyResourceImportRow> rows) {
        List<ImportError> errors = new ArrayList<>();
        functionalPermissionService.assertCurrent("property:resource:treeImport");
        if (projectId == null) errors.add(error(null, "projectId", "项目不能为空"));
        if (rows.isEmpty()) errors.add(error(null, "file", "未读取到房屋数据"));
        if (rows.size() > MAX_ROWS) errors.add(error(null, "file", "单次最多导入 2000 条房屋数据，请拆分后重新导入。"));
        if (!errors.isEmpty()) return errors;
        scopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
        Project project = projectMapper.selectById(projectId);
        if (project == null || !"0".equals(project.getStatus())) {
            errors.add(error(null, "projectId", "项目不存在或已停用"));
            return errors;
        }

        Map<String, PropertyResourceImportRow> buildings = new LinkedHashMap<>();
        Map<String, PropertyResourceImportRow> units = new LinkedHashMap<>();
        Map<String, PropertyResourceImportRow> rooms = new LinkedHashMap<>();
        for (PropertyResourceImportRow row : rows) {
            normalizeDictionary(row, errors);
            merge(buildings, row.getBuildingCode(), row, this::sameBuilding, "楼栋编码", errors);
            merge(units, row.getBuildingCode() + '\u0000' + row.getUnitCode(), row, this::sameUnit, "单元编码", errors);
            PropertyResourceImportRow previous = rooms.putIfAbsent(row.getRoomCode(), row);
            if (previous != null) errors.add(error(row.getRowNumber(), "roomCode",
                "房屋编码 " + row.getRoomCode() + " 与第 " + previous.getRowNumber() + " 行重复"));
        }

        Map<String, Building> dbBuildings = buildingMapper.selectList(Wrappers.<Building>lambdaQuery()
            .eq(Building::getProjectId, projectId)).stream().collect(Collectors.toMap(Building::getBuildingCode, Function.identity()));
        Map<Long, List<BuildingUnit>> dbUnitGroups = unitMapper.selectList(Wrappers.<BuildingUnit>lambdaQuery()
            .eq(BuildingUnit::getProjectId, projectId)).stream().collect(Collectors.groupingBy(BuildingUnit::getBuildingId));
        Map<String, Room> dbRooms = roomMapper.selectList(Wrappers.<Room>lambdaQuery().eq(Room::getProjectId, projectId))
            .stream().collect(Collectors.toMap(Room::getRoomCode, Function.identity()));
        for (PropertyResourceImportRow row : buildings.values()) {
            Building existing = dbBuildings.get(row.getBuildingCode());
            if (existing != null && !sameBuilding(existing, row)) errors.add(error(row.getRowNumber(), "buildingCode",
                "数据库已有楼栋 " + row.getBuildingCode() + " 的属性与导入文件不一致"));
        }
        for (PropertyResourceImportRow row : units.values()) {
            Building building = dbBuildings.get(row.getBuildingCode());
            if (building == null) continue;
            BuildingUnit existing = dbUnitGroups.getOrDefault(building.getId(), List.of()).stream()
                .filter(unit -> Objects.equals(unit.getUnitCode(), row.getUnitCode())).findFirst().orElse(null);
            if (existing != null && !sameUnit(existing, row)) errors.add(error(row.getRowNumber(), "unitCode",
                "数据库已有单元 " + row.getBuildingCode() + "/" + row.getUnitCode() + " 的属性与导入文件不一致"));
        }
        for (PropertyResourceImportRow row : rows) {
            Room existing = dbRooms.get(row.getRoomCode());
            if (existing != null && !sameRoom(existing, row, dbBuildings, dbUnitGroups)) errors.add(error(row.getRowNumber(), "roomCode",
                "数据库已有房屋 " + row.getRoomCode() + " 的属性或父级归属与导入文件不一致"));
        }
        return errors;
    }

    private void commit(Long projectId, List<PropertyResourceImportRow> rows) {
        functionalPermissionService.assertCurrent("property:resource:treeImport");
        scopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
        Project project = projectMapper.selectActiveByIdForUpdate(projectId);
        if (project == null || !"0".equals(project.getStatus())) throw new IllegalStateException("项目不存在或已停用");
        Map<String, Building> buildings = new HashMap<>();
        Map<String, BuildingUnit> units = new HashMap<>();
        for (PropertyResourceImportRow row : rows) {
            Building building = buildings.get(row.getBuildingCode());
            if (building == null) {
                building = buildingMapper.selectActiveByProjectAndCodeForUpdate(projectId, row.getBuildingCode());
                if (building == null) {
                    building = toBuilding(projectId, row);
                    buildingMapper.insert(building);
                } else if (!sameBuilding(building, row)) throw new IllegalStateException("楼栋属性已发生变化");
                buildings.put(row.getBuildingCode(), building);
            }
            String unitKey = row.getBuildingCode() + '\u0000' + row.getUnitCode();
            BuildingUnit unit = units.get(unitKey);
            if (unit == null) {
                unit = unitMapper.selectActiveByBuildingAndCodeForUpdate(building.getId(), row.getUnitCode());
                if (unit == null) {
                    unit = toUnit(projectId, building.getId(), row);
                    unitMapper.insert(unit);
                } else if (!projectId.equals(unit.getProjectId()) || !sameUnit(unit, row)) throw new IllegalStateException("单元属性已发生变化");
                units.put(unitKey, unit);
            }
            Room room = roomMapper.selectActiveByProjectAndCodeForUpdate(projectId, row.getRoomCode());
            if (room == null) {
                commitHooks.forEach(hook -> hook.beforeRoomInsert(projectId, row));
                roomMapper.insert(toRoom(projectId, building.getId(), unit.getId(), row));
            }
            else if (!sameRoom(room, row, building.getId(), unit.getId())) throw new IllegalStateException("房屋属性已发生变化");
        }
    }

    private void normalizeDictionary(PropertyResourceImportRow row, List<ImportError> errors) {
        row.setBuildingType(resolve(row, "buildingType", "楼栋类型", PropertyDictTypes.BUILDING_TYPE, row.getBuildingType(), true, errors));
        row.setRoomType(resolve(row, "roomType", "房屋类型", PropertyDictTypes.ROOM_TYPE, row.getRoomType(), false, errors));
        row.setPropertyType(resolve(row, "propertyType", "产权属性", PropertyDictTypes.PROPERTY_TYPE, row.getPropertyType(), false, errors));
        row.setDeliveryStatus(resolve(row, "deliveryStatus", "交付状态", PropertyDictTypes.DELIVERY_STATUS, row.getDeliveryStatus(), false, errors));
        row.setOccupancyStatus(resolve(row, "occupancyStatus", "入住状态", PropertyDictTypes.OCCUPANCY_STATUS, row.getOccupancyStatus(), false, errors));
        if (row.getOccupancyStatus() != null && !"VACANT".equals(row.getOccupancyStatus())) {
            errors.add(error(row.getRowNumber(), "occupancyStatus", "新建房屋入住状态只能为空置，请通过房屋客户关系办理入住"));
        }
        row.setStatus(resolve(row, "status", "状态", PropertyDictTypes.NORMAL_DISABLE, row.getStatus(), true, errors));
    }

    private String resolve(PropertyResourceImportRow row, String field, String label, String dictType, String input,
                           boolean required, List<ImportError> errors) {
        if (input == null || input.isBlank()) {
            if (required) errors.add(error(row.getRowNumber(), field, label + "不能为空"));
            return null;
        }
        String code = dictionaryService.resolveInput(dictType, input);
        if (code == null) errors.add(error(row.getRowNumber(), field, label + "“" + input.trim() + "”不是有效值"));
        return code;
    }

    private void merge(Map<String, PropertyResourceImportRow> map, String key, PropertyResourceImportRow row,
                       java.util.function.BiPredicate<PropertyResourceImportRow, PropertyResourceImportRow> same,
                       String label, List<ImportError> errors) {
        PropertyResourceImportRow first = map.putIfAbsent(key, row);
        if (first != null && !same.test(first, row)) errors.add(error(row.getRowNumber(), label,
            label + " " + key.replace('\u0000', '/') + " 在第 " + first.getRowNumber() + " 行和第 " + row.getRowNumber() + " 行属性不一致"));
    }

    private boolean sameBuilding(PropertyResourceImportRow a, PropertyResourceImportRow b) {
        return Objects.equals(a.getBuildingName(), b.getBuildingName()) && Objects.equals(a.getBuildingType(), b.getBuildingType())
            && Objects.equals(a.getAboveGroundFloors(), b.getAboveGroundFloors()) && Objects.equals(a.getUndergroundFloors(), b.getUndergroundFloors())
            && Objects.equals(a.getBuildingSort(), b.getBuildingSort()) && Objects.equals(a.getStatus(), b.getStatus());
    }
    private boolean sameBuilding(Building a, PropertyResourceImportRow b) {
        return Objects.equals(a.getBuildingName(), b.getBuildingName()) && Objects.equals(a.getBuildingType(), b.getBuildingType())
            && Objects.equals(a.getAboveGroundFloors(), b.getAboveGroundFloors()) && Objects.equals(a.getUndergroundFloors(), b.getUndergroundFloors())
            && Objects.equals(a.getSort(), b.getBuildingSort()) && Objects.equals(a.getStatus(), b.getStatus());
    }
    private boolean sameUnit(PropertyResourceImportRow a, PropertyResourceImportRow b) {
        return Objects.equals(a.getUnitName(), b.getUnitName()) && Objects.equals(a.getFloorCount(), b.getFloorCount())
            && Objects.equals(a.getUnitSort(), b.getUnitSort()) && Objects.equals(a.getStatus(), b.getStatus());
    }
    private boolean sameUnit(BuildingUnit a, PropertyResourceImportRow b) {
        return Objects.equals(a.getUnitName(), b.getUnitName()) && Objects.equals(a.getFloorCount(), b.getFloorCount())
            && Objects.equals(a.getSort(), b.getUnitSort()) && Objects.equals(a.getStatus(), b.getStatus());
    }
    private boolean sameRoom(Room room, PropertyResourceImportRow row, Map<String, Building> buildings, Map<Long, List<BuildingUnit>> units) {
        Building b = buildings.get(row.getBuildingCode());
        if (b == null) return false;
        BuildingUnit u = units.getOrDefault(b.getId(), List.of()).stream().filter(x -> Objects.equals(x.getUnitCode(), row.getUnitCode())).findFirst().orElse(null);
        return u != null && sameRoom(room, row, b.getId(), u.getId());
    }
    private boolean sameRoom(Room a, PropertyResourceImportRow b, Long buildingId, Long unitId) {
        return Objects.equals(a.getBuildingId(), buildingId) && Objects.equals(a.getUnitId(), unitId) && Objects.equals(a.getRoomNo(), b.getRoomNo())
            && Objects.equals(a.getFloorNo(), b.getFloorNo()) && Objects.equals(a.getRoomType(), b.getRoomType())
            && Objects.equals(a.getPropertyType(), b.getPropertyType()) && Objects.equals(a.getBuildingArea(), b.getBuildingArea())
            && Objects.equals(a.getUsableArea(), b.getUsableArea()) && Objects.equals(a.getDeliveryStatus(), b.getDeliveryStatus())
            && Objects.equals(a.getSort(), b.getRoomSort())
            && Objects.equals(a.getStatus(), b.getStatus());
    }

    private Building toBuilding(Long projectId, PropertyResourceImportRow r) { Building x = new Building(); x.setProjectId(projectId); x.setBuildingCode(r.getBuildingCode()); x.setBuildingName(r.getBuildingName()); x.setBuildingType(r.getBuildingType()); x.setAboveGroundFloors(r.getAboveGroundFloors()); x.setUndergroundFloors(r.getUndergroundFloors()); x.setSort(r.getBuildingSort()); x.setStatus(r.getStatus()); return x; }
    private BuildingUnit toUnit(Long projectId, Long buildingId, PropertyResourceImportRow r) { BuildingUnit x = new BuildingUnit(); x.setProjectId(projectId); x.setBuildingId(buildingId); x.setUnitCode(r.getUnitCode()); x.setUnitName(r.getUnitName()); x.setFloorCount(r.getFloorCount()); x.setSort(r.getUnitSort()); x.setStatus(r.getStatus()); return x; }
    private Room toRoom(Long projectId, Long buildingId, Long unitId, PropertyResourceImportRow r) { Room x = new Room(); x.setProjectId(projectId); x.setBuildingId(buildingId); x.setUnitId(unitId); x.setRoomCode(r.getRoomCode()); x.setRoomNo(r.getRoomNo()); x.setFloorNo(r.getFloorNo()); x.setRoomType(r.getRoomType()); x.setPropertyType(r.getPropertyType()); x.setBuildingArea(r.getBuildingArea()); x.setUsableArea(r.getUsableArea()); x.setDeliveryStatus(r.getDeliveryStatus()); x.setOccupancyStatus("VACANT"); x.setSort(r.getRoomSort()); x.setStatus(r.getStatus()); return x; }
    private ImportError error(Integer row, String field, String message) { return new ImportError(row, field, message); }
    private String rootMessage(Throwable e) { Throwable root = e; while (root.getCause() != null) root = root.getCause(); return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage(); }
}
