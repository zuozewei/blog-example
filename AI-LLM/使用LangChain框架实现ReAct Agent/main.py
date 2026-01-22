# -*- coding: utf-8 -*-
"""
@Time    : 2026/01/03 13:08
@Author  : zuozewei
@File    : main.py
@Desc    : 指定ReAct Agent
"""

from agent import create_agent

if __name__ == '__main__':
    # 创建Agent
    agent = create_agent()

    # 执行Agent
    result = agent.invoke({
        "input": "3公斤苹果和2公斤香蕉的总价格是多少?"
    })

    # 打印结果
    print(result)
