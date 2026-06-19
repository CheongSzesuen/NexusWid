#!/usr/bin/env bash
# APK 本地下载服务公共逻辑
# 调用方需提供 log_info / log_warn / log_error 函数

# 启动 APK HTTP 服务（端口 9190）
# 用法: start_apk_server_common <APK 文件绝对路径>
# 行为:
#   1. 强制释放 9190 端口（kill -> 等待 -> kill -9 兜底）
#   2. 启动 python http.server 暴露 APK 文件所在目录
#   3. 自检 APK 文件可访问，失败时打印日志路径并退出非零
# 将 Gradle 默认输出的 APK 重命名为项目唯一名称
# 用法: rename_apk_common <期望路径> <Gradle原始文件名>
# 示例: rename_apk_common "$APK_PATH" "androidApp-debug.apk"
rename_apk_common() {
    local target_path="$1"
    local src_name="$2"
    local dir; dir="$(dirname "$target_path")"
    local gradle_output="$dir/$src_name"
    if [[ -f "$gradle_output" ]] && [[ "$gradle_output" != "$target_path" ]]; then
        mv -f "$gradle_output" "$target_path"
    fi
}

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

    # 2) 启动自定义 HTTP 服务（带 Cache-Control: no-cache 防止 Cloudflare 缓存）
    local log_file="/tmp/apk-server-${port}.log"
    : > "$log_file"
    nohup python3 -c "
import http.server, os
os.chdir('$apk_dir')
class H(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        super().end_headers()
http.server.HTTPServer(('0.0.0.0', $port), H).serve_forever()
" >"$log_file" 2>&1 &
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
