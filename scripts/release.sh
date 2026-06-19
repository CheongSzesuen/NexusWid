#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

GRADLEW="$ROOT_DIR/gradlew"
if [[ ! -x "$GRADLEW" ]]; then
    echo "错误: 未找到可执行的 gradlew: $GRADLEW"
    exit 1
fi

APK_PATH="$ROOT_DIR/androidApp/build/outputs/apk/release/nexuswid-release.apk"
BUILD_MODE="release_clean"
BUILD_MODE_LABEL="清缓存 Release"
BUILD_TASKS=(clean :androidApp:assembleRelease)

timestamp() { date "+%H:%M:%S"; }
log_info() { echo "[$(timestamp)] [INFO] $*"; }
log_error() { echo "[$(timestamp)] [ERROR] $*" >&2; }
log_warn() { echo "[$(timestamp)] [WARN] $*"; }
die() { log_error "$*"; exit 1; }

# shellcheck source=lib/apk_server.sh
source "$ROOT_DIR/scripts/lib/apk_server.sh"

print_artifact_size() {
    local path="$1"
    local label="$2"
    if [[ -f "$path" ]]; then
        local size_h
        local size_b
        size_h="$(du -h "$path" | cut -f1)"
        size_b="$(stat -c%s "$path")"
        log_info "$label: $size_h (${size_b} bytes)"
        log_info "位置: $path"
    else
        die "$label 未找到: $path"
    fi
}

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

log_info "构建模式: ${BUILD_MODE_LABEL} (${BUILD_MODE})"
log_info "Gradle任务: ${BUILD_TASKS[*]}"
"$GRADLEW" "${BUILD_TASKS[@]}" "$@"

rename_apk_common "$APK_PATH" "androidApp-release.apk"

log_info "构建产物"
print_artifact_size "$APK_PATH" "APK"

if [[ -f "$APK_PATH" ]] && command -v unzip >/dev/null 2>&1; then
    log_info "APK 内文件体积 Top 15"
    unzip -l "$APK_PATH" \
        | awk 'NR > 3 && $4 != "" {print $1 "\t" $4}' \
        | sort -nr \
        | head -n 15 || true
fi

# 启动 HTTP 服务暴露 APK
if start_apk_server_common "$APK_PATH"; then
    log_info "下载地址: https://apk.waijade.cn/nexuswid-release.apk"
else
    log_warn "APK 服务启动失败，跳过本地分发"
fi

log_info "发布构建完成"
