package org.dromara.property.customer.relation.importer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.importer.ImportError;
import org.dromara.common.excel.importer.ImportResult;
import org.dromara.property.customer.domain.Customer;
import org.dromara.property.customer.mapper.CustomerMapper;
import org.dromara.property.customer.relation.domain.RoomCustomerRelation;
import org.dromara.property.customer.relation.domain.bo.RoomCustomerRelationBo;
import org.dromara.property.customer.relation.importer.domain.RoomCustomerRelationImportRow;
import org.dromara.property.customer.relation.importer.service.IRoomCustomerRelationImportService;
import org.dromara.property.customer.relation.importer.service.RoomCustomerRelationImportCommitHook;
import org.dromara.property.customer.relation.mapper.RoomCustomerRelationMapper;
import org.dromara.property.customer.relation.service.IRoomCustomerRelationService;
import org.dromara.property.dict.PropertyDictTypes;
import org.dromara.property.dict.PropertyDictionaryService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
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

/** 房屋客户关系 Excel 两阶段原子导入。 */
@Service
@RequiredArgsConstructor
public class RoomCustomerRelationImportServiceImpl implements IRoomCustomerRelationImportService {
    private static final int MAX_ROWS = 2000;
    private static final String IMPORT_PERMISSION = "property:customer-relation:import";
    private final ProjectMapper projectMapper;
    private final BuildingMapper buildingMapper;
    private final BuildingUnitMapper unitMapper;
    private final RoomMapper roomMapper;
    private final CustomerMapper customerMapper;
    private final RoomCustomerRelationMapper relationMapper;
    private final IRoomCustomerRelationService relationService;
    private final PropertyFunctionalPermissionService functionalPermissionService;
    private final PropertyProjectScopeService scopeService;
    private final PropertyDictionaryService dictionaryService;
    private final Validator validator;
    private final TransactionTemplate transactionTemplate;
    private final List<RoomCustomerRelationImportCommitHook> commitHooks;

    @Override
    public ImportResult importRows(Long projectId, List<RoomCustomerRelationImportRow> sourceRows) {
        List<RoomCustomerRelationImportRow> rows = sourceRows == null ? List.of() : sourceRows;
        for (int i = 0; i < rows.size(); i++) rows.get(i).setRowNumber(i + 2);
        List<ImportError> errors = validate(projectId, rows);
        if (!errors.isEmpty()) return ImportResult.failure(rows.size(), errors);
        try {
            commitHooks.forEach(hook -> hook.beforeCommit(projectId));
            transactionTemplate.executeWithoutResult(status -> commit(projectId, rows));
            return ImportResult.success(rows.size());
        } catch (RuntimeException e) {
            return ImportResult.failure(rows.size(), List.of(error(null, "transaction", safeCommitMessage(e))));
        }
    }

    private List<ImportError> validate(Long projectId, List<RoomCustomerRelationImportRow> rows) {
        functionalPermissionService.assertCurrent(IMPORT_PERMISSION);
        List<ImportError> errors = new ArrayList<>();
        if (projectId == null) errors.add(error(null, "projectId", "项目不能为空"));
        if (rows.isEmpty()) errors.add(error(null, "file", "未读取到房屋客户关系数据"));
        if (rows.size() > MAX_ROWS) errors.add(error(null, "file", "单次最多导入 2000 条房屋客户关系，请拆分文件后重新导入。"));
        if (!errors.isEmpty()) return errors;
        scopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.WRITE);
        Project project = projectMapper.selectById(projectId);
        if (project == null || !"0".equals(project.getStatus())) return List.of(error(null, "projectId", "项目不存在或已停用"));

