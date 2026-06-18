#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

GRADLEW="$ROOT_DIR/gradlew"
ADB_BIN="${ADB:-adb}"
PACKAGE_NAME="cn.waijade.nexuswid"
MAIN_ACTIVITY=".MainActivity"
APK_PATH="$ROOT_DIR/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
BUILD_MODE="debug_incremental"
BUILD_MODE_LABEL="增量 Debug"
BUILD_TASK=":androidApp:assembleDebug"

timestamp() { date "+%H:%M:%S"; }
log_info() { echo "[$(timestamp)] [INFO] $*"; }
log_warn() { echo "[$(timestamp)] [WARN] $*" >&2; }
log_error() { echo "[$(timestamp)] [ERROR] $*" >&2; }
die() { log_error "$*"; exit 1; }

require_cmd() {
    command -v "$1" >/dev/null 2>&1 || die "缺少命令: $1"
}

select_device() {
    if [[ -n "${ANDROID_SERIAL:-}" ]]; then
        echo "$ANDROID_SERIAL"
        return
    fi

    mapfile -t devices < <("$ADB_BIN" devices | awk '/\tdevice$/{print $1}')
    if [[ ${#devices[@]} -eq 0 ]]; then
        die "未检测到可用设备（状态为 device）"
    fi
    if [[ ${#devices[@]} -gt 1 ]]; then
        log_warn "检测到多个设备，默认使用第一个: ${devices[0]}"
    fi
    echo "${devices[0]}"
}

print_apk_size() {
    [[ -f "$APK_PATH" ]] || die "APK 不存在: $APK_PATH"
    local size_h size_b
    size_h="$(du -h "$APK_PATH" | cut -f1)"
    size_b="$(stat -c%s "$APK_PATH")"
    log_info "APK: ${size_h} (${size_b} bytes)"
}

main() {
    require_cmd "$ADB_BIN"
    [[ -x "$GRADLEW" ]] || die "gradlew 不可执行: $GRADLEW"

    log_info "开始快速调试流程"
    log_info "构建模式: ${BUILD_MODE_LABEL} (${BUILD_MODE})"
    log_info "Gradle任务: ${BUILD_TASK}"
    log_info "检查设备连接"

    local serial
    serial="$(select_device)"
    local adb_cmd=("$ADB_BIN" -s "$serial")
    log_info "目标设备: $serial"

    if [[ "$(uname -s)" == Linux ]] && ! java -version 2>&1 | grep -q 'version "\(17\|18\|19\|20\|21\)\.'; then
        for jdk in /usr/lib/jvm/java-17-openjdk /usr/lib/jvm/java-21-openjdk; do
            if [[ -x "$jdk/bin/java" ]]; then
                export JAVA_HOME="$jdk"
                export PATH="$JAVA_HOME/bin:$PATH"
                log_info "自动切换到 JDK 17: $JAVA_HOME"
                break
            fi
        done
        if ! java -version 2>&1 | grep -q 'version "\(17\|18\|19\|20\|21\)\.'; then
            log_warn "当前 JDK 版本可能不被 AGP 支持 (需 JDK 17~21): $(java -version 2>&1)"
        fi
    fi

    log_info "执行增量构建: ${BUILD_TASK}"
    "$GRADLEW" "$BUILD_TASK" -x lint --configure-on-demand --parallel --daemon

    print_apk_size

    log_info "安装 APK"
    "${adb_cmd[@]}" install -r -t "$APK_PATH"

    log_info "重启应用进程"
    "${adb_cmd[@]}" shell am force-stop "$PACKAGE_NAME"
    sleep 1

    log_info "启动应用"
    "${adb_cmd[@]}" shell am start -n "${PACKAGE_NAME}/${MAIN_ACTIVITY}" \
        -a android.intent.action.MAIN \
        -c android.intent.category.LAUNCHER >/dev/null

    log_info "完成"
}

main "$@"
