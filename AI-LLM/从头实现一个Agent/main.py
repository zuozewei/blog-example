# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/13 19:39
@Author  : zuozewei
@File    : main.py
@Desc    : 实现Agent的执行过程
"""
import re

from agent import Agent
from tools import calculate, ask_fruit_unit_price

# 定义ReAct Prompt
react_prompt = """
You run in a loop of Thought, Action, PAUSE, Observation.
At the end of the loop you output an Answer
Use Thought to describe your thoughts about the question you have been asked.
Use Action to run one of the actions available to you - then return PAUSE.
Observation will be the result of running those actions.

Your available actions are:

calculate:
e.g. calculate: 4 * 7 / 3
Runs a calculation and returns the number - uses Python so be sure to use floating point syntax if necessary

ask_fruit_unit_price:
e.g. ask_fruit_unit_price: apple
Asks the user for the price of a fruit

Example session:

Question: What is the unit price of apple?
Thought: I need to ask the user for the price of an apple to provide the unit price. 
Action: ask_fruit_unit_price: apple
PAUSE

You will be called again with this:

Observation: Apple unit price is 10/kg

You then output:

Answer: The unit price of apple is 10 per kg.
""".strip()

# 定义正则表达式,用于匹配Action指令
action_re = re.compile(r'^Action: (\w+): (.*)$')

# 定于可用的工具(动作)列表
known_tools = {
    "calculate": calculate,
    "ask_fruit_unit_price": ask_fruit_unit_price,
}


def react(query: str, max_turns: int = 5) -> None:
    """
    ReAct执行过程
    :param query: 用户的提问
    :param max_turns: 最大执行轮次,默认为5
    """

    # 创建Agent实例
    agent = Agent(system_prompt=react_prompt)

    # 执行ReAct推理过程
    question = query
    for turn in range(max_turns):
        print(f"Question: {question}")

        # 执行Agent,获取结果
        result = agent.invoke(question)

        # 匹配Action指令
        actions = [action_re.match(a) for a in result.split('\n') if action_re.match(a)]
        if len(actions) == 0:
            print(f"Finish")
            return

        # 解析需要调用的工具及调用参数
        tool, params = actions[0].groups()
        if tool not in known_tools:
            print(f"Unknown tool: {tool}")
            return

        # 调用工具,获取结果
        print(f"call tool: {tool}, params: {params}")
        observation = known_tools[tool](params)
        print(f"observation: {observation}")

        # 将Observation作为下一轮的输入,继续该过程
        question = f"Observation: {observation}\n\n"


if __name__ == '__main__':
    react(query="What is the total price of 3kg of apple and 2kg of banana?", max_turns=10)
