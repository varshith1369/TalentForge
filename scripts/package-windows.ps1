# Build a Windows desktop app image with a bundled Java runtime.
# Run from the project root:
#   .\scripts\package-windows.ps1

param(
    [string]$AppName = "TalentForge",
    [string]$Version = "1.0.0",
    [string]$MainJar = "target\TalentForge-1.0-SNAPSHOT-shaded.jar",
    [string]$MainClass = "AuthTestApp",
    [string]$RuntimeImageDir = "target\runtime-image",
    [string]$InputDir = "target\jpackage-input",
    [string]$OutDir = "target\desktop-app"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "jpackage not found on PATH. Install a JDK 17+ that includes jpackage and try again."
}

if (-not (Get-Command jlink -ErrorAction SilentlyContinue)) {
    throw "jlink not found on PATH. Install a JDK 17+ that includes jlink and try again."
}

if (-not (Test-Path $MainJar)) {
    Write-Host "Shaded JAR not found. Building it with Maven..."
    mvn -q -DskipTests package
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

if (-not (Test-Path $MainJar)) {
    throw "Main JAR still not found: $MainJar"
}

Remove-Item -Recurse -Force $RuntimeImageDir -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force $InputDir -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force $OutDir -ErrorAction SilentlyContinue

New-Item -ItemType Directory -Force -Path $InputDir | Out-Null
Copy-Item -LiteralPath $MainJar -Destination $InputDir

if (Test-Path "talentforge.db") {
    Copy-Item -LiteralPath "talentforge.db" -Destination $InputDir
}

Write-Host "Creating compact runtime image..."
jlink --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml,jdk.crypto.ec `
    --output $RuntimeImageDir `
    --strip-debug `
    --compress=2 `
    --no-header-files `
    --no-man-pages

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Creating desktop app image..."
jpackage `
    --type app-image `
    --dest $OutDir `
    --name $AppName `
    --input $InputDir `
    --main-jar (Split-Path $MainJar -Leaf) `
    --main-class $MainClass `
    --app-version $Version `
    --runtime-image $RuntimeImageDir

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$exePath = Join-Path $OutDir "$AppName\$AppName.exe"
Write-Host "Desktop app ready: $exePath"
