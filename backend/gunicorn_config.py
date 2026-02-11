# gunicorn_config.py

# 绑定监听地址 (0.0.0.0 表示允许公网访问)
bind = "127.0.0.1:5000"

# 进程数 (Worker Processes)
# 核心逻辑：你的机器只有 1GiB 内存。
# Python 进程启动至少需要 30-50MB，随着请求可能会涨到 100MB。
# 操作系统和其他服务大概占用 300-400MB。
# 剩下的空间大概能跑 4-5 个 Worker，但为了安全，我们只开 2 个。
# 2 个 Worker 足够应付 10 个人同时使用了。
workers = 2

# 线程数 (Threads per Worker)
# 处理并发请求
threads = 2

# 超时时间
# AI 请求可能很慢，所以这里要设置得长一点，防止 Gunicorn 杀掉正在等待 AI 的进程
timeout = 120

# 日志配置
accesslog = "-"  # 输出到控制台
errorlog = "-"
loglevel = "info"
