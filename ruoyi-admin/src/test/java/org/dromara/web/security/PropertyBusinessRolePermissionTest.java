package org.dromara.web.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** AUTH-010A 物业标准角色最小权限矩阵静态校验。 */
class PropertyBusinessRolePermissionTest {

    private static final Path MIGRATION = repositoryRoot().resolve(
        "script/sql/zbm/archive/legacy-development-migrations/V2.0.34__converge_property_business_role_permissions.sql");
    private static final Pattern ROLE_PERMISSION = Pattern.compile(
        "\\('(?<role>property_[a-z_]+)',\\s*'(?<permission>(?:property|warehouse):[^']+)'\\)");
    private static final Set<String> ROLES = Set.of(
        "property_operation_manager", "property_customer_service", "property_cashier",
        "property_finance_manager", "property_warehouse_keeper",
        "property_warehouse_manager", "property_project_manager");

    @Test
    void matrixUsesOnlyRegisteredMenuPermissions() throws IOException {
        Map<String, Set<String>> matrix = matrix();
        assertEquals(ROLES, matrix.keySet());
        String menuBaseline = Files.readString(repositoryRoot().resolve("script/sql/zbm/baseline/03_menu_permission.sql"));
        Set<String> missing = new LinkedHashSet<>();
        matrix.values().stream().flatMap(Set::stream).distinct()
            .filter(permission -> !menuBaseline.contains("'" + permission + "'"))
            .forEach(missing::add);
        assertTrue(missing.isEmpty(), () -> "角色矩阵包含未注册菜单权限: " + missing);
    }

    @Test
    void customerServiceNeedsNoMasterDataManagementPermissions() throws IOException {
        assertEquals(Set.of(
            "property:customerRelation:list", "property:customerRelation:query",
            "property:customerRelation:add", "property:customerRelation:edit",
            "property:customerRelation:moveIn", "property:customerRelation:moveOut"),
            matrix().get("property_customer_service"));
    }

    @Test
    void cashierCannotConfigureCancelOrReverse() throws IOException {
        Set<String> permissions = matrix().get("property_cashier");
        assertTrue(permissions.containsAll(Set.of("property:receivable:list", "property:receivable:query",
            "property:payment:list", "property:payment:query", "property:payment:add", "property:payment:allocate")));
        assertTrue(permissions.stream().noneMatch(p -> p.startsWith("property:chargeItem:")
            || p.startsWith("property:chargeRule:") || p.equals("property:receivable:cancel")
            || p.equals("property:payment:reverse")));
    }

    @Test
    void warehouseKeeperHasNoMasterDataOrApprovalPermissions() throws IOException {
        Set<String> permissions = matrix().get("property_warehouse_keeper");
        assertTrue(permissions.containsAll(Set.of("warehouse:inbound:add", "warehouse:inbound:edit",
            "warehouse:outbound:add", "warehouse:outbound:edit", "warehouse:inventory:list")));
        assertTrue(permissions.stream().noneMatch(p -> p.startsWith("warehouse:warehouse:")
            || p.startsWith("warehouse:zone:") || p.startsWith("warehouse:location:")
            || p.startsWith("warehouse:material:") || p.startsWith("warehouse:materialCategory:")
            || p.startsWith("warehouse:unit:") || p.endsWith(":confirm") || p.endsWith(":complete")));
    }

    @Test
    void sensitiveCapabilitiesRemainSeparated() throws IOException {
        Map<String, Set<String>> matrix = matrix();
        assertTrue(matrix.get("property_finance_manager").contains("property:payment:reverse"));
        assertFalse(matrix.get("property_cashier").contains("property:payment:reverse"));
        assertTrue(matrix.get("property_warehouse_manager").containsAll(Set.of(
            "warehouse:inbound:confirm", "warehouse:inbound:complete",
            "warehouse:outbound:confirm", "warehouse:outbound:complete")));
        assertTrue(matrix.get("property_project_manager").stream().noneMatch(p ->
            p.startsWith("property:projectAdminScope:") || p.equals("property:project:add")
                || p.equals("property:project:edit") || p.equals("property:project:remove")));
        assertTrue(matrix.values().stream().flatMap(Set::stream)
            .noneMatch(p -> p.startsWith("system:") || p.startsWith("monitor:")));
    }

    @Test
    void migrationConvergesOnlyTargetRoleMenuRows() throws IOException {
        String sql = Files.readString(MIGRATION);
        assertTrue(sql.contains("DELETE role_menu"));
        assertTrue(sql.contains("JOIN `tmp_auth010_role` target ON target.`role_key` = role.`role_key`"));
        assertFalse(sql.contains("DELETE FROM `sys_role`"));
        assertFalse(sql.contains("UPDATE `sys_role`"));
        assertFalse(sql.contains("UPDATE `sys_menu`"));
    }

    private static Map<String, Set<String>> matrix() throws IOException {
        String sql = Files.readString(MIGRATION);
        String matrixSql = sql.substring(0, sql.indexOf("tmp_auth010_removed_permission"));
        Matcher matcher = ROLE_PERMISSION.matcher(matrixSql);
        Map<String, Set<String>> result = new LinkedHashMap<>();
        while (matcher.find()) {
            result.computeIfAbsent(matcher.group("role"), ignored -> new LinkedHashSet<>())
                .add(matcher.group("permission"));
        }
        return result;
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        if (Files.exists(current.resolve("script/sql/zbm"))) return current;
        Path parent = current.getParent();
        if (parent != null && Files.exists(parent.resolve("script/sql/zbm"))) return parent;
        throw new IllegalStateException("无法定位仓库根目录: " + current);
    }
}
