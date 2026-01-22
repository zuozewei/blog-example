# -*- coding: utf-8 -*-
"""
@Time    : 2026/01/03 13:08
@Author  : zuozewei
@File    : agent.py 
@Desc    : 使用LangChain实现ReAct Agent
"""
import os

import dotenv
from langchain.agents import AgentExecutor, create_react_agent
from langchain_core.prompts import PromptTemplate
from langchain_openai import ChatOpenAI

from tools import calculate, ask_fruit_unit_price

# ReAct Prompt
prompt = PromptTemplate.from_template('''Answer the following questions as best you can. You have access to the following tools:

{tools}

Use the following format:

Question: the input question you must answer
Thought: you should always think about what to do
Action: the action to take, should be one of [{tool_names}]
Action Input: the input to the action
Observation: the result of the action
... (this Thought/Action/Action Input/Observation can repeat N times)
Thought: I now know the final answer
Final Answer: the final answer to the original input question

Begin!

Question: {input}
Thought:{agent_scratchpad}''')


def create_agent() -> AgentExecutor:
    """创建ReAct Agent执行器"""

    # 加载环境变量
    dotenv.load_dotenv()

    # 创建LLM
    llm = ChatOpenAI(
        openai_api_key=os.getenv("OPENAI_API_KEY"),
        openai_api_base=os.getenv("OPENAI_API_BASE"),
        model=os.getenv("AI_MODEL", "deepseek-chat"),
        temperature=0.1,  # 在推理场景中,设置较小的temperature,可以生成较为确定性的结果
    )

    # 定义Tools工具列表
    tools = [calculate, ask_fruit_unit_price]

    # 创建ReAct Agent
    agent = create_react_agent(llm=llm, tools=tools, prompt=prompt)

    # 创建AgentExecutor执行器,并返回
    return AgentExecutor(agent=agent,  # 指定Agent
                         tools=tools,  # 指定工具列表
                         verbose=True  # 开启verbose,可以打印详细的执行过程
                         )
