param([string]$OutputPath)

$root = Resolve-Path (Join-Path $PSScriptRoot '../..')
$items = Get-ChildItem (Join-Path $root 'script/sql/zbm/archive/legacy-development-migrations/V*.sql') | ForEach-Object {
    if ($_.Name -notmatch '^V(?<Version>[0-9.]+)__(?<Name>.+)\.sql$') { return }
    [pscustomobject]@{
        Version = $Matches.Version
        File = $_.Name
        Sha256 = (Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    }
} | Sort-Object { [version]$_.Version }

if ($items.Count -ne 37 -or $items[0].Version -ne '2.0.1' -or $items[-1].Version -ne '2.0.36') {
    throw 'Migration chain must contain V2.0.1 through V2.0.36 plus V2.0.33.1 (37 SQL files).'
}

if ($OutputPath) {
    $items | ConvertTo-Json | Set-Content -Encoding utf8 $OutputPath
} else {
    $items
}

