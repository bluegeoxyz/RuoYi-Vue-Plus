param(
    [string]$BaseUrl = 'http://127.0.0.1:8080',
    [Parameter(Mandatory = $true)][string]$CredentialDirectory
)

$ErrorActionPreference = 'Stop'
$clientId = 'e5cd7e4891bf95d1d19206ce24a7b32e'
$password = (& wsl -d archlinux -- cat "$CredentialDirectory/initial-password.txt").Trim()
if ([string]::IsNullOrWhiteSpace($password)) { throw 'Credential file is empty.' }

$users = @(
    'test_customer', 'test_operation', 'test_cashier', 'test_finance', 'test_warehouse',
    'test_wh_manager', 'test_pm_a', 'test_pm_b', 'test_multi_role', 'test_no_project'
)
$tokens = @{}
$infos = @{}

function Invoke-Login([string]$userName) {
    $body = @{ clientId = $clientId; grantType = 'password'; username = $userName; password = $password } |
        ConvertTo-Json -Compress
    $response = Invoke-RestMethod -Method Post -Uri "$BaseUrl/auth/login" -ContentType 'application/json' -Body $body
    if ($response.code -ne 200 -or [string]::IsNullOrWhiteSpace($response.data.access_token)) {
        throw "Authentication failed for $userName (code=$($response.code), message=$($response.msg))."
    }
    return $response.data.access_token
}

function Invoke-Api([string]$userName, [string]$path, [string]$method = 'GET') {
    $headers = @{ Authorization = "Bearer $($tokens[$userName])"; clientid = $clientId }
    try {
        return Invoke-RestMethod -Method $method -Uri "$BaseUrl$path" -Headers $headers
    } catch {
        if ($_.ErrorDetails.Message) { return ($_.ErrorDetails.Message | ConvertFrom-Json) }
        throw
    }
}

function Assert-True([bool]$condition, [string]$message) {
    if (-not $condition) { throw $message }
}

function Json-Contains($value, [string]$needle) {
    return (($value | ConvertTo-Json -Depth 20 -Compress) -like "*$needle*")
}

try {
    foreach ($user in $users) {
        $tokens[$user] = Invoke-Login $user
        $infos[$user] = Invoke-Api $user '/system/user/getInfo'
        Assert-True ($infos[$user].code -eq 200) "getInfo failed for $user."
        Assert-True (-not (Json-Contains $infos[$user].data.permissions 'system:role')) "$user unexpectedly has system role management permission."
        Assert-True (-not (Json-Contains $infos[$user].data.roles 'superadmin')) "$user unexpectedly has SuperAdmin."
    }

    $customerA = Invoke-Api 'test_customer' '/property/customer-relation/context/create/projects'
    Assert-True ($customerA.code -eq 200) 'Customer create context was denied.'
    Assert-True (Json-Contains $customerA.data 'TEST_AUTH_A') 'Customer cannot see project A.'
    Assert-True (-not (Json-Contains $customerA.data 'TEST_AUTH_B')) 'Customer leaked project B.'

    $noProject = Invoke-Api 'test_no_project' '/property/customer-relation/context/create/projects'
    Assert-True ($noProject.code -eq 200) 'No-project account context call failed.'
    Assert-True (($null -eq $noProject.data) -or ($noProject.data.Count -eq 0)) 'No-project account received project data.'

    $financeProjects = Invoke-Api 'test_finance' '/property/charge/binding/context/list/projects'
    Assert-True ($financeProjects.code -eq 200) 'Finance charge binding context was denied.'
    Assert-True ((Json-Contains $financeProjects.data 'TEST_AUTH_A') -and (Json-Contains $financeProjects.data 'TEST_AUTH_B')) 'Finance account did not receive both projects.'
    Assert-True (-not (Json-Contains $infos['test_finance'].data.permissions 'property:room:list')) 'Finance account still has property:room:list.'

    $cashierRoomsA = Invoke-Api 'test_cashier' '/property/payment/context/create/rooms?projectId=2199011000000000101'
    Assert-True ($cashierRoomsA.code -eq 200) "Cashier payment context for project A failed (code=$($cashierRoomsA.code), message=$($cashierRoomsA.msg))."
    Assert-True (($null -ne $cashierRoomsA.data) -and ($cashierRoomsA.data.Count -gt 0)) "Cashier cannot see project A collectable room: $($cashierRoomsA.data | ConvertTo-Json -Depth 8 -Compress)"
    $cashierRoomsB = Invoke-Api 'test_cashier' '/property/payment/context/create/rooms?projectId=2199011000000000102'
    Assert-True ($cashierRoomsB.code -ne 200) 'Cashier crossed into project B payment context.'

    $warehouseA = Invoke-Api 'test_warehouse' '/warehouse/inbound/context/warehouses'
    Assert-True ($warehouseA.code -eq 200) 'Warehouse keeper inbound context failed.'
    Assert-True (Json-Contains $warehouseA.data 'TEST-A-WH') 'Warehouse keeper cannot see project A warehouse.'
    Assert-True (-not (Json-Contains $warehouseA.data 'TEST-B-WH')) 'Warehouse keeper leaked project B warehouse.'
    Assert-True (-not (Json-Contains $warehouseA.data 'TEST-COMPANY-WH')) 'Warehouse keeper unexpectedly received company warehouse through SELF data scope.'

    $managerWarehouses = Invoke-Api 'test_wh_manager' '/warehouse/inbound/context/warehouses'
    Assert-True (($managerWarehouses.code -eq 200) -and (Json-Contains $managerWarehouses.data 'TEST-A-WH') -and (Json-Contains $managerWarehouses.data 'TEST-B-WH')) 'Warehouse manager did not receive both project warehouses.'

    Assert-True (Json-Contains $infos['test_multi_role'].data.permissions 'property:customerRelation:add') 'Multi-role account lacks customer relation permission.'
    Assert-True (Json-Contains $infos['test_multi_role'].data.permissions 'property:payment:add') 'Multi-role account lacks cashier permission.'

    $forbiddenConfirm = Invoke-Api 'test_warehouse' '/warehouse/inbound/2199011999999999999/confirm' 'POST'
    Assert-True ($forbiddenConfirm.code -ne 200) 'Warehouse keeper unexpectedly passed inbound confirm authorization.'
    Assert-True (-not (Json-Contains $infos['test_cashier'].data.permissions 'property:payment:reverse')) 'Cashier unexpectedly has reversal permission.'

    [pscustomobject]@{
        authenticatedUsers = $users.Count
        customerProjectIsolation = 'PASS'
        noProjectEmptyScope = 'PASS'
        financeChargeBindingWithoutRoomList = 'PASS'
        cashierProjectIsolation = 'PASS'
        warehouseProjectIsolation = 'PASS'
        multiRoleUnion = 'PASS'
        forbiddenOperations = 'PASS'
    } | ConvertTo-Json -Compress
} finally {
    $password = $null
    $tokens.Clear()
}
