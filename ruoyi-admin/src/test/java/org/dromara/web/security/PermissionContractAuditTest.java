package org.dromara.web.security;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 物业、仓库与 HR 权限契约审计。
 *
 * <p>该测试只读取 Controller、Service 源码和菜单基线，不启动 Spring、不连接数据库，
 * 也不改变任何角色授权。已知耦合保留为显式基线，后续整改必须同步更新本测试和审计报告。</p>
 */
class PermissionContractAuditTest {

    private static final List<String> CONTROLLER_PACKAGES = List.of(
        "org.dromara.property",
        "org.dromara.warehouse",
        "org.dromara.hr"
    );

    private static final Pattern MENU_ROW = menuRowPattern();

    private static final List<KnownCoupling> KNOWN_OPTION_COUPLINGS = List.of(
        new KnownCoupling("GET", "/property/project/options", "property:project:list", "项目管理列表权限"),
        new KnownCoupling("GET", "/property/project/business-options", "property:project:list", "项目管理列表权限"),
        new KnownCoupling("GET", "/property/project/member/options", "property:projectMember:list", "项目成员管理列表权限"),
        new KnownCoupling("GET", "/hr/employee/options", "hr:employee:list", "员工管理列表权限"),
        new KnownCoupling("GET", "/property/resource/building/options", "property:building:list", "楼栋管理列表权限"),
        new KnownCoupling("GET", "/property/resource/building/project-options", "property:building:list", "楼栋管理列表权限"),
        new KnownCoupling("GET", "/property/resource/unit/options", "property:unit:list", "单元管理列表权限"),
        new KnownCoupling("GET", "/property/resource/room/options", "property:room:list", "房屋管理列表权限"),
        new KnownCoupling("GET", "/property/customer-relation/options", "property:customerRelation:list", "入住关系列表权限"),
        new KnownCoupling("GET", "/property/customer-relation/project-options", "property:customerRelation:list", "入住关系列表权限"),
        new KnownCoupling("GET", "/property/charge/item/options", "property:chargeItem:list", "收费项目管理列表权限"),
        new KnownCoupling("GET", "/property/charge/item/project-options", "property:chargeItem:list", "收费项目管理列表权限"),
        new KnownCoupling("GET", "/property/charge/rule/options", "property:chargeRule:list", "计费规则管理列表权限"),
        new KnownCoupling("GET", "/warehouse/warehouse/options", "warehouse:warehouse:list", "仓库管理列表权限"),
        new KnownCoupling("GET", "/warehouse/warehouse/company-options", "warehouse:warehouse:list", "仓库管理列表权限"),
        new KnownCoupling("GET", "/warehouse/warehouse/project-options", "warehouse:warehouse:list", "仓库管理列表权限"),
        new KnownCoupling("GET", "/warehouse/zone/options", "warehouse:zone:list", "库区管理列表权限"),
        new KnownCoupling("GET", "/warehouse/location/options", "warehouse:location:list", "库位管理列表权限"),
        new KnownCoupling("GET", "/warehouse/materialCategory/options", "warehouse:materialCategory:list", "物料分类管理列表权限"),
        new KnownCoupling("GET", "/warehouse/unit/options", "warehouse:unit:list", "计量单位管理列表权限"),
        new KnownCoupling("GET", "/warehouse/material/options", "warehouse:material:list", "物料管理列表权限"),
        new KnownCoupling("GET", "/warehouse/material/company-options", "warehouse:material:list", "物料管理列表权限")
    );

    @Test
    void allBusinessEndpointsDeclareFunctionalPermission() {
        List<EndpointContract> unsecured = scanEndpoints().stream()
            .filter(endpoint -> endpoint.permissions().isEmpty())
            .toList();
        assertTrue(unsecured.isEmpty(), () -> "发现仅登录即可调用的业务接口: " + unsecured);
    }

