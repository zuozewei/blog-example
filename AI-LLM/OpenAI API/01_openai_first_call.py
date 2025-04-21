import os
from typing import Dict, List
from openai import OpenAI, OpenAIError
from openai.types.chat import ChatCompletion

def create_openai_client(api_key: str = None, base_url: str = None) -> OpenAI:
    """
    创建OpenAI客户端实例
    
    Args:
        api_key: API密钥，如果为None则从环境变量获取
        base_url: API基础URL，如果为None则使用默认值
        
    Returns:
        OpenAI: OpenAI客户端实例
    """
    return OpenAI(
        api_key=api_key or os.getenv("DASHSCOPE_API_KEY"),
        base_url=base_url or "https://dashscope.aliyuncs.com/compatible-mode/v1"
    )

def get_chat_completion(
    client: OpenAI,
    messages: List[Dict[str, str]],
    model: str = "deepseek-r1"
) -> ChatCompletion:
    """
    获取聊天完成响应
    
    Args:
        client: OpenAI客户端实例
        messages: 消息列表
        model: 模型名称
        
    Returns:
        ChatCompletion: 聊天完成响应
        
    Raises:
        OpenAIError: 当API调用失败时
    """
    try:
        return client.chat.completions.create(
            model=model,
            messages=messages
        )
    except OpenAIError as e:
        print(f"API调用出错: {str(e)}")
        raise

def main():
    """主函数"""
    try:
        # 创建客户端
        client = create_openai_client()
        
        # 准备消息
        messages = [
            {'role': 'user', 'content': '9.9和9.11谁大'}
        ]
        
        # 获取响应
        completion = get_chat_completion(client, messages)
        
        # 打印思考过程
        print("思考过程：")
        print(completion.choices[0].message.reasoning_content)
        
        # 打印最终答案
        print("\n最终答案：")
        print(completion.choices[0].message.content)
        
    except Exception as e:
        print(f"程序执行出错: {str(e)}")

if __name__ == "__main__":
    main()