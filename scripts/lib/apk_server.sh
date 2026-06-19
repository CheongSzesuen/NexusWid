#!/usr/bin/env bash
# APK 本地下载服务公共逻辑
# 调用方需提供 log_info / log_warn / log_error 函数

# 启动 APK HTTP 服务（端口 9190）
# 用法: start_apk_server_common <APK 文件绝对路径>
# 行为:
#   1. 强制释放 9190 端口（kill -> 等待 -> kill -9 兜底）
#   2. 启动 python http.server 暴露 APK 文件所在目录
#   3. 自检 APK 文件可访问，失败时打印日志路径并退出非零
start_apk_server_common() {
    local apk_path="$1"
    local port=9190
    local apk_file
    local apk_dir
    apk_file="$(basename "$apk_path")"
    apk_dir="$(dirname "$apk_path")"

    if [[ ! -f "$apk_path" ]]; then
        log_error "APK 文件不存在: $apk_path"
        return 1
    fi

    # 1) 释放端口：先 SIGTERM，最多等 3 秒，再 SIGKILL 兜底
    local pids
    pids="$(lsof -ti:"$port" 2>/dev/null || true)"
    if [[ -n "$pids" ]]; then
        log_info "释放端口 $port (旧进程: $pids)"
        # shellcheck disable=SC2086
        kill $pids 2>/dev/null || true
        local waited=0
        while [[ $waited -lt 30 ]]; do
            sleep 0.1
            waited=$((waited + 1))
            pids="$(lsof -ti:"$port" 2>/dev/null || true)"
            [[ -z "$pids" ]] && break
        done
        if [[ -n "$pids" ]]; then
            log_warn "端口 $port 未释放，强制 kill -9: $pids"
            # shellcheck disable=SC2086
            kill -9 $pids 2>/dev/null || true
            sleep 0.3
        fi
    fi

    # 2) 启动新服务，日志写到 /tmp 方便排查
    local log_file="/tmp/apk-server-${port}.log"
    : > "$log_file"
    nohup python3 -m http.server "$port" --directory "$apk_dir" >"$log_file" 2>&1 &
    local server_pid=$!

    # 3) 自检：等待端口 LISTEN + curl 拉到正确 APK
    local attempt=0
    while [[ $attempt -lt 20 ]]; do
        sleep 0.1
        attempt=$((attempt + 1))
        if ! kill -0 "$server_pid" 2>/dev/null; then
            log_error "APK 服务进程已退出，详见: $log_file"
            tail -n 5 "$log_file" >&2 || true
            return 1
        fi
        local code
        code="$(curl -s -o /dev/null -w '%{http_code}' --max-time 2 \
            "http://127.0.0.1:${port}/${apk_file}" 2>/dev/null || echo 000)"
        if [[ "$code" == "200" ]]; then
            log_info "APK 服务已启动 (端口 $port, 目录 $apk_dir)"
            return 0
        fi
    done

    log_error "APK 服务自检失败：http://127.0.0.1:${port}/${apk_file} 不可访问"
    log_error "服务日志: $log_file"
    tail -n 10 "$log_file" >&2 || true
    return 1
}
