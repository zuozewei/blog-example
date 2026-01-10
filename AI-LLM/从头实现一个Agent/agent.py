# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/11 17:49
@Author  : zuozewei
@File    : agent.py 
@Desc    : Agent推理框架
"""
import os

import dotenv
from openai import OpenAI

# 初始化DeepSeek客户端
dotenv.load_dotenv()
client = OpenAI(
    api_key=os.getenv("OPENAI_API_KEY"),
    base_url=os.getenv("OPENAI_API_BASE")
)

DEFAULT_MODEL = os.getenv("AI_MODEL", "deepseek-chat")


class Agent:
    """Agent推理框架"""

    def __init__(self, system_prompt=""):
        """初始化方法"""

        # 初始化消息列表
        self._messages = []
        # 构造SystemPrompt
        if system_prompt is not None and system_prompt != "":
            self._messages.append({"role": "system", "content": system_prompt})

    def invoke(self, query: str) -> str:
        """调用Agent"""

        # 在Prompt中添加消息列表
        self._messages.append({"role": "user", "content": query})
        # 执行推理,获取结果
        result = self.exec()
        # 保存聊天历史
        self._messages.append({"role": "assistant", "content": result})

        # 返回结果
        return result

    def exec(self) -> str:
        """执行推理,返回结果"""

        # 调用DeepSeek接口
        completion = client.chat.completions.create(
            model=DEFAULT_MODEL,
            messages=self._messages,
            temperature=0,  # 设置较小的temperature,因为是推理的场景,需要生成更确定性的结果
        )

        # 返回结果
        return completion.choices[0].message.content