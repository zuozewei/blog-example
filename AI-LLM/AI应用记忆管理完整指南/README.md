# AI应用记忆管理完整指南

## 项目概述

这是一个基于mem0框架实现的长期记忆系统，支持多用户隔离、向量存储和个性化AI回复。参考了`chat_history.py`的实现模式，实现了会话隔离和文件持久化。

## 文件说明

### 1. config.py - 配置文件

**功能：**
- 配置mem0的LLM、embedder和vector_store
- 创建Memory实例
- 提供get_memory()函数获取Memory实例
- 包含完整的测试代码

**主要配置：**
```python
config = {
    "version": "v1.1",
    "llm": {
        "provider": "openai",
        "config": {
            "model": "gpt-4o-mini",
            "temperature": 0,
            "max_tokens": 1500,
        }
    },
    "embedder": {
        "provider": "openai",
        "config": {
            "model": "text-embedding-ada-002"
        }
    },
    "vector_store": {
        "provider": "chroma",
        "config": {
            "collection_name": "mem0db",
            "path": "mem0db",
        }
    },
    "history_db_path": "history.db",
}
```

**测试功能：**
- 添加记忆
- 搜索记忆
- 多用户隔离测试

### 2. memory_manager.py - 记忆管理

**功能：**
- 检索用户上下文记忆
- 保存用户交互记录
- 添加用户记忆
- 搜索用户记忆
- 调用AI助手（自动检索上下文并保存交互）

**主要函数：**

```python
def retrieve_context(query: str, user_id: str) -> str:
    """检索用户的上下文记忆"""

def save_interaction(user_id: str, user_input: str, assistant_response: str):
    """保存用户交互记录到记忆中"""

def add_memory(user_id: str, memory: str, metadata: dict = None):
    """添加用户记忆"""

def search_memories(query: str, user_id: str) -> list:
    """搜索用户记忆"""

def invoke(user_input: str, user_id: str) -> str:
    """调用AI助手，自动检索上下文并保存交互"""
```

**测试功能：**
- 添加用户记忆
- 搜索用户记忆
- AI对话测试（孔子风格）
- 基于记忆的个性化回复
- 多用户隔离测试
- 交互式对话

### 3. chat_history_manager.py - 会话历史管理

**功能：**
- 管理用户会话历史
- 支持会话隔离
- 提供会话历史的持久化存储

### 4. example.py - 使用示例

**功能：**
- 展示如何使用MemoryManager和ChatHistoryManager
- 提供完整的使用流程示例
- 包含多用户隔离、记忆搜索和AI对话等功能演示

### 5. test_mem.py - 模拟测试

**功能：**
- 模拟mem0的Memory类
- 测试config.py的功能
- 测试memory_manager.py的功能
- 集成测试

**使用场景：**
- 在没有安装mem0库的情况下验证代码逻辑
- 测试多用户隔离
- 测试记忆搜索和检索

## 安装依赖

### 方法一：使用requirements.txt

```bash
pip install -r requirements.txt
```

### 方法二：手动安装

```bash
pip install mem0 langchain-openai langchain-core langchain-community python-dotenv chromadb
```

## 使用方法

### 1. 基础使用（config.py）

```python
from config import get_memory

# 获取Memory实例
m = get_memory()

# 添加记忆
m.add("我喜欢读书", user_id="dreamhead", metadata={"category": "hobbies"})
m.add("我喜欢编程", user_id="dreamhead", metadata={"category": "hobbies"})

# 搜索记忆
related_memories = m.search(query="dreamhead有哪些爱好？", user_id="dreamhead")
memories = ' '.join([mem["memory"] for mem in related_memories['results']])
print(memories)
```

### 2. 高级使用（memory_manager.py）

```python
from memory_manager import add_memory, search_memories, invoke

# 添加记忆
add_memory("dreamhead", "我喜欢读书", {"category": "hobbies"})
add_memory("dreamhead", "我喜欢编程", {"category": "hobbies"})

# 搜索记忆
memories = search_memories("dreamhead有哪些爱好？", "dreamhead")
for mem in memories:
    print(mem['memory'])

# AI对话
response = invoke("你了解我吗？", "dreamhead")
print(response)
```

### 3. 交互式对话

```python
from memory_manager import invoke

user_id = "dreamhead"
while True:
    user_input = input(f"[{user_id}] > ")
    if user_input.lower() == 'exit':
        break

    response = invoke(user_input, user_id)
    print(f"AI: {response}")
```

## 运行测试

### 1. 运行config.py测试

```bash
python config.py
```

