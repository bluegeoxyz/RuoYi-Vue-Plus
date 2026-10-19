param([ValidateSet('single','property','http','all')][string]$Scope = 'single')

$required = 'ZBM_TEST_DB_URL','ZBM_TEST_DB_USER','ZBM_TEST_DB_PASSWORD','ZBM_TEST_AES_KEY','ZBM_TEST_HMAC_KEY_BASE64'
foreach ($name in $required) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) { throw "$name is required." }
}
if ($env:ZBM_TEST_DB_URL -notmatch '^jdbc:mysql://[^/]+/zbm_property_test(?:\?|$)') {
    throw 'Refusing to run: JDBC URL must target exactly zbm_property_test.'
}
if ($env:ZBM_TEST_DB_USER -eq 'root') { throw 'Refusing to run integration tests as MySQL root.' }
if ($env:ZBM_TEST_REDIS_DATABASE -ne '15') { throw 'ZBM_TEST_REDIS_DATABASE must be 15.' }

$marker = wsl.exe -d archlinux -- docker exec jdpm-mysql mysql -uroot -pbluezero -N -e `
    "SELECT COUNT(*) FROM zbm_property_test.zbm_test_environment WHERE environment_key='environment' AND environment_value='integration-test'" 2>$null
if ($LASTEXITCODE -ne 0 -or $marker -ne '1') { throw 'Integration-test database marker is missing.' }

$maven = Join-Path (Resolve-Path (Join-Path $PSScriptRoot '../..')) 'mvnw.cmd'
$common = @('-Dsurefire.failIfNoSpecifiedTests=false', '-Dspring.data.redis.database=15', '-Dredisson.singleServerConfig.database=15')
switch ($Scope) {
    'single' { & $maven -pl ruoyi-modules/ruoyi-property -am '-Dtest=ChargeItemMySqlTest' @common test }
    'property' { & $maven -pl ruoyi-modules/ruoyi-property -am @common test }
    'http' {
        $env:ZBM_HTTP_TEST_ENABLED = 'true'
        & $maven -pl ruoyi-admin -am '-Dtest=*HttpIntegrationTest,*HttpSecurityIntegrationTest' @common test
    }
    'all' {
        $env:ZBM_HTTP_TEST_ENABLED = 'true'
        & $maven @common test
    }
}
exit $LASTEXITCODE
