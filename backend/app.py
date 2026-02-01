from flask import Flask, request, jsonify
import time
import os
import json
import sqlite3
import hashlib
from dotenv import load_dotenv
from openai import OpenAI

# 加载 .env 文件
load_dotenv()

app = Flask(__name__)

# 配置 DeepSeek / Moonshot 客户端
client = OpenAI(
    api_key=os.getenv("LLM_API_KEY"),
    base_url=os.getenv("LLM_BASE_URL", "https://api.deepseek.com")
)

# 数据库文件路径
DB_PATH = 'cache.db'

def init_db():
    """初始化 SQLite 数据库"""
    with sqlite3.connect(DB_PATH) as conn:
        cursor = conn.cursor()
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS idiom_cache (
                original_text_hash TEXT PRIMARY KEY,
                original_text TEXT,
                response_json TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        conn.commit()

# 初始化数据库
init_db()

def get_cache(text):
    """从缓存中获取结果"""
    text_hash = hashlib.md5(text.encode('utf-8')).hexdigest()
    try:
        with sqlite3.connect(DB_PATH) as conn:
            cursor = conn.cursor()
            cursor.execute('SELECT response_json FROM idiom_cache WHERE original_text_hash = ?', (text_hash,))
            row = cursor.fetchone()
            if row:
                return json.loads(row[0])
    except Exception as e:
        print(f"读取缓存出错: {e}")
    return None

def save_cache(text, response_data):
    """保存结果到缓存"""
    text_hash = hashlib.md5(text.encode('utf-8')).hexdigest()
    try:
        with sqlite3.connect(DB_PATH) as conn:
            cursor = conn.cursor()
            json_str = json.dumps(response_data, ensure_ascii=False)
            cursor.execute('''
                INSERT OR REPLACE INTO idiom_cache (original_text_hash, original_text, response_json)
                VALUES (?, ?, ?)
            ''', (text_hash, text, json_str))
            conn.commit()
    except Exception as e:
        print(f"写入缓存出错: {e}")

SYSTEM_PROMPT = """
你是一位博古通今的国学大师。用户的输入通常是粗俗的互联网流行语或网络梗。
你的任务是将这些话转化为含义相近、优雅、精炼的四字成语或传统俗语。

返回格式必须是纯 JSON，不要包含 Markdown 格式标记（如 ```json ... ```）：
{
  "original": "用户输入的词",
  "idiom": "推荐成语（最贴切的一个）",
  "alternatives": ["备选成语1", "备选成语2"],
  "explanation": "用通俗语言解释为什么用这个成语替代，包含成语出处或典故（50字以内）。",
  "tone_score": "从1-10给原句的文雅程度打分"
}

注意：解释部分(explanation)内部如果包含引号，请使用单引号或转义，确保 JSON 格式合法。
"""

@app.route('/polish', methods=['POST'])
def polish():
    data = request.json
    text = data.get('text', '')
    print(f"收到请求: {text}")

    if not text:
         return jsonify({"error": "No text provided"}), 400

    # 1. 先查缓存
    cached_result = get_cache(text)
    if cached_result:
        print(f"命中缓存: {text}")
        # 为了演示效果，稍微睡一下，不然太快了用户以为没反应
        # 实际生产中可以去掉
        time.sleep(0.5)
        return jsonify(cached_result)

    # 如果没有配置 Key，还是回退到 Mock 数据 (方便测试)
    if not os.getenv("LLM_API_KEY"):
        print("未检测到 LLM_API_KEY，使用 Mock 数据")
        time.sleep(1)
        # ... (保留原有的 mock 逻辑作为 fallback)
        return jsonify({
            "original": text,
            "idiom": "未配置API",
            "alternatives": ["请配置Key", "并在.env中"],
            "explanation": "后端未检测到 LLM_API_KEY，请在 backend/.env 文件中配置。",
            "tone_score": "0"
        })

    try:
        completion = client.chat.completions.create(
            model=os.getenv("LLM_MODEL", "deepseek-chat"),
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": text}
            ],
            temperature=1.0
        )
        
        result_content = completion.choices[0].message.content
        print(f"LLM 返回: {result_content}")
        
        # 清理可能存在的 Markdown 标记
        cleaned_content = result_content.replace("```json", "").replace("```", "").strip()
        
        # 简单的 JSON 修复尝试 (针对双引号嵌套问题)
        # 如果 LLM 在 JSON 字符串值里用了双引号但没转义，json.loads 会挂
        # 这里做一个非常粗糙的替换，把值里面的 " 换成 ' (仅作保底，不完美)
        # 更好的方式是让 Prompt 约束，或者用更强的 JSON 修复库
        
        try:
            result_json = json.loads(cleaned_content)
        except json.JSONDecodeError:
            print("JSON 解析失败，尝试手动修复...")
            # 尝试修复：有时候模型返回的 JSON 里 explanation 字段包含未转义的双引号
            # 这里简单处理：如果解析失败，直接返回原始文本作为 fallback，避免 App 报错
            return jsonify({
                "original": text,
                "idiom": "解析异常",
                "alternatives": ["格式错误"],
                "explanation": f"AI 返回了非标准 JSON，原始内容：{cleaned_content[:50]}...",
                "tone_score": "0"
            })

        # 存入缓存
        save_cache(text, result_json)

        return jsonify(result_json)

    except Exception as e:
        print(f"调用 LLM 出错: {e}")
        return jsonify({
            "original": text,
            "idiom": "调用出错",
            "alternatives": ["网络繁忙", "稍后再试"],
            "explanation": f"后端报错: {str(e)[:50]}",
            "tone_score": "0"
        })

if __name__ == '__main__':
    # host='0.0.0.0' 允许局域网访问
    app.run(host='0.0.0.0', port=5000, debug=True)