        Map<String, Building> buildings = buildingMapper.selectList(Wrappers.<Building>lambdaQuery()
            .eq(Building::getProjectId, projectId).eq(Building::getStatus, "0"))
            .stream().collect(Collectors.toMap(Building::getBuildingCode, Function.identity()));
        Map<Long, List<BuildingUnit>> units = unitMapper.selectList(Wrappers.<BuildingUnit>lambdaQuery()
            .eq(BuildingUnit::getProjectId, projectId).eq(BuildingUnit::getStatus, "0"))
            .stream().collect(Collectors.groupingBy(BuildingUnit::getBuildingId));
        Map<Long, List<Room>> rooms = roomMapper.selectList(Wrappers.<Room>lambdaQuery()
            .eq(Room::getProjectId, projectId).eq(Room::getStatus, "0"))
            .stream().collect(Collectors.groupingBy(Room::getUnitId));
        Map<String, Customer> customers = customerMapper.selectList(Wrappers.<Customer>lambdaQuery()
                .select(Customer::getId, Customer::getCustomerNo,
                    Customer::getLifecycleStatus, Customer::getStatus))
            .stream()
            .collect(Collectors.toMap(Customer::getCustomerNo, Function.identity(), (first, ignored) -> first));

        Map<String, Integer> fileKeys = new HashMap<>();
        for (RoomCustomerRelationImportRow row : rows) {
            normalize(row, errors);
            Set<ConstraintViolation<RoomCustomerRelationImportRow>> violations = validator.validate(row);
            for (ConstraintViolation<RoomCustomerRelationImportRow> violation : violations) {
                errors.add(error(row.getRowNumber(), violation.getPropertyPath().toString(), violation.getMessage()));
            }
            Building building = buildings.get(trim(row.getBuildingCode()));
            if (building == null) {
                errors.add(error(row.getRowNumber(), "buildingCode", "楼栋编码“" + safe(row.getBuildingCode()) + "”不存在或不可用"));
                continue;
            }
            BuildingUnit unit = units.getOrDefault(building.getId(), List.of()).stream()
                .filter(value -> Objects.equals(value.getUnitCode(), trim(row.getUnitCode()))).findFirst().orElse(null);
            if (unit == null) {
                errors.add(error(row.getRowNumber(), "unitCode", "单元编码“" + safe(row.getUnitCode()) + "”不属于目标楼栋或不可用"));
                continue;
            }
            Room room = rooms.getOrDefault(unit.getId(), List.of()).stream()
                .filter(value -> Objects.equals(value.getRoomCode(), trim(row.getRoomCode()))).findFirst().orElse(null);
            if (room == null || !Objects.equals(room.getBuildingId(), building.getId())) {
                errors.add(error(row.getRowNumber(), "roomCode", "房屋编码“" + safe(row.getRoomCode()) + "”不存在、不可用或层级归属不正确"));
                continue;
            }
            row.setRoomId(room.getId());
            Customer customer = customers.get(trim(row.getCustomerNo()));
            if (customer == null) {
                errors.add(error(row.getRowNumber(), "customerNo", "客户编号“" + safe(row.getCustomerNo()) + "”不存在"));
                continue;
            }
            if (!"ACTIVE".equals(customer.getLifecycleStatus()) || !"0".equals(customer.getStatus())) {
                errors.add(error(row.getRowNumber(), "customerNo", "客户编号“" + safe(row.getCustomerNo()) + "”当前状态不允许建立关系"));
                continue;
            }
            row.setCustomerId(customer.getId());
            if (StringUtils.isBlank(row.getRelationType())) continue;
            String key = room.getId() + "\u0000" + customer.getId() + "\u0000" + row.getRelationType();
            Integer first = fileKeys.putIfAbsent(key, row.getRowNumber());
            if (first != null) errors.add(error(row.getRowNumber(), "relationType", "与第 " + first + " 行为重复房屋客户关系"));
        }
        if (!errors.isEmpty()) return errors;

