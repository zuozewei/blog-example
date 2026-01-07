# -*- coding: utf-8 -*-
"""
@Time    : 2026/01/02 09:39
@Author  : zuozewei
@File    : LangChain的核心抽象.py 
@Desc    : LangChain的核心抽象
@doc     : https://blog.csdn.net/zuozewei/article/details/156682779?spm=1001.2014.3001.5501
"""
import os

import dotenv
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI

# 加载环境变量
dotenv.load_dotenv()

# ChatModel: 对于大模型的抽象
llm = ChatOpenAI(
    openai_api_key=os.getenv("OPENAI_API_KEY"),
    openai_api_base=os.getenv("OPENAI_API_BASE"),
    model_name=os.getenv("AI_MODEL", "deepseek-chat"),
)

# PromptTemplate: 负责编排提示词
prompt = ChatPromptTemplate.from_messages([
    ("system", "请将下面的内容翻译成英文："),
    ("user", "{text}"),
])

# OutputParser: 负责解析LLM的生成结果
parser = StrOutputParser()

# 构造Chain
# Chain中组件的声明顺序,就是其实际的执行顺序
chain = prompt | llm | parser

# 调用Chain,获取执行结果
# 采用流式响应
# 流式响应返回的是一个Token的Iterator,可以通过for循环遍历
stream = chain.stream({"text": "床前明月光,疑似地上霜"})
for chunk in stream: 
    print(chunk, flush=True, end="")
print()