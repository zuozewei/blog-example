# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/14 04:33
@Author  : zuozewei
@File    : memory_manager.py
@Desc    : mem0长期记忆管理 - 基于向量存储的持久化记忆系统，支持多用户隔离和个性化回复
"""
import os
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from memory_config import create_memory


class MemoryManager:
    """记忆管理器类"""

    def __init__(self):
        """初始化记忆管理器"""
        self.mem0 = create_memory()
        api_base = os.getenv("OPENAI_BASE_URL") or os.getenv("OPENAI_API_BASE")
        self.llm = ChatOpenAI(
            model=os.getenv("AI_MODEL", "deepseek-v3.2"),
            api_key=os.getenv("OPENAI_API_KEY"),
            base_url=api_base,
            temperature=0.7
        )
        self.prompt = ChatPromptTemplate.from_messages([
            ("system", """"你现在扮演孔子的角色，尽量按照孔子的风格回复，不要出现'子曰'。
            利用提供的上下文进行个性化回复，并记住用户的偏好和以往的交互行为。
            上下文：{context}"""),
            ("user", "{input}")
        ])
        self.chain = self.prompt | self.llm

    def retrieve_context(self, query: str, user_id: str) -> str:
        """
        检索用户的上下文记忆
        :param query: 查询内容
        :param user_id: 用户ID
        :return: 上下文记忆
        """
        memories = self.mem0.search(query, user_id=user_id)
        return ' '.join([mem["memory"] for mem in memories['results']])

    def save_interaction(self, user_id: str, user_input: str, assistant_response: str):
        """
        保存用户交互记录到记忆中
        :param user_id: 用户ID
        :param user_input: 用户输入
        :param assistant_response: 助手回复
        """
        interaction = [
            {
                "role": "user",
                "content": user_input
            },
            {
                "role": "assistant",
                "content": assistant_response
            }
        ]
        self.mem0.add(interaction, user_id=user_id)

    def add_memory(self, user_id: str, memory: str, metadata: dict = None):
        """
        添加用户记忆
        :param user_id: 用户ID
        :param memory: 记忆内容
        :param metadata: 元数据（可选）
        """
        if metadata is None:
            metadata = {}
        self.mem0.add(memory, user_id=user_id, metadata=metadata)

    def search_memories(self, query: str, user_id: str) -> list:
        """
        搜索用户记忆
        :param query: 查询内容
        :param user_id: 用户ID
        :return: 记忆列表
        """
        memories = self.mem0.search(query, user_id=user_id)
        return memories['results']

    def invoke(self, user_input: str, user_id: str) -> str:
        """
        调用AI助手，自动检索上下文并保存交互
        :param user_input: 用户输入
        :param user_id: 用户ID
        :return: AI回复
        """
        context = self.retrieve_context(user_input, user_id)
        response = self.chain.invoke({
            "context": context,
            "input": user_input
        })

        content = response.content
        self.save_interaction(user_id, user_input, content)
        return content
