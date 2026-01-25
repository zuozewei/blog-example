# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/14 04:33
@Author  : zuozewei
@File    : chat_history_manager.py
@Desc    : 聊天历史管理 - 基于文件持久化+内存缓存，支持多会话隔离，实现对话历史的保存、读取和管理
"""
from langchain_community.chat_message_histories import FileChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory


class ChatHistoryManager:
    """聊天历史管理器类"""

    def __init__(self):
        """初始化聊天历史管理器"""
        self.mem_store = {}

    def get_session_history(self, session_id: str) -> BaseChatMessageHistory:
        """
        获取会话历史
        :param session_id: 会话ID
        :return: 会话历史
        """
        if session_id not in self.mem_store:
            self.mem_store[session_id] = FileChatMessageHistory(
                file_path=f"./history_{session_id}.json"
            )

        return self.mem_store[session_id]
