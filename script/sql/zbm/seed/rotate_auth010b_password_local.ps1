param(
    [Parameter(Mandatory = $true)][string]$CredentialDirectory
)

$ErrorActionPreference = 'Stop'
$coreJar = Join-Path $env:USERPROFILE '.m2\repository\cn\dev33\sa-token-core\1.46.0\sa-token-core-1.46.0.jar'
if (-not (Test-Path $coreJar)) { throw 'Sa-Token BCrypt implementation was not found.' }

$bytes = New-Object byte[] 18
$random = [Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($bytes)
$random.Dispose()
$password = [Convert]::ToBase64String($bytes)
$escapedPassword = $password.Replace('\', '\\').Replace('"', '\"')
$jshellInput = @"
import cn.dev33.satoken.secure.BCrypt;
System.out.println("AUTH010B_HASH=" + BCrypt.hashpw("$escapedPassword"));
/exit
"@
$jshellOutput = $jshellInput | jshell --class-path $coreJar
$hashLine = $jshellOutput | Where-Object { $_ -match 'AUTH010B_HASH=\$2[aby]\$' } | Select-Object -Last 1
if (-not $hashLine) { throw 'BCrypt generation failed.' }
$hash = ($hashLine -replace '^.*AUTH010B_HASH=', '').Trim()

$credentialPath = "\\wsl.localhost\archlinux$CredentialDirectory"
[IO.File]::WriteAllText((Join-Path $credentialPath 'initial-password.txt'), $password + "`n", (New-Object Text.UTF8Encoding($false)))
[IO.File]::WriteAllText((Join-Path $credentialPath 'password.bcrypt'), $hash + "`n", (New-Object Text.UTF8Encoding($false)))
& wsl -d archlinux -- chmod 600 "$CredentialDirectory/initial-password.txt" "$CredentialDirectory/password.bcrypt"
if ($LASTEXITCODE -ne 0) { throw 'Could not secure credential files.' }

& wsl -d archlinux -- bash /mnt/e/space/project/zbm-platform/script/sql/zbm/seed/apply_auth010b_password_local.sh $CredentialDirectory | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Could not update TEST-AUTH-010B passwords.' }

$password = $null
$escapedPassword = $null
$hash = $null
Write-Output '{"rotatedTestAccounts":10,"credentialFilesMode":"0600"}'
