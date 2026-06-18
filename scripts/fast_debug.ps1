$ErrorActionPreference = "Stop"

$RootDir = Resolve-Path (Join-Path $PSScriptRoot "..")
$Gradlew = Join-Path $RootDir "gradlew.bat"
$AdbBin = if ($env:ADB) { $env:ADB } else { "adb" }
$PackageName = "cn.waijade.nexuswid.debug"
$MainActivity = "cn.waijade.nexuswid.MainActivity"
$ApkPath = Join-Path $RootDir "androidApp\build\outputs\apk\debug\androidApp-debug.apk"
$BuildTask = ":androidApp:assembleDebug"
$BuildMode = "debug_incremental"
$BuildModeLabel = "增量 Debug"

function Get-Timestamp {
    Get-Date -Format "HH:mm:ss"
}

function Log-Info([string]$Message) {
    "[$(Get-Timestamp)] [INFO] $Message"
}

function Log-Warn([string]$Message) {
    Write-Warning "[$(Get-Timestamp)] [WARN] $Message"
}

function Fail([string]$Message) {
    throw "[$(Get-Timestamp)] [ERROR] $Message"
}

function Require-Command([string]$CommandName) {
    if (-not (Get-Command $CommandName -ErrorAction SilentlyContinue)) {
        Fail "缺少命令: $CommandName"
    }
}

function Invoke-Native([scriptblock]$Command) {
    & $Command
    if ($LASTEXITCODE -ne 0) {
        Fail "命令执行失败，退出码: $LASTEXITCODE"
    }
}

function Select-Device {
    if ($env:ANDROID_SERIAL) {
        return $env:ANDROID_SERIAL
    }

    $devices = @(& $AdbBin devices |
        Where-Object { $_ -match "\tdevice$" } |
        ForEach-Object { ($_ -split "\t")[0] })

    if (-not $devices -or $devices.Count -eq 0) {
        Fail "未检测到可用设备（状态为 device）"
    }

    if ($devices.Count -gt 1) {
        Log-Warn "检测到多个设备，默认使用第一个: $($devices[0])"
    }

    return $devices[0]
}

function Print-ApkSize {
    if (-not (Test-Path -LiteralPath $ApkPath)) {
        Fail "APK 不存在: $ApkPath"
    }

    $size = (Get-Item -LiteralPath $ApkPath).Length
    $sizeMb = [math]::Round($size / 1MB, 2)
    Log-Info "APK: ${sizeMb} MB ($size bytes)"
}

Set-Location -LiteralPath $RootDir

Require-Command $AdbBin
if (-not (Test-Path -LiteralPath $Gradlew)) {
    Fail "gradlew.bat 不存在: $Gradlew"
}

Log-Info "开始快速调试流程"
Log-Info "构建模式: ${BuildModeLabel} (${BuildMode})"
Log-Info "Gradle任务: ${BuildTask}"
Log-Info "检查设备连接"

$Serial = Select-Device
Log-Info "目标设备: $Serial"

Log-Info "执行增量构建: ${BuildTask}"
Invoke-Native { & $Gradlew $BuildTask -x lint --configure-on-demand --parallel --daemon }

Print-ApkSize

Log-Info "安装 APK"
Invoke-Native { & $AdbBin -s $Serial install -r -t $ApkPath }

Log-Info "重启应用进程"
Invoke-Native { & $AdbBin -s $Serial shell am force-stop $PackageName }
Start-Sleep -Seconds 1

Log-Info "启动应用"
Invoke-Native { & $AdbBin -s $Serial shell am start -n "$PackageName/$MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER | Out-Null }

Log-Info "完成"
