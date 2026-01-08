# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/24 08:03
@Author  : zuozewei
@File    : 格式化响应.py 
@Desc    : 格式化响应
"""
import os

import dotenv
from langchain_core.output_parsers import JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from pydantic import BaseModel, Field


# 定义响应数据格式
class Work(BaseModel):
    title: str = Field(description="作品标题")
    intro: str = Field(description="作品简介")


dotenv.load_dotenv()

llm = ChatOpenAI(
    openai_api_key=os.getenv("OPENAI_API_KEY"),
    openai_api_base=os.getenv("OPENAI_API_BASE"),
    model_name=os.getenv("AI_MODEL", "deepseek-chat"),
)

# PromptTemplate: 负责编排提示词
prompt = ChatPromptTemplate.from_messages([
    ("human", "请帮我列出{author}的{count}部作品信息。按照以下格式返回: \n{format_instruction}"),
])

# 根据指定的数据类型,创建json输出解析器
parser = JsonOutputParser(pydantic_object=Work)

# 使用输出解析器生成format_instruction, 并传入到prompt中
prompt = prompt.partial(format_instruction=parser.get_format_instructions())

# 构造Chain
chain = prompt | llm | parser

# 执行chain
result = chain.invoke({"author": "鲁迅", "count": 4})
print(type(result[0]))  # 每一项的类型为<class 'dict'>
print(result)