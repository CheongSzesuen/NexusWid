#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

GRADLEW="$ROOT_DIR/gradlew"
if [[ ! -x "$GRADLEW" ]]; then
    echo "错误: 未找到可执行的 gradlew: $GRADLEW"
    exit 1
fi

APK_PATH="$ROOT_DIR/androidApp/build/outputs/apk/release/androidApp-release.apk"
BUILD_MODE="release_incremental"
BUILD_MODE_LABEL="增量 Release"
BUILD_TASK=":androidApp:assembleRelease"

timestamp() { date "+%H:%M:%S"; }
log_info() { echo "[$(timestamp)] [INFO] $*"; }
log_warn() { echo "[$(timestamp)] [WARN] $*"; }
log_error() { echo "[$(timestamp)] [ERROR] $*" >&2; }
die() { log_error "$*"; exit 1; }

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
log_info "Gradle任务: ${BUILD_TASK}"
if ! "$GRADLEW" "$BUILD_TASK" -x lint --configure-on-demand --parallel --daemon; then
    log_warn "增量构建失败，回退标准构建: ${BUILD_TASK}"
    "$GRADLEW" "$BUILD_TASK"
fi

if [[ -f "$APK_PATH" ]]; then
    APK_SIZE_H="$(du -h "$APK_PATH" | cut -f1)"
    APK_SIZE_B="$(stat -c%s "$APK_PATH")"
    log_info "APK: $APK_SIZE_H (${APK_SIZE_B} bytes)"
    log_info "位置: $APK_PATH"
else
    die "APK 未找到: $APK_PATH"
fi

if command -v unzip >/dev/null 2>&1; then
    log_info "APK 内文件体积 Top 15"
    unzip -l "$APK_PATH" \
        | awk 'NR > 3 && $4 != "" {print $1 "\t" $4}' \
        | sort -nr \
        | head -n 15
fi

log_info "快速发布构建完成"
