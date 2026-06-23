# Maven Installation Script for Windows
# Run: D:\Lablaptrinhjava\CuoikiJava1\install-maven.ps1

Write-Host "=== Maven Installation Script ===" -ForegroundColor Green

# Kiểm tra Maven đã cài
Write-Host "`n[1/4] Checking Maven..." -ForegroundColor Yellow
$MavenCheck = (& mvn -v 2>&1 | Select-String "Apache Maven") 2>$null
if ($MavenCheck) {
    Write-Host "Maven already installed: $MavenCheck" -ForegroundColor Green
    exit 0
}

# Định nghĩa biến
$MavenVersion = "3.9.6"
# Try multiple mirrors
$MavenUrls = @(
    "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/apache-maven-$MavenVersion-bin.zip",
    "https://dlcdn.apache.org/maven/maven-3/$MavenVersion/apache-maven-$MavenVersion-bin.zip",
    "https://mirror.softaculous.com/apache/maven/maven-3/$MavenVersion/apache-maven-$MavenVersion-bin.zip"
)
$MavenHome = "C:\tools\apache-maven-$MavenVersion"
$DownloadPath = "$env:TEMP\apache-maven-$MavenVersion-bin.zip"

# Bước 2: Tạo thư mục
Write-Host "`n[2/4] Creating C:\tools directory..." -ForegroundColor Yellow
if (!(Test-Path "C:\tools")) {
    New-Item -Path "C:\tools" -ItemType Directory -Force | Out-Null
    Write-Host "Created: C:\tools" -ForegroundColor Green
}

# Bước 3: Tải Maven
Write-Host "`n[3/4] Downloading Maven $MavenVersion..." -ForegroundColor Yellow
$Downloaded = $false
foreach ($Url in $MavenUrls) {
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $ProgressPreference = 'SilentlyContinue'
        Write-Host "Trying: $Url" -ForegroundColor Gray
        Invoke-WebRequest -Uri $Url -OutFile $DownloadPath -UseBasicParsing -TimeoutSec 30
        Write-Host "Downloaded: $DownloadPath" -ForegroundColor Green
        $Downloaded = $true
        break
    } catch {
        Write-Host "Failed with this URL, trying next..." -ForegroundColor Yellow
    }
}

if (-not $Downloaded) {
    Write-Host "ERROR: Failed to download from all mirrors" -ForegroundColor Red
    exit 1
}

# Bước 4: Giải nén
Write-Host "`n[4/4] Extracting Maven..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $DownloadPath -DestinationPath "C:\tools" -Force
    Write-Host "Extracted: $MavenHome" -ForegroundColor Green
    Remove-Item -Path $DownloadPath -Force
} catch {
    Write-Host "ERROR: Failed to extract Maven." -ForegroundColor Red
    exit 1
}

# Cấu hình PATH
Write-Host "`nConfiguring PATH..." -ForegroundColor Yellow
$CurrentPath = [Environment]::GetEnvironmentVariable('PATH', 'User')
if ($CurrentPath -notlike "*apache-maven*") {
    $NewPath = "$CurrentPath;$MavenHome\bin"
    [Environment]::SetEnvironmentVariable('PATH', $NewPath, 'User')
    Write-Host "Updated User PATH" -ForegroundColor Green
}

# Update session
$env:PATH = "$env:PATH;$MavenHome\bin"

# Verify
Write-Host "`nVerifying installation..." -ForegroundColor Yellow
& "$MavenHome\bin\mvn.cmd" -v
if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[SUCCESS] Maven installed! Close and reopen PowerShell, then run:" -ForegroundColor Green
    Write-Host "  mvn -v" -ForegroundColor Yellow
} else {
    Write-Host "`n[ERROR] Maven verification failed" -ForegroundColor Red
}



