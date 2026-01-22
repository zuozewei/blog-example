# -*- coding: utf-8 -*-
"""
@Time    : 2026/01/03 13:08
@Author  : zuozewei
@File    : tools.py 
@Desc    : 定义工具
"""
from langchain_core.tools import tool


# 使用LangChain提供的@tool装饰器,将一个函数装饰成Tool工具
# @tool装饰器会自动提取函数名作为工具名,函数参数作为工具调用参数
# 并且函数的Docstring会作为工具的描述
@tool
def calculate(expression: str) -> float:
    """执行计算并返回结果 - 使用 Python 语法,必要时请使用浮点数语法"""

    # 使用Python的eval机制计算
    return eval(expression)


@tool
def ask_fruit_unit_price(fruit: str) -> str:
    """询问水果的价格"""
    fruit = fruit.strip()
    if fruit.casefold() in ["apple", "苹果"]:
        return "苹果单价是 10元/公斤"
    if fruit.casefold() in ["banana", "香蕉"]:
        return "香蕉单价是 6元/公斤"
    return f"{fruit} 单价是 20元/公斤"

# 经过@tool的装饰后,函数就变成了一个Tool工具
# 即拥有name,description,args等属性
# print(calculate.name)
# print(calculate.description)
# print(calculate.args)