**预期输出：**
```
=== 测试mem0基础功能 ===

1. 添加记忆
   已添加: '我喜欢读书' 和 '我喜欢编程'

2. 搜索记忆
   搜索结果: 我喜欢读书 我喜欢编程

3. 添加更多记忆
   已添加: '我住在上海' 和 '我是一名软件工程师'

4. 搜索所有记忆
   所有记忆:
     1. 我喜欢读书
     2. 我喜欢编程
     3. 我住在上海
     4. 我是一名软件工程师

5. 为另一个用户添加记忆
   已为用户 'alice' 添加记忆

6. 搜索不同用户的记忆
   Alice的爱好: 我喜欢音乐
   Dreamhead的爱好: 我喜欢读书 我喜欢编程

=== 测试完成 ===
```

### 2. 运行example.py示例

```bash
python example.py
```

**预期输出：**
```
=== mem0长期记忆系统使用示例 ===

1. 添加用户记忆
   已为用户 'dreamhead' 添加3条记忆

2. 搜索用户记忆
   搜索结果:
     1. 喜欢编程
     2. 喜欢读书
     3. 住在上海

3. AI对话测试（孔子风格）
   用户: 你好，我是新来的
   AI: （微笑）有朋自远方来，不亦乐乎。你来自何处？

4. 基于记忆的个性化回复
   用户: 你了解我吗？
   AI: （微笑）吾闻汝言，汝乃新来之客，好编程之术，喜读书之道，居于沪上。然人之性，非止于表，愿闻汝志。

5. 多用户隔离测试
   Alice的爱好: 喜欢音乐 住在北京
   Dreamhead的爱好: 喜欢编程 喜欢读书 你好，我是新来的 住在上海

6. 交互式对话测试（输入 'exit' 退出）
   提示: 你可以尝试问AI关于你的信息

   [dreamhead] > 你好，我是新来的
   AI: 善哉！汝自远方而来，当以礼相待。上海乃繁华之地，然吾闻之，繁华中亦有宁静之处。汝既好读书与编程，此二者皆需静心钻研。昔者吾尝言："学而时习之，不亦说乎？"愿汝在上海能得其所，以学问修身，以技艺立世。

   [dreamhead] > exit
   退出对话

=== 示例完成 ===
```

### 3. 运行模拟测试（无需安装mem0）

```bash
python test_mem.py
```

**预期输出：**
```
=== 测试config.py功能 ===
...
=== config.py测试完成 ===

=== 测试mem.py功能 ===
...
=== mem.py测试完成 ===

=== 集成测试 ===
...
=== 集成测试完成 ===

✅ 所有测试通过！
```

## 核心特性

### 1. 多用户隔离

每个用户的记忆完全隔离，互不影响：

```python
from memory_manager import add_memory, search_memories

# 为dreamhead添加记忆
add_memory("dreamhead", "我喜欢读书")

# 为alice添加记忆
add_memory("alice", "我喜欢音乐")

# 搜索dreamhead的记忆
memories = search_memories("爱好", "dreamhead")
# 只返回dreamhead的记忆

# 搜索alice的记忆
memories = search_memories("爱好", "alice")
# 只返回alice的记忆
```

### 2. 向量存储

使用ChromaDB作为向量存储，支持语义搜索：

```python
config = {
    "vector_store": {
        "provider": "chroma",
        "config": {
            "collection_name": "mem0db",
            "path": "mem0db",
        }
    }
}
```

### 3. 个性化AI回复

基于用户的记忆和历史交互，提供个性化回复：

```python
from memory_manager import invoke

# AI会根据用户的记忆调整回复风格和内容
response = invoke("你了解我吗？", "dreamhead")
# AI会说：我知道你喜欢读书和编程...
```

### 4. 持久化存储

所有记忆都持久化存储到ChromaDB和SQLite数据库：

```python
config = {
    "vector_store": {
        "path": "mem0db",  # ChromaDB数据目录
    },
    "history_db_path": "history.db",  # SQLite数据库
}
```

## 与chat_history.py的对比

| 特性 | chat_history.py | mem0长期记忆 |
|------|----------------|--------------|
| 存储方式 | 文件（JSON） | 向量数据库（ChromaDB） |
| 搜索能力 | 精确匹配 | 语义搜索 |
| 记忆类型 | 对话历史 | 结构化记忆+对话历史 |
| 隔离方式 | session_id | user_id |
| 持久化 | 文件持久化 | 数据库持久化 |

## 注意事项

1. **API密钥**：需要配置OpenAI API密钥
2. **成本**：每次调用都会消耗tokens，建议设置合理的限制
3. **隐私**：记忆数据存储在本地，但会调用OpenAI API
4. **性能**：向量搜索比精确匹配慢，但更智能

## 扩展建议

1. **添加记忆分类**：使用metadata对记忆进行分类管理
2. **记忆过期**：实现记忆的自动过期和清理机制
3. **记忆优先级**：为重要记忆设置优先级
4. **记忆总结**：定期总结和压缩旧记忆
5. **多模态记忆**：支持图片、音频等多模态记忆

## 相关文档

- [mem0官方文档](https://docs.mem0.ai/)
- [ChromaDB文档](https://docs.trychroma.com/)
- [LangChain文档](https://python.langchain.com/)
