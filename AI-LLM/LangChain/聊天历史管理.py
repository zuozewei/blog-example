# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/14 04:33
@Author  : zuozewei
@File    : 聊天历史管理.py 
@Desc    : 基于文件持久化+内存缓存，支持多会话隔离，实现对话历史的保存、读取和管理
"""
from langchain_community.chat_message_histories import FileChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.messages import HumanMessage, AIMessage

# 使用内存保存聊天历史
mem_store = {}


def get_session_history(session_id: str) -> BaseChatMessageHistory:
    """
    获取会话历史
    :param session_id: 会话ID
    :return: 会话历史
    """

    # 从内存中获取聊天历史
    if session_id not in mem_store:
        # 使用文件保存聊天历史
        mem_store[session_id] = FileChatMessageHistory(file_path=f"./history_{session_id}.json")

    return mem_store[session_id]


if __name__ == "__main__":
    print("=== 测试会话历史功能 ===\n")
    
    # 测试1: 创建新会话
    print("1. 创建新会话 'test_session'")
    history = get_session_history("test_session")
    print(f"   会话历史对象: {history}")
    print(f"   当前消息数量: {len(history.messages)}\n")
    
    # 测试2: 添加消息
    print("2. 添加消息到会话")
    history.add_message(HumanMessage(content="你好，我是用户"))
    history.add_message(AIMessage(content="你好，我是AI助手"))
    print(f"   添加2条消息后，当前消息数量: {len(history.messages)}")
    print(f"   消息列表:")
    for i, msg in enumerate(history.messages, 1):
        print(f"     {i}. [{msg.type}]: {msg.content}")
    print()
    
    # 测试3: 获取已有会话
    print("3. 获取已有会话 'test_session'")
    history2 = get_session_history("test_session")
    print(f"   当前消息数量: {len(history2.messages)}")
    print(f"   消息列表:")
    for i, msg in enumerate(history2.messages, 1):
        print(f"     {i}. [{msg.type}]: {msg.content}")
    print()
    
    # 测试4: 创建另一个会话
    print("4. 创建另一个会话 'test_session2'")
    history3 = get_session_history("test_session2")
    history3.add_message(HumanMessage(content="这是第二个会话"))
    print(f"   会话 'test_session2' 的消息数量: {len(history3.messages)}")
    print(f"   会话 'test_session' 的消息数量: {len(history.messages)}")
    print()
    
    # 测试5: 清空消息
    print("5. 清空 'test_session2' 的消息")
    history3.clear()
    print(f"   清空后消息数量: {len(history3.messages)}")
    
    print("\n=== 测试完成 ===\n")
    
    # 测试6: 读取现有的历史文件 'zsa'
    print("=== 读取现有历史文件 'zsa' ===\n")
    zsa_history = get_session_history("zsa")
    print(f"会话 'zsa' 的消息数量: {len(zsa_history.messages)}")
    print(f"\n消息列表:")
    for i, msg in enumerate(zsa_history.messages, 1):
        print(f"{i}. [{msg.type}]: {msg.content}")
    print("\n=== 读取完成 ===")
