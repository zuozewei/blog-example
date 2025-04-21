import os
from openai import OpenAI
from typing import List, Dict
import logging

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def get_api_key() -> str:
    """获取API密钥，优先从环境变量获取，如果没有则提示错误"""
    api_key = os.getenv("DASHSCOPE_API_KEY")
    if not api_key:
        raise ValueError("请设置环境变量 'DASHSCOPE_API_KEY' 或直接在代码中设置 api_key")
    return api_key

def create_chat_completion(client: OpenAI, messages: List[Dict[str, str]], model: str = "deepseek-r1") -> dict:
    """创建对话并获取响应"""
    try:
        completion = client.chat.completions.create(
            model=model,
            messages=messages
        )
        return completion
    except Exception as e:
        logging.error(f"调用API时发生错误: {str(e)}")
        raise

def print_response(completion: dict, round_num: int):
    """格式化打印响应结果"""
    separator = "=" * 20
    print(f"\n{separator} 第{round_num}轮对话 {separator}")
    print(f"{separator} 思考过程 {separator}")
    print(completion.choices[0].message.reasoning_content)
    print(f"{separator} 最终答案 {separator}")
    print(completion.choices[0].message.content)
    print("\n")

def main():
    try:
        # 初始化客户端
        client = OpenAI(
            api_key=get_api_key(),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1"
        )

        # 初始化对话历史
        messages = [
            {'role': 'user', 'content': '你好'}
        ]

        # 第一轮对话
        completion = create_chat_completion(client, messages)
        print_response(completion, 1)

        # 更新对话历史
        messages.append({'role': 'assistant', 'content': completion.choices[0].message.content})
        messages.append({'role': 'user', 'content': '你是谁'})

        # 第二轮对话
        completion = create_chat_completion(client, messages)
        print_response(completion, 2)

    except Exception as e:
        logging.error(f"程序执行出错: {str(e)}")
        raise

if __name__ == "__main__":
    main()