param(
    [ValidateSet('zbm_property_test','zbm-data')][string]$Database = 'zbm_property_test',
    [string]$WslDistribution = 'archlinux',
    [string]$Container = 'jdpm-mysql'
)

$root = Resolve-Path (Join-Path $PSScriptRoot '../..')
$baseline = Get-Content -Raw (Join-Path $root 'script/sql/zbm/baseline/01_schema.sql')
$expected = [regex]::Matches($baseline, '(?im)^CREATE TABLE `([^`]+)`') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
$actual = wsl.exe -d $WslDistribution -- docker exec $Container mysql -uroot -pbluezero -N -e `
    "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='$Database' ORDER BY TABLE_NAME" 2>$null
if ($LASTEXITCODE -ne 0) { throw 'Read-only information_schema query failed.' }

[ordered]@{
    database = $Database
    expectedBusinessTables = $expected.Count
    actualTables = $actual.Count
    missingBusinessTables = @($expected | Where-Object { $_ -notin $actual })
    extraBusinessTables = @($actual | Where-Object { $_ -notin $expected -and $_ -match '^(pm_|wm_|hr_)' })
} | ConvertTo-Json -Depth 4

