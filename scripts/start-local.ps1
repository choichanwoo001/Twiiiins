param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runtimeRoot = Join-Path $env:USERPROFILE '.cache/twiiiins-local-runtime'
$mysqlRoot = Join-Path $runtimeRoot 'mysql/mysql-8.0.43-winx64'
$localRoot = Join-Path $projectRoot '.local'
$logRoot = Join-Path $localRoot 'logs'
New-Item -ItemType Directory -Force -Path $logRoot | Out-Null
& python (Join-Path $PSScriptRoot 'setup-local-runtime.py')
if ($LASTEXITCODE -ne 0) { throw 'Local runtime setup failed.' }
function PortOwner([int]$Port) {
    $connection = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($connection) { return Get-CimInstance Win32_Process -Filter "ProcessId=$($connection.OwningProcess)" }
}
function WaitPort([int]$Port) {
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        if (PortOwner $Port) { return }
        Start-Sleep -Seconds 1
    }
    throw "Local port $Port did not start. See $logRoot."
}
$mysqlData = Join-Path $localRoot 'mysql-data'
$mysqlExe = Join-Path $mysqlRoot 'bin/mysqld.exe'
$mysqlOwner = PortOwner 3307
if ($mysqlOwner -and ($mysqlOwner.ExecutablePath -ne $mysqlExe -or !$mysqlOwner.CommandLine.Contains($mysqlData))) {
    throw 'Port 3307 belongs to another database. No changes were made to it.'
}
if (!$mysqlOwner) {
    if (!(Test-Path (Join-Path $mysqlData 'mysql'))) {
        New-Item -ItemType Directory -Force -Path $mysqlData | Out-Null
        & $mysqlExe --no-defaults --initialize-insecure "--basedir=$mysqlRoot" "--datadir=$mysqlData" --console
        if ($LASTEXITCODE -ne 0) { throw 'Local MySQL initialization failed.' }
    }
    $mysqlArgs = @('--no-defaults', "--basedir=`"$mysqlRoot`"", "--datadir=`"$mysqlData`"", '--port=3307', '--bind-address=127.0.0.1', '--mysqlx=OFF', '--skip-log-bin', '--max-allowed-packet=64M', "--log-error=`"$(Join-Path $logRoot 'mysql.log')`"")
    Start-Process -FilePath $mysqlExe -ArgumentList $mysqlArgs -WindowStyle Hidden | Out-Null
    WaitPort 3307
}
& python (Join-Path $PSScriptRoot 'seed-local-db.py') --bootstrap-only
if ($LASTEXITCODE -ne 0) { throw 'Local database bootstrap failed.' }
$mailpitExe = Join-Path $runtimeRoot 'mailpit/mailpit.exe'
$mailOwner = PortOwner 1025
if ($mailOwner -and $mailOwner.ExecutablePath -ne $mailpitExe) { throw 'Port 1025 belongs to another service.' }
if (!$mailOwner) {
    if (PortOwner 8025) { throw 'Port 8025 belongs to another service.' }
    # Never inherit relay/forwarding settings into the local capture server.
    $mailpitEnvironment = @{}
    Get-ChildItem Env:MP_* | ForEach-Object { $mailpitEnvironment[$_.Name] = $_.Value; [Environment]::SetEnvironmentVariable($_.Name, $null) }
    try {
        Start-Process -FilePath $mailpitExe -ArgumentList @('--smtp=127.0.0.1:1025', '--listen=127.0.0.1:8025', "--database=`"$(Join-Path $localRoot 'mailpit.db')`"", '--disable-version-check', '--label=TWIIIINS-local') -WindowStyle Hidden -RedirectStandardOutput (Join-Path $logRoot 'mailpit.stdout.log') -RedirectStandardError (Join-Path $logRoot 'mailpit.stderr.log') | Out-Null
    } finally { foreach ($name in $mailpitEnvironment.Keys) { [Environment]::SetEnvironmentVariable($name, $mailpitEnvironment[$name]) } }
    WaitPort 1025
}
$javaRoot = $env:JAVA_HOME
if (!$javaRoot -or !(Test-Path (Join-Path $javaRoot 'bin/java.exe'))) {
    $javaRoot = Join-Path $env:USERPROFILE '.cache/twiiiins-test-java/jdk-17.0.20.1+1'
}
if (!(Test-Path (Join-Path $javaRoot 'bin/java.exe'))) { throw 'Java 17 is required. Set JAVA_HOME to your JDK.' }
$env:JAVA_HOME = $javaRoot
$backendRoot = Join-Path $projectRoot 'backend'
if (!$SkipBuild) {
    Push-Location $backendRoot
    try { & .\gradlew.bat bootJar --no-daemon; if ($LASTEXITCODE -ne 0) { throw 'Backend build failed.' } }
    finally { Pop-Location }
}
$jar = Get-ChildItem (Join-Path $backendRoot 'build/libs') -Filter '*.jar' | Where-Object { $_.Name -notlike '*-plain.jar' } | Select-Object -First 1
if (!$jar) { throw 'Build the backend before using -SkipBuild.' }
$backendOwner = PortOwner 8080
if ($backendOwner -and !$backendOwner.CommandLine.Contains($jar.FullName)) { throw 'Port 8080 belongs to another service.' }
if (!$backendOwner) {
    $values = @{
        DB_URL='jdbc:mysql://127.0.0.1:3307/twiiiins_local?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true'
        DB_USERNAME='twiiiins_local'; DB_PASSWORD='twiiiins-local-dev'; JPA_DDL_AUTO='update'; PORT='8080'
        DEFAULT_ADMIN_USERNAME='dowon'; DEFAULT_ADMIN_PASSWORD='1234'
        FILE_UPLOAD_DIR=($backendRoot.Replace('\','/') + '/uploads'); FILE_BASE_URL='/uploads'
        CORS_ORIGINS='http://localhost:5173,http://127.0.0.1:5173'
        NEWSLETTER_ENABLED='true'; NEWSLETTER_TOKEN_KEY='local-development-newsletter-key-123456789'
        NEWSLETTER_PUBLIC_URL='http://localhost:5173'; NEWSLETTER_FROM='newsletter@twiiiins.local'; NEWSLETTER_REPLY_TO='contact@twiiiins.local'
        NEWSLETTER_OPERATOR='TWIIIINS Local Development'; NEWSLETTER_TEST_RECIPIENTS='admin@example.com'
        NEWSLETTER_LOCAL_MAILBOX_URL='http://localhost:8025'
        SMTP_HOST='127.0.0.1'; SMTP_PORT='1025'; SMTP_AUTH='false'; SMTP_STARTTLS='false'; SMTP_SSL='false'
        SMTP_USERNAME=''; SMTP_PASSWORD=''; NEWSLETTER_PER_MINUTE='10'; NEWSLETTER_DAILY_LIMIT='300'
    }
    $previous = @{}
    foreach ($name in $values.Keys) { $previous[$name] = [Environment]::GetEnvironmentVariable($name); [Environment]::SetEnvironmentVariable($name, $values[$name]) }
    try {
        $process = Start-Process -FilePath (Join-Path $javaRoot 'bin/java.exe') -ArgumentList @('-jar', "`"$($jar.FullName)`"") -WorkingDirectory $backendRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $logRoot 'backend.stdout.log') -RedirectStandardError (Join-Path $logRoot 'backend.stderr.log') -PassThru
        $process.Id | Set-Content (Join-Path $localRoot 'backend.pid')
    } finally { foreach ($name in $previous.Keys) { [Environment]::SetEnvironmentVariable($name, $previous[$name]) } }
}
$ready = $false
for ($attempt = 0; $attempt -lt 90; $attempt++) {
    try { $health = Invoke-RestMethod 'http://127.0.0.1:8080/actuator/health'; if ($health.status -eq 'UP') { $ready = $true; break } } catch { }
    Start-Sleep -Seconds 1
}
if (!$ready) { throw "Backend startup failed. See $logRoot/backend.stdout.log." }
& python (Join-Path $PSScriptRoot 'seed-local-db.py')
if ($LASTEXITCODE -ne 0) { throw 'Local data import failed.' }
$frontendRoot = Join-Path $projectRoot 'frontend'
$envFile = Join-Path $frontendRoot '.env.local'
$lines = if (Test-Path $envFile) { Get-Content -LiteralPath $envFile | Where-Object { $_ -notmatch '^VITE_(DUMMY_DATA|API_BASE_URL)=' } } else { @() }
@($lines; 'VITE_DUMMY_DATA=false'; 'VITE_API_BASE_URL=/api') | Set-Content -LiteralPath $envFile -Encoding utf8
if (!(PortOwner 5173)) {
    $node = (Get-Command node.exe).Source
    Start-Process -FilePath $node -ArgumentList @('node_modules/vite/bin/vite.js', '--host', '127.0.0.1', '--port', '5173', '--strictPort') -WorkingDirectory $frontendRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $logRoot 'frontend.stdout.log') -RedirectStandardError (Join-Path $logRoot 'frontend.stderr.log') | Out-Null
    WaitPort 5173
}
Write-Output 'Local site: http://localhost:5173'
Write-Output 'Administrator: dowon / 1234'
Write-Output 'Local captured mail: http://localhost:8025 (no external delivery)'
