# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/14 04:33
@Author  : zuozewei
@File    : example.py
@Desc    : 使用示例 - 展示如何使用MemoryManager和ChatHistoryManager
"""
from memory_manager import MemoryManager
from chat_history_manager import ChatHistoryManager


def main():
    """主函数 - 演示使用示例"""
    print("=== mem0长期记忆系统使用示例 ===\n")

    memory_manager = MemoryManager()
    chat_history_manager = ChatHistoryManager()

    print("1. 添加用户记忆")
    memory_manager.add_memory("dreamhead", "我喜欢读书", {"category": "hobbies"})
    memory_manager.add_memory("dreamhead", "我喜欢编程", {"category": "hobbies"})
    memory_manager.add_memory("dreamhead", "我住在上海", {"category": "location"})
    print("   已为用户 'dreamhead' 添加3条记忆\n")

    print("2. 搜索用户记忆")
    memories = memory_manager.search_memories("dreamhead有哪些爱好？", "dreamhead")
    print("   搜索结果:")
    for i, mem in enumerate(memories, 1):
        print(f"     {i}. {mem['memory']}")
    print()

    print("3. AI对话测试（孔子风格）")
    user_input = "你好，我是新来的"
    response = memory_manager.invoke(user_input, "dreamhead")
    print(f"   用户: {user_input}")
    print(f"   AI: {response}\n")

    print("4. 基于记忆的个性化回复")
    user_input = "你了解我吗？"
    response = memory_manager.invoke(user_input, "dreamhead")
    print(f"   用户: {user_input}")
    print(f"   AI: {response}\n")

    print("5. 多用户隔离测试")
    memory_manager.add_memory("alice", "我喜欢音乐", {"category": "hobbies"})
    memory_manager.add_memory("alice", "我住在北京", {"category": "location"})

    alice_memories = memory_manager.search_memories("alice有哪些爱好？", "alice")
    print(f"   Alice的爱好: {' '.join([mem['memory'] for mem in alice_memories])}")

    dreamhead_memories = memory_manager.search_memories("dreamhead有哪些爱好？", "dreamhead")
    print(f"   Dreamhead的爱好: {' '.join([mem['memory'] for mem in dreamhead_memories])}\n")

    print("6. 交互式对话测试（输入 'exit' 退出）")
    print("   提示: 你可以尝试问AI关于你的信息\n")

    user_id = "dreamhead"
    while True:
        try:
            user_input = input(f"   [{user_id}] > ")
            if user_input.lower() == 'exit':
                print("   退出对话")
                break

            response = memory_manager.invoke(user_input, user_id)
            print(f"   AI: {response}\n")
        except KeyboardInterrupt:
            print("\n   退出对话")
            break

    print("\n=== 示例完成 ===")


if __name__ == "__main__":
    main()
