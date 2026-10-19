param(
    [Parameter(Mandatory = $true)][string]$DatabasePassword,
    [string]$Database = 'zbm_dev_v1',
    [string]$CredentialDirectory = 'backups/TASK-V1-005-D/dev-credentials',
    [string]$WslDistribution = 'archlinux',
    [string]$Container = 'jdpm-mysql'
)

$ErrorActionPreference = 'Stop'
if ($Database -ne 'zbm_dev_v1') {
    throw 'This development credential rotation script may only target zbm_dev_v1.'
}

$coreJar = Join-Path $env:USERPROFILE '.m2\repository\cn\dev33\sa-token-core\1.46.0\sa-token-core-1.46.0.jar'
if (-not (Test-Path $coreJar)) {
    throw 'Sa-Token BCrypt implementation was not found.'
}

$bytes = [byte[]]::new(18)
$random = [Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($bytes)
$random.Dispose()
$password = [Convert]::ToBase64String($bytes)
$escapedPassword = $password.Replace('\', '\\').Replace('"', '\"')
$jshellInput = @"
import cn.dev33.satoken.secure.BCrypt;
System.out.println("ZBM_DEV_HASH=" + BCrypt.hashpw("$escapedPassword"));
/exit
"@
$jshellOutput = $jshellInput | jshell --class-path $coreJar
$hashLine = $jshellOutput | Where-Object { $_ -match 'ZBM_DEV_HASH=\$2[aby]\$' } | Select-Object -Last 1
if (-not $hashLine) {
    throw 'BCrypt generation failed.'
}
$hash = ($hashLine -replace '^.*ZBM_DEV_HASH=', '').Trim()

$credentialPath = Join-Path (Resolve-Path (Join-Path $PSScriptRoot '../../../..')) $CredentialDirectory
New-Item -ItemType Directory -Force -Path $credentialPath | Out-Null
$passwordFile = Join-Path $credentialPath 'admin-initial-password.txt'
[IO.File]::WriteAllText($passwordFile, $password + "`n", [Text.UTF8Encoding]::new($false))

$sql = "UPDATE sys_user SET password='$hash', update_time=NOW() WHERE user_name='admin' AND user_id=1761100000000000001 AND del_flag='0'; SELECT ROW_COUNT();"
$result = $sql | & wsl.exe -d $WslDistribution -- docker exec -i -e "MYSQL_PWD=$DatabasePassword" $Container `
    mysql -uzbm_dev_v1 -D $Database -N 2>$null
if ($LASTEXITCODE -ne 0 -or ($result | Select-Object -Last 1) -ne '1') {
    throw 'The development administrator password was not updated exactly once.'
}

$password = $null
$escapedPassword = $null
$hash = $null
[ordered]@{
    database = $Database
    user = 'admin'
    credentialFile = $passwordFile
} | ConvertTo-Json
