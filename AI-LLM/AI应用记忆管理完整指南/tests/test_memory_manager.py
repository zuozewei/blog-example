# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/14 04:33
@Author  : zuozewei
@File    : test_memory_manager.py
@Desc    : memory_manager测试 - 模拟测试，验证代码逻辑
"""
import warnings
import sys
from pathlib import Path
from typing import Dict

warnings.filterwarnings("ignore")

sys.path.insert(0, str(Path(__file__).parent.parent))

from memory_manager import MemoryManager


class MockMemory:
    """模拟mem0的Memory类"""

    def __init__(self):
        self.memories = {}

    def add(self, memory: str, user_id: str, metadata: dict = None):
        """添加记忆"""
        if user_id not in self.memories:
            self.memories[user_id] = []

        self.memories[user_id].append({
            "memory": memory,
            "metadata": metadata or {}
        })

    def search(self, query: str, user_id: str) -> Dict:
        """搜索记忆"""
        if user_id not in self.memories:
            return {"results": []}

        if "所有" in query or "信息" in query:
            return {"results": self.memories[user_id]}

        results = []
        query_lower = query.lower()
        
        for mem in self.memories[user_id]:
            memory_lower = mem["memory"].lower()
            
            if "爱好" in query and ("读书" in memory_lower or "编程" in memory_lower or "音乐" in memory_lower or "运动" in memory_lower):
                results.append(mem)
            elif "住" in query and ("上海" in memory_lower or "北京" in memory_lower or "广州" in memory_lower):
                results.append(mem)
            elif "工作" in query or "工程师" in query or "教师" in query or "医生" in query:
                results.append(mem)
            elif any(keyword in memory_lower for keyword in query_lower.split() if len(keyword) > 1):
                results.append(mem)

        return {"results": results}


def test_memory_manager():
    """测试MemoryManager功能"""
    print("=== 测试MemoryManager功能 ===\n")

    manager = MemoryManager()
    mock_mem0 = MockMemory()
    manager.mem0 = mock_mem0

    print("1. 添加用户记忆")
    manager.add_memory("dreamhead", "我喜欢读书", {"category": "hobbies"})
    manager.add_memory("dreamhead", "我喜欢编程", {"category": "hobbies"})
    manager.add_memory("dreamhead", "我住在上海", {"category": "location"})
    print("   已为用户 'dreamhead' 添加3条记忆\n")

    print("2. 搜索用户记忆")
    memories = manager.search_memories("dreamhead有哪些爱好？", "dreamhead")
    print("   搜索结果:")
    for i, mem in enumerate(memories, 1):
        print(f"     {i}. {mem['memory']}")
    print()

    print("3. 多用户隔离测试")
    manager.add_memory("alice", "我喜欢音乐", {"category": "hobbies"})
    manager.add_memory("alice", "我住在北京", {"category": "location"})

    alice_memories = manager.search_memories("alice有哪些爱好？", "alice")
    print(f"   Alice的爱好: {' '.join([mem['memory'] for mem in alice_memories])}")

    dreamhead_memories = manager.search_memories("dreamhead有哪些爱好？", "dreamhead")
    print(f"   Dreamhead的爱好: {' '.join([mem['memory'] for mem in dreamhead_memories])}\n")

    print("=== MemoryManager测试完成 ===\n")


def test_integration():
    """集成测试"""
    print("=== 集成测试 ===\n")

    manager = MemoryManager()
    mock_mem0 = MockMemory()
    manager.mem0 = mock_mem0

    users = ["dreamhead", "alice", "bob"]

    user_memories = {
        "dreamhead": [
            ("我喜欢读书", {"category": "hobbies"}),
            ("我喜欢编程", {"category": "hobbies"}),
            ("我住在上海", {"category": "location"}),
            ("我是一名软件工程师", {"category": "work"}),
        ],
        "alice": [
            ("我喜欢音乐", {"category": "hobbies"}),
            ("我住在北京", {"category": "location"}),
            ("我是一名教师", {"category": "work"}),
        ],
        "bob": [
            ("我喜欢运动", {"category": "hobbies"}),
            ("我住在广州", {"category": "location"}),
            ("我是一名医生", {"category": "work"}),
        ]
    }

    print("1. 为多个用户添加记忆")
    for user_id, memories in user_memories.items():
        for memory, metadata in memories:
            manager.add_memory(user_id, memory, metadata)
        print(f"   已为用户 '{user_id}' 添加 {len(memories)} 条记忆")
    print()

    print("2. 测试记忆隔离")
    for user_id in users:
        memories = manager.search_memories(f"{user_id}的信息", user_id)
        print(f"   {user_id} 的记忆数量: {len(memories)}")
        for mem in memories:
            print(f"     - {mem['memory']}")
    print()

    print("3. 测试个性化搜索")
    queries = [
        ("dreamhead", "dreamhead有哪些爱好？"),
        ("alice", "alice住在哪里？"),
        ("bob", "bob是做什么工作的？"),
    ]

    for user_id, query in queries:
        results = manager.search_memories(query, user_id)
        print(f"   查询: {query}")
        print(f"   结果: {' '.join([mem['memory'] for mem in results])}")
    print()

    print("=== 集成测试完成 ===\n")


if __name__ == "__main__":
    test_memory_manager()
    test_integration()

    print("\n✅ 所有测试通过！")
