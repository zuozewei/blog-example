# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/14 04:33
@Author  : zuozewei
@File    : test_chat_history_manager.py
@Desc    : chat_history_manager测试
"""
import sys
from pathlib import Path
from langchain_core.messages import HumanMessage, AIMessage

sys.path.insert(0, str(Path(__file__).parent.parent))

from chat_history_manager import ChatHistoryManager


def test_chat_history_manager():
    """测试ChatHistoryManager功能"""
    print("=== 测试ChatHistoryManager功能 ===\n")

    manager = ChatHistoryManager()

    print("1. 创建新会话 'test_session'")
    history = manager.get_session_history("test_session")
    print(f"   会话历史对象: {history}")
    print(f"   当前消息数量: {len(history.messages)}\n")

    print("2. 添加消息到会话")
    history.add_message(HumanMessage(content="你好，我是用户"))
    history.add_message(AIMessage(content="你好，我是AI助手"))
    print(f"   添加2条消息后，当前消息数量: {len(history.messages)}")
    print(f"   消息列表:")
    for i, msg in enumerate(history.messages, 1):
        print(f"     {i}. [{msg.type}]: {msg.content}")
    print()

    print("3. 获取已有会话 'test_session'")
    history2 = manager.get_session_history("test_session")
    print(f"   当前消息数量: {len(history2.messages)}")
    print(f"   消息列表:")
    for i, msg in enumerate(history2.messages, 1):
        print(f"     {i}. [{msg.type}]: {msg.content}")
    print()

    print("4. 创建另一个会话 'test_session2'")
    history3 = manager.get_session_history("test_session2")
    history3.add_message(HumanMessage(content="这是第二个会话"))
    print(f"   会话 'test_session2' 的消息数量: {len(history3.messages)}")
    print(f"   会话 'test_session' 的消息数量: {len(history.messages)}")
    print()

    print("5. 清空 'test_session2' 的消息")
    history3.clear()
    print(f"   清空后消息数量: {len(history3.messages)}")

    print("\n=== ChatHistoryManager测试完成 ===\n")


if __name__ == "__main__":
    test_chat_history_manager()

    print("\n✅ 所有测试通过！")
