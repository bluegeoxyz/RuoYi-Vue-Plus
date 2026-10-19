param(
    [string]$FrontendRoot = 'E:\space\project\zbm-platform-ui',
    [string[]]$AllowedBackendOnly = @()
)

$root = Resolve-Path (Join-Path $PSScriptRoot '../..')
$java = Get-ChildItem $root -Recurse -Filter '*.java' | Where-Object { $_.FullName -notmatch '\\target\\' }
$backend = $java | ForEach-Object {
    [regex]::Matches((Get-Content -Raw $_.FullName), '@SaCheckPermission\s*\(\s*(?:value\s*=\s*)?\{?\s*"([^"]+)"') |
        ForEach-Object { $_.Groups[1].Value }
} | Sort-Object -Unique
$backend = @($backend | Where-Object { $_ -match '^(property|warehouse|hr):' })

$frontend = Get-ChildItem (Join-Path $FrontendRoot 'src') -Recurse -File -Include '*.vue','*.ts','*.tsx' | ForEach-Object {
    [regex]::Matches((Get-Content -Raw $_.FullName), '(?:v-hasPermi|hasPermi)[^\r\n]*?[''"]((?:property|warehouse|hr):[^''"]+)[''"]') |
        ForEach-Object { $_.Groups[1].Value }
} | Sort-Object -Unique
$frontend = @($frontend | Where-Object { $_ -notmatch '\$\{' })

$sqlText = Get-Content -Raw (Join-Path $root 'script/sql/zbm/baseline/03_menu_permission.sql')
$menu = [regex]::Matches($sqlText, "'((?:property|warehouse|hr):[^']+)'") |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique

$result = [ordered]@{
    backendCount = $backend.Count
    frontendCount = $frontend.Count
    menuCount = $menu.Count
    backendMissingFromMenu = @($backend | Where-Object { $_ -notin $menu -and $_ -notin $AllowedBackendOnly })
    frontendMissingFromBackend = @($frontend | Where-Object { $_ -notin $backend })
    frontendMissingFromMenu = @($frontend | Where-Object { $_ -notin $menu })
    menuWithoutController = @($menu | Where-Object { $_ -notin $backend })
}
$result | ConvertTo-Json -Depth 5
if ($result.backendMissingFromMenu.Count -or $result.frontendMissingFromMenu.Count) { exit 1 }

