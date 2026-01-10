# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/09 19:19
@Author  : zuozewei
@File    : tools.py 
@Desc    : 定义工具
"""


def calculate(expression: str) -> float:
    """计算表达式"""

    # 使用Python的eval机制计算
    return eval(expression)


def ask_fruit_unit_price(fruit: str) -> str:
    """询问水果的单价"""
    if fruit.casefold() == "apple":
        return "Apple unit price is 10/kg"
    if fruit.casefold() == "banana":
        return "Banana unit price is 6/kg"
    return f"{fruit} unit price is 20/kg"
