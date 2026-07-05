# Packaging script for Windows using jlink + jpackage
# Prerequisites: JDK 17+ installed and jlink/jpackage on PATH
# Run from project root: .\scripts\package-windows.ps1

param(
    [string]$AppName = "TalentForge",
    [string]$Version = "1.0",
    [string]$MainJar = "target\TalentForge-1.0-SNAPSHOT-shaded.jar",
    [string]$MainClass = "AuthTestApp",
    [string]$RuntimeImageDir = "runtime",
    [string]$OutDir = "target\installer"
)

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    Write-Error "jpackage not found on PATH. Install a JDK 17+ that includes jpackage and try again."
    exit 1
}

if (-not (Test-Path $MainJar)) {
    Write-Error "Main JAR not found: $MainJar. Build with 'mvn clean package -DskipTests' first."
    exit 1
}

Write-Output "Creating runtime image in '$RuntimeImageDir' (may take a minute)..."
# Include java.sql module for JDBC (sqlite-jdbc) and java.desktop for GUI
jlink --add-modules java.base,java.desktop,java.sql --output $RuntimeImageDir --strip-debug --compress=2 --no-header-files --no-man-pages

if ($LASTEXITCODE -ne 0) {
    Write-Error "jlink failed. Ensure the JDK supports jlink and the specified modules are available."
    exit $LASTEXITCODE
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

Write-Output "Running jpackage to create installer in '$OutDir'..."
jpackage --input target --name $AppName --main-jar (Split-Path $MainJar -Leaf) --main-class $MainClass --type exe --dest $OutDir --app-version $Version --runtime-image $RuntimeImageDir

if ($LASTEXITCODE -ne 0) {
    Write-Error "jpackage failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Output "Packaging complete. Output in: $OutDir"