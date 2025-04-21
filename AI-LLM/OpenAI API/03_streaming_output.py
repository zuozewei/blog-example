from typing import Optional
from openai import OpenAI
from openai.types.chat import ChatCompletionChunk
import os
import sys

class StreamingChat:
    """用于处理OpenAI API流式聊天响应的类。"""
    
    def __init__(self, api_key: Optional[str] = None, base_url: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"):
        """
        初始化StreamingChat实例。
        
        参数:
            api_key: 可选的API密钥。如果未提供，将尝试从环境变量获取。
            base_url: API端点的基础URL。
        """
        self.api_key = api_key or os.getenv("DASHSCOPE_API_KEY")
        if not self.api_key:
            raise ValueError("必须直接提供API密钥或通过环境变量设置")
            
        self.client = OpenAI(
            api_key=self.api_key,
            base_url=base_url
        )
        self.reasoning_content = ""
        self.answer_content = ""
        self.is_answering = False

    def process_stream(self, prompt: str, model: str = "deepseek-r1") -> tuple[str, str]:
        """
        处理流式聊天响应。
        
        参数:
            prompt: 用户的输入提示
            model: 使用的模型名称
            
        返回:
            tuple: (推理内容, 回答内容)的元组
        """
        try:
            completion = self.client.chat.completions.create(
                model=model,
                messages=[{"role": "user", "content": prompt}],
                stream=True
            )
            
            print("\n" + "=" * 20 + "思考过程" + "=" * 20 + "\n")
            
            for chunk in completion:
                self._process_chunk(chunk)
                
            return self.reasoning_content, self.answer_content
            
        except Exception as e:
            print(f"发生错误: {str(e)}", file=sys.stderr)
            raise

    def _process_chunk(self, chunk: ChatCompletionChunk) -> None:
        """
        处理流中的单个数据块。
        
        参数:
            chunk: 来自流的数据块
        """
        if not chunk.choices:
            if hasattr(chunk, 'usage'):
                print("\n用量统计:", chunk.usage)
            return

        delta = chunk.choices[0].delta
        
        if hasattr(delta, 'reasoning_content') and delta.reasoning_content:
            print(delta.reasoning_content, end='', flush=True)
            self.reasoning_content += delta.reasoning_content
        else:
            if delta.content and not self.is_answering:
                print("\n" + "=" * 20 + "完整回复" + "=" * 20 + "\n")
                self.is_answering = True
            if delta.content:
                print(delta.content, end='', flush=True)
                self.answer_content += delta.content

def main():
    """主函数，用于演示使用方法。"""
    try:
        chat = StreamingChat()
        reasoning, answer = chat.process_stream("9.9和9.11谁大")
        
    except Exception as e:
        print(f"程序执行出错: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()