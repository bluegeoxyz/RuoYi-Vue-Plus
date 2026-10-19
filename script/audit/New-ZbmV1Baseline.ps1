param(
    [string]$Database = 'zbm-data',
    [string]$WslDistribution = 'archlinux',
    [string]$Container = 'jdpm-mysql',
    [string]$Output = 'ruoyi-admin/src/main/resources/db/migration/V1__initial_schema.sql'
)

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false
$root = Resolve-Path (Join-Path $PSScriptRoot '../..')
$outputPath = Join-Path $root $Output

if ($Database -ne 'zbm-data') {
    throw 'This generator is audited only against the read-only zbm-data structure source.'
}

$tables = & wsl.exe -d $WslDistribution -- docker exec -e MYSQL_PWD=bluezero $Container mysql -uroot -D $Database -N -e `
    "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='$Database' AND TABLE_NAME NOT IN ('test_demo','test_tree','flyway_schema_history') ORDER BY TABLE_NAME" 2>$null
if ($LASTEXITCODE -ne 0 -or $tables.Count -ne 59) {
    throw "Expected 59 retained tables, got $($tables.Count)."
}

$dumpArgs = @(
    '-d', $WslDistribution, '--', 'docker', 'exec', '-e', 'MYSQL_PWD=bluezero', $Container,
    'mysqldump', '-uroot',
    '--no-data', '--skip-comments', '--skip-add-locks', '--skip-disable-keys', '--skip-lock-tables',
    '--set-gtid-purged=OFF', '--no-tablespaces', '--default-character-set=utf8mb4',
    $Database
) + $tables
$schema = (& wsl.exe @dumpArgs 2>$null) -join "`n"
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($schema)) {
    throw 'Schema dump failed.'
}
$schema = $schema -replace '(?m)^DROP TABLE IF EXISTS .*\r?\n', ''
$schema = $schema -replace ' AUTO_INCREMENT=\d+', ''

function Get-SeedDump([string]$Table, [string]$Where = '') {
    $args = @(
        '-d', $WslDistribution, '--', 'docker', 'exec', '-e', 'MYSQL_PWD=bluezero', $Container,
        'mysqldump', '-uroot',
        '--no-create-info', '--skip-triggers', '--skip-comments', '--skip-add-locks', '--skip-lock-tables',
        '--skip-disable-keys', '--compact', '--complete-insert', '--set-gtid-purged=OFF',
        '--no-tablespaces', '--default-character-set=utf8mb4'
    )
    if ($Where) { $args += "--where=$Where" }
    $args += @($Database, $Table)
    $value = (& wsl.exe @args 2>$null) -join "`n"
    if ($LASTEXITCODE -ne 0) { throw "Seed dump failed for $Table." }
    return $value
}

$businessRoles = "'superadmin','property_operation_manager','property_customer_service','property_cashier','property_finance_manager','property_warehouse_keeper','property_warehouse_manager','property_project_manager'"
$removedMenu = "COALESCE(perms,'') REGEXP '^(demo|workflow|monitor:snail(ai|job)):' OR COALESCE(component,'') REGEXP '^(demo|workflow|ai/chat|monitor/snail(ai|job))' OR COALESCE(path,'') IN ('demo','workflow','ai','aichat','snailai','snailjob')"
$roleIds = "SELECT role_id FROM sys_role WHERE role_key IN ($businessRoles)"
$menuIds = "SELECT menu_id FROM sys_menu WHERE NOT ($removedMenu)"

$seed = [System.Collections.Generic.List[string]]::new()
$seed.Add((Get-SeedDump 'sys_dept' "dept_id IN (1761000000000000100,1761000000000000101,1761000000000000103)"))
$seed.Add((Get-SeedDump 'sys_post' "post_id IN (1761200000000000001,1761200000000000002,1761200000000000003,1761200000000000004)"))
$seed.Add((Get-SeedDump 'sys_role' "role_key IN ($businessRoles)"))
$seed.Add((Get-SeedDump 'sys_user' 'user_id=1761100000000000001'))
$seed.Add((Get-SeedDump 'sys_user_role' 'user_id=1761100000000000001'))
$seed.Add((Get-SeedDump 'sys_user_post' 'user_id=1761100000000000001'))
$seed.Add((Get-SeedDump 'sys_role_dept' "role_id IN ($roleIds)"))
$seed.Add((Get-SeedDump 'sys_config'))
$seed.Add((Get-SeedDump 'sys_dict_type'))
$seed.Add((Get-SeedDump 'sys_dict_data'))
$seed.Add((Get-SeedDump 'sys_menu' "NOT ($removedMenu)"))
$seed.Add((Get-SeedDump 'sys_role_menu' "role_id IN ($roleIds) AND menu_id IN ($menuIds)"))
$seed.Add((Get-SeedDump 'sys_client'))

$header = @'
-- Zero Boundary Property Management V1 database baseline.
-- Generated from the read-only zbm-data structure by script/audit/New-ZbmV1Baseline.ps1.
-- Authoritative Flyway migration for new empty MySQL 8.0 databases.
-- Do not execute against an existing non-empty database and never edit after release.
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
'@
$footer = @'

SET FOREIGN_KEY_CHECKS = 1;
'@
$content = $header + "`n`n" + $schema.Trim() + "`n`n-- Required bootstrap data only; no development business transactions.`n" + (($seed | Where-Object { $_ }) -join "`n") + $footer

$directory = Split-Path -Parent $outputPath
New-Item -ItemType Directory -Force -Path $directory | Out-Null
[System.IO.File]::WriteAllText($outputPath, $content, [System.Text.UTF8Encoding]::new($false))

[ordered]@{
    output = $outputPath
    retainedTables = $tables.Count
    sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $outputPath).Hash.ToLowerInvariant()
    bytes = (Get-Item -LiteralPath $outputPath).Length
} | ConvertTo-Json
