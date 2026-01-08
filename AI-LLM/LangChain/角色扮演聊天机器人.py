# -*- coding: utf-8 -*-
"""
@Time    : 2026/01/07 15:35
@Author  : zuozewei
@File    : 角色扮演聊天机器人.py 
@Desc    : 实现角色扮演聊天机器人
"""
import os
import dotenv
from langchain_openai import ChatOpenAI
from langchain_core.messages import trim_messages
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnableWithMessageHistory

from chat_history import get_session_history
from token_counter import tiktoken_counter

dotenv.load_dotenv()

# 在System Prompt中指定角色描述和上下文信息
prompt = ChatPromptTemplate.from_messages([
    ("system", "你现在正在扮演孔子，他是中国伟大的思想家。请以孔子的口吻和语气，与用户进行对话，对话内容不要出现‘子曰’"),
    ("human", "{query}")
])

# 使用trim_messages来控制历史消息规模
trimmer = trim_messages(
    max_tokens=4096,  # 指定消息的最大Token数量
    strategy="last",  # 指定截断策略为保留最新消息
    token_counter=tiktoken_counter,  # 指定Token计数器函数
    include_system=True,  # 保留System Prompt
)

llm = ChatOpenAI(
    openai_api_key=os.getenv("OPENAI_API_KEY"),
    openai_api_base=os.getenv("OPENAI_API_BASE"),
    model=os.getenv("AI_MODEL", "deepseek-chat"),
    temperature=0.2
)

# 使用RunnableWithMessageHistory,创建带有记忆功能的chain
# 它是对Runnable组件的封装
chain = RunnableWithMessageHistory(
    runnable=prompt | llm | StrOutputParser(),
    get_session_history=get_session_history
)

while True:
    query = input("You >: ")
    if "bye" == query.lower():
        print("bye bye~")
        break

    output = chain.stream(input={"query": query},
                          config={"configurable": {"session_id": "zsa"}}  # 在Config配置中传入session_id
                          )
    print(f"Bot >: ", end="", flush=True)
    for chunk in output:
        print(chunk, end="", flush=True)

    print()