        Set<Long> roomIds = rows.stream().map(RoomCustomerRelationImportRow::getRoomId).collect(Collectors.toSet());
        List<RoomCustomerRelation> existing = relationMapper.selectList(Wrappers.<RoomCustomerRelation>lambdaQuery()
            .in(RoomCustomerRelation::getRoomId, roomIds).in(RoomCustomerRelation::getRelationStatus, "PENDING", "ACTIVE")
            .eq(RoomCustomerRelation::getStatus, "0"));
        Set<String> existingKeys = existing.stream().map(value -> value.getRoomId() + "\u0000" + value.getCustomerId() + "\u0000" + value.getRelationType()).collect(Collectors.toSet());
        for (RoomCustomerRelationImportRow row : rows) {
            String key = row.getRoomId() + "\u0000" + row.getCustomerId() + "\u0000" + row.getRelationType();
            if (existingKeys.contains(key)) errors.add(error(row.getRowNumber(), "relationType", "该房屋与客户已存在有效关系"));
        }
        return errors;
    }

    private void normalize(RoomCustomerRelationImportRow row, List<ImportError> errors) {
        row.setBuildingCode(trim(row.getBuildingCode()));
        row.setUnitCode(trim(row.getUnitCode()));
        row.setRoomCode(trim(row.getRoomCode()));
        row.setCustomerNo(trim(row.getCustomerNo()));
        String input = trim(row.getRelationType());
        String code = dictionaryService.resolveInput(PropertyDictTypes.RELATION_TYPE, input);
        if (StringUtils.isBlank(input) || code == null || input.equals(code)) {
            errors.add(error(row.getRowNumber(), "relationType", "关系类型“" + safe(input) + "”不是有效值"));
            row.setRelationType(null);
        } else row.setRelationType(code);
        if ("是".equals(trim(row.getIsPrimary()))) row.setIsPrimary("1");
        else if ("否".equals(trim(row.getIsPrimary()))) row.setIsPrimary("0");
        else {
            errors.add(error(row.getRowNumber(), "isPrimary", "是否主要关系“" + safe(row.getIsPrimary()) + "”不是有效值"));
            row.setIsPrimary(null);
        }
    }

    private void commit(Long projectId, List<RoomCustomerRelationImportRow> rows) {
        functionalPermissionService.assertCurrent(IMPORT_PERMISSION);
        scopeService.assertBusinessAccess(projectId, PropertyBusinessDomain.CUSTOMER_RELATION, ProjectBusinessAction.WRITE);
        Project project = projectMapper.selectActiveByIdForUpdate(projectId);
        if (project == null || !"0".equals(project.getStatus())) throw new ServiceException("项目不存在或已停用");
        List<RoomCustomerRelationImportRow> ordered = rows.stream()
            .sorted(Comparator.comparing(RoomCustomerRelationImportRow::getRoomId)
                .thenComparing(RoomCustomerRelationImportRow::getCustomerId)).toList();
        for (int i = 0; i < ordered.size(); i++) {
            RoomCustomerRelationImportRow row = ordered.get(i);
            for (RoomCustomerRelationImportCommitHook hook : commitHooks) hook.beforeInsert(i, row);
            RoomCustomerRelationBo bo = new RoomCustomerRelationBo();
            bo.setProjectId(projectId);
            bo.setRoomId(row.getRoomId());
            bo.setCustomerId(row.getCustomerId());
            bo.setRelationType(row.getRelationType());
            bo.setIsPrimary(row.getIsPrimary());
            bo.setStartDate(row.getStartDate());
            bo.setRemark(row.getRemark());
            relationService.insertForImport(bo);
        }
    }

    private String safeCommitMessage(Throwable error) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (current instanceof ServiceException && current.getMessage() != null) return current.getMessage() + "，本次写入 0 条。";
        }
        return "提交阶段发生并发或数据库冲突，本次写入 0 条。";
    }

    private String trim(String value) { return value == null ? null : value.trim(); }
    private String safe(String value) { return StringUtils.isBlank(value) ? "" : value.trim(); }
    private ImportError error(Integer row, String field, String message) { return new ImportError(row, field, message); }
}
