param(
    [string]$HealthUrl = "http://localhost:8080/api/v1/health",
    [string]$AppUrl = "http://localhost:8080/",
    [int]$TimeoutSeconds = 120,
    [switch]$NoBrowser
)

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
Set-Location -LiteralPath $ProjectRoot

function Test-FlexFitHealth {
    param([string]$Url)
    try {
        $resp = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 3 -ErrorAction Stop
        if ($null -ne $resp -and $resp.status -eq "UP") {
            return $true
        }
        return $false
    } catch {
        return $false
    }
}

function Test-PortListening {
    param([int]$Port)
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $iar = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
        $connected = $iar.AsyncWaitHandle.WaitOne(400, $false)
        if ($connected -and $client.Connected) {
            $client.EndConnect($iar)
            return $true
        }
        return $false
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Open-FlexFitBrowser {
    param([string]$Url)
    if ($NoBrowser) {
        Write-Host "[DryRun/NoBrowser] Would open $Url in default browser." -ForegroundColor Cyan
        return
    }
    Write-Host "Opening $Url in your default browser..." -ForegroundColor Green
    Start-Process $Url | Out-Null
}

Write-Host "==========================================" -ForegroundColor DarkYellow
Write-Host " FlexFit Application Launcher" -ForegroundColor White
Write-Host " Project Root: $ProjectRoot" -ForegroundColor Gray
Write-Host "==========================================" -ForegroundColor DarkYellow

# Step 1: Check existing application
if (Test-FlexFitHealth -Url $HealthUrl) {
    Write-Host "FlexFit is already running and healthy on $AppUrl." -ForegroundColor Green
    Open-FlexFitBrowser -Url $AppUrl
    exit 0
}

if (Test-PortListening -Port 8080) {
    Write-Host "Port 8080 is already in use, but $HealthUrl did not report UP." -ForegroundColor Yellow
    Write-Host "Will not terminate the existing process or launch a duplicate instance." -ForegroundColor Yellow
    exit 1
}

# Step 2: Start backend in a separate PowerShell window
Write-Host "FlexFit is not running. Launching backend PowerShell window..." -ForegroundColor Cyan
Write-Host "Please enter your MySQL password in the new backend window when prompted." -ForegroundColor Cyan

$escapedRoot = $ProjectRoot.Replace("'", "''")
$backendScript = @"
Set-Location -LiteralPath '$escapedRoot'
Write-Host 'Starting FlexFit Spring Boot Backend...' -ForegroundColor Cyan

# Load gitignored .env if present (without printing values)
`$envFile = Join-Path '$escapedRoot' '.env'
if (Test-Path -LiteralPath `$envFile) {
    Get-Content -LiteralPath `$envFile | ForEach-Object {
        `$line = `$_.Trim()
        if (`$line -and -not `$line.StartsWith('#') -and `$line.Contains('=')) {
            `$parts = `$line.Split('=', 2)
            `$k = `$parts[0].Trim()
            `$v = `$parts[1].Trim().Trim('"').Trim("'")
            if (`$k -and -not [string]::IsNullOrEmpty(`$v) -and -not (Test-Path "Env:`$k")) {
                Set-Item -Path "Env:`$k" -Value `$v
            }
        }
    }
}

# Ensure persistent local JWT_SECRET (>= 32 bytes) without hardcoding or CLI exposure
`$createdJwtEnv = `$false
if ([string]::IsNullOrWhiteSpace(`$env:JWT_SECRET) -or ([System.Text.Encoding]::UTF8.GetByteCount(`$env:JWT_SECRET) -lt 32)) {
    `$secretDir = Join-Path `$env:LOCALAPPDATA 'FlexFit'
    `$secretPath = Join-Path `$secretDir 'jwt.secret'
    if (Test-Path -LiteralPath `$secretPath) {
        `$env:JWT_SECRET = (Get-Content -LiteralPath `$secretPath -Raw).Trim()
    }
    if ([string]::IsNullOrWhiteSpace(`$env:JWT_SECRET) -or ([System.Text.Encoding]::UTF8.GetByteCount(`$env:JWT_SECRET) -lt 32)) {
        New-Item -ItemType Directory -Force -Path `$secretDir | Out-Null
        `$bytes = New-Object byte[] 32
        [System.Security.Cryptography.RandomNumberGenerator]::Fill(`$bytes)
        `$env:JWT_SECRET = [Convert]::ToBase64String(`$bytes)
        Set-Content -LiteralPath `$secretPath -Value `$env:JWT_SECRET -NoNewline
    }
    `$createdJwtEnv = `$true
}

if ([string]::IsNullOrWhiteSpace(`$env:DB_PASSWORD)) {
    `$securePwd = Read-Host -Prompt 'Enter MySQL password (DB_PASSWORD)' -AsSecureString
    `$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR(`$securePwd)
    try {
        `$plainPwd = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(`$bstr)
        `$env:DB_PASSWORD = `$plainPwd
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR(`$bstr)
        `$plainPwd = `$null
    }
}

try {
    & '.\mvnw.cmd' -pl backend spring-boot:run
} finally {
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
    if (`$createdJwtEnv) {
        Remove-Item Env:JWT_SECRET -ErrorAction SilentlyContinue
    }
    Write-Host ''
    Write-Host 'Temporary secrets cleared from process environment. Backend process has ended.' -ForegroundColor Yellow
}
"@

$encodedCommand = [Convert]::ToBase64String([System.Text.Encoding]::Unicode.GetBytes($backendScript))
$backendProc = Start-Process -FilePath "powershell.exe" `
    -ArgumentList @("-NoExit", "-NoProfile", "-ExecutionPolicy", "Bypass", "-EncodedCommand", $encodedCommand) `
    -WorkingDirectory $ProjectRoot `
    -PassThru

# Step 3: Wait for backend health
Write-Host "Waiting up to $TimeoutSeconds seconds for FlexFit to become healthy..." -ForegroundColor Cyan
$deadline = (Get-Date).AddSeconds($TimeoutSeconds)

while ((Get-Date) -lt $deadline) {
    if ($backendProc.HasExited) {
        Write-Host "Backend PowerShell window exited unexpectedly (ExitCode: $($backendProc.ExitCode))." -ForegroundColor Red
        Write-Host "Check the backend PowerShell window for MySQL or Spring Boot startup errors." -ForegroundColor Yellow
        exit 1
    }

    if (Test-FlexFitHealth -Url $HealthUrl) {
        Write-Host "FlexFit backend is UP and healthy!" -ForegroundColor Green
        Open-FlexFitBrowser -Url $AppUrl
        exit 0
    }

    Start-Sleep -Seconds 2
}

Write-Host "Timed out after $TimeoutSeconds seconds waiting for $HealthUrl." -ForegroundColor Red
Write-Host "Please check the backend PowerShell window for MySQL connection or startup errors." -ForegroundColor Yellow
exit 1