    @Test
    void controllerPermissionsAreRegisteredExactlyOnceInCurrentMenuBaseline() throws IOException {
        MenuContract menu = readMenuContract();
        Set<String> controllerPermissions = scanEndpoints().stream()
            .flatMap(endpoint -> endpoint.permissions().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> missing = controllerPermissions.stream()
            .filter(permission -> !menu.permissionToMenuIds().containsKey(permission))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Set<Long>> duplicateMenuPermissions = menu.permissionToMenuIds().entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertTrue(missing.isEmpty(), () -> "Controller 权限未注册到 sys_menu 基线: " + missing);
        assertTrue(duplicateMenuPermissions.isEmpty(),
            () -> "同一权限标识绑定了多个菜单 ID: " + duplicateMenuPermissions);
    }

    @Test
    void businessContextPoliciesOnlyUseExistingControllerPermissions() {
        Set<String> controllerPermissions = scanEndpoints().stream()
            .flatMap(endpoint -> endpoint.permissions().stream())
            .collect(Collectors.toSet());
        Set<String> policyPermissions = Arrays.stream(BusinessContext.values())
            .map(BusinessContextPolicy::find)
            .flatMap(Optional::stream)
            .flatMap(policy -> policy.permissions().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> unknown = policyPermissions.stream()
            .filter(permission -> !controllerPermissions.contains(permission))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        assertTrue(unknown.isEmpty(), () -> "业务上下文使用了非现有接口权限: " + unknown);
    }

    @Test
    void knownManagementListOptionCouplingsRemainExplicit() {
        Map<String, EndpointContract> endpoints = scanEndpoints().stream()
            .collect(Collectors.toMap(EndpointContract::key, endpoint -> endpoint));
        for (KnownCoupling coupling : KNOWN_OPTION_COUPLINGS) {
            EndpointContract endpoint = endpoints.get(coupling.method() + " " + coupling.path());
            assertTrue(endpoint != null, () -> "已知耦合接口已变化，请更新契约: " + coupling);
            assertEquals(List.of(coupling.permission()), endpoint.permissions(),
                () -> "Options 权限已变化，请同步完成语义审计: " + coupling);
            assertEquals(EndpointKind.BUSINESS_OPTIONS, endpoint.kind());
        }
    }

    @Test
    void customerRelationControllerServicePermissionsStayAligned() throws IOException {
        Path source = repositoryRoot().resolve(
            "ruoyi-modules/ruoyi-property/src/main/java/org/dromara/property/customer/relation/service/impl/RoomCustomerRelationServiceImpl.java");
        String service = Files.readString(source);

        assertTrue(service.contains("scope(ProjectBusinessAction.READ, \"property:customerRelation:list\")"));
        assertTrue(service.contains("access(projectId, ProjectBusinessAction.OPTION, \"property:customerRelation:add\")"));
        assertTrue(service.contains("scope(ProjectBusinessAction.OPTION, \"property:customerRelation:list\")"));
        assertFalse(service.contains("ProjectBusinessAction.OPTION, \"property:customerRelation:query\""));

        Map<String, EndpointContract> endpoints = scanEndpoints().stream()
            .collect(Collectors.toMap(EndpointContract::key, endpoint -> endpoint));
        assertEquals(List.of("property:customerRelation:list"),
            endpoints.get("GET /property/customer-relation/options").permissions());
        assertEquals(List.of("property:customerRelation:add"),
            endpoints.get("GET /property/customer-relation/customer-options").permissions());
        assertEquals(List.of("property:customerRelation:list"),
            endpoints.get("GET /property/customer-relation/project-options").permissions());
    }

    @Test
    void generatePermissionContractReport() throws IOException {
        List<EndpointContract> endpoints = scanEndpoints();
        Path output = Path.of("target", "permission-contract.csv");
        Files.createDirectories(output.getParent());
        List<String> lines = new ArrayList<>();
        lines.add("module,kind,http_method,path,permission_mode,permissions,controller,handler");
        endpoints.forEach(endpoint -> lines.add(String.join(",",
            csv(endpoint.module()), csv(endpoint.kind().name()), csv(endpoint.httpMethod()), csv(endpoint.path()),
            csv(endpoint.permissionMode()), csv(String.join("|", endpoint.permissions())),
            csv(endpoint.controller()), csv(endpoint.handler()))));
        Files.write(output, lines, StandardCharsets.UTF_8);
        assertFalse(endpoints.isEmpty());
    }

    private List<EndpointContract> scanEndpoints() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<EndpointContract> endpoints = new ArrayList<>();
        for (String basePackage : CONTROLLER_PACKAGES) {
            scanner.findCandidateComponents(basePackage).forEach(bean -> {
                try {
                    Class<?> controller = Class.forName(bean.getBeanClassName());
                    endpoints.addAll(readController(controller));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("无法加载 Controller: " + bean.getBeanClassName(), e);
                }
            });
        }
        endpoints.sort(Comparator.comparing(EndpointContract::module)
            .thenComparing(EndpointContract::path)
            .thenComparing(EndpointContract::httpMethod));
        return endpoints;
    }

    private List<EndpointContract> readController(Class<?> controller) {
        RequestMapping classMapping = AnnotatedElementUtils.findMergedAnnotation(controller, RequestMapping.class);
        List<String> bases = mappingPaths(classMapping);
        if (bases.isEmpty()) bases = List.of("");
        List<EndpointContract> endpoints = new ArrayList<>();
        for (Method method : controller.getDeclaredMethods()) {
            RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (mapping == null) continue;
            SaCheckPermission permission = AnnotatedElementUtils.findMergedAnnotation(method, SaCheckPermission.class);
            List<String> permissions = permission == null ? List.of() : Arrays.stream(permission.value()).sorted().toList();
            String mode = permission == null ? "NONE" : permission.mode() == SaMode.AND ? "AND" : "OR";
            List<String> paths = mappingPaths(mapping);
            if (paths.isEmpty()) paths = List.of("");
            List<String> methods = Arrays.stream(mapping.method()).map(Enum::name).toList();
            if (methods.isEmpty()) methods = List.of("ANY");
            for (String base : bases) {
                for (String path : paths) {
                    String fullPath = normalizePath(base + "/" + path);
                    for (String httpMethod : methods) {
                        endpoints.add(new EndpointContract(module(controller), classify(fullPath), httpMethod,
                            fullPath, mode, permissions, controller.getName(), method.getName()));
                    }
                }
            }
        }
        return endpoints;
    }

    private MenuContract readMenuContract() throws IOException {
        Path baseline = repositoryRoot().resolve("script/sql/zbm/baseline/03_menu_permission.sql");
        Matcher matcher = MENU_ROW.matcher(Files.readString(baseline));
        Map<String, Set<Long>> permissions = new LinkedHashMap<>();
        while (matcher.find()) {
            Long menuId = Long.valueOf(matcher.group(1));
            String permission = matcher.group(2);
            if (permission != null) {
                permissions.computeIfAbsent(permission, ignored -> new LinkedHashSet<>()).add(menuId);
            }
        }
        return new MenuContract(permissions);
    }

    private static Pattern menuRowPattern() {
        String token = "(?:NULL|'(?:''|[^'])*'|[^,()]+)";
        StringBuilder regex = new StringBuilder("\\(\\s*(\\d+)\\s*");
        for (int field = 2; field <= 12; field++) {
            regex.append(",\\s*").append(token).append("\\s*");
        }
        regex.append(",\\s*(?:NULL|'((?:property|warehouse|hr):[^']+)')");
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    private static List<String> mappingPaths(RequestMapping mapping) {
        if (mapping == null) return List.of();
        String[] paths = mapping.path().length == 0 ? mapping.value() : mapping.path();
        return Arrays.asList(paths);
    }

    private static EndpointKind classify(String path) {
        String lower = path.toLowerCase();
        if (lower.contains("option") || lower.contains("/context/")) return EndpointKind.BUSINESS_OPTIONS;
        if (lower.endsWith("/list") || lower.endsWith("/export") || lower.matches(".*/\\{[^/]+}$")) {
            return EndpointKind.MANAGEMENT;
        }
        return EndpointKind.BUSINESS_ACTION;
    }

    private static String module(Class<?> controller) {
        String name = controller.getName();
        if (name.startsWith("org.dromara.property")) return "property";
        if (name.startsWith("org.dromara.warehouse")) return "warehouse";
        if (name.startsWith("org.dromara.hr")) return "hr";
        return "unknown";
    }

    private static String normalizePath(String path) {
        String normalized = path.replaceAll("/+", "/");
        if (!normalized.startsWith("/")) normalized = "/" + normalized;
        return normalized.length() > 1 && normalized.endsWith("/")
            ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("script/sql/zbm/baseline/03_menu_permission.sql"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位仓库根目录");
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private enum EndpointKind {
        MANAGEMENT,
        BUSINESS_ACTION,
        BUSINESS_OPTIONS
    }

    private record EndpointContract(String module, EndpointKind kind, String httpMethod, String path,
                                    String permissionMode, List<String> permissions,
                                    String controller, String handler) {
        private String key() {
            return httpMethod + " " + path;
        }
    }

    private record MenuContract(Map<String, Set<Long>> permissionToMenuIds) {
    }

    private record KnownCoupling(String method, String path, String permission, String dependency) {
    }
}
