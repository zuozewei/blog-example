import os
import re
import ffmpeg
import whisper
import jieba
import jieba.analyse
from datetime import datetime
from dotenv import load_dotenv
import argparse
from openai import OpenAI
import warnings

# 加载环境变量
load_dotenv()

# 过滤掉特定的警告
warnings.filterwarnings("ignore", message="FP16 is not supported on CPU; using FP32 instead")

class MeetingSummarizer:
    def __init__(self, video_path, output_dir="output", whisper_model="medium"):
        """初始化会议总结器"""
        self.video_path = video_path
        self.output_dir = output_dir
        self.audio_path = os.path.join(output_dir, "audio.m4a")
        self.transcript_path = os.path.join(output_dir, "transcript.txt")
        self.summary_path = os.path.join(output_dir, "summary.md")
        self.whisper_model = whisper_model
        
        # 创建输出目录
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)
    
    def extract_audio(self):
        """从视频中提取音频"""
        # 检查音频文件是否已存在
        if os.path.exists(self.audio_path):
            print(f"音频文件已存在: {self.audio_path}，跳过提取步骤")
            return True
        
        print("正在从视频中提取音频...")
        try:
            (
                ffmpeg
                .input(self.video_path)
                .output(self.audio_path, acodec='copy', vn=None)
                .run(quiet=True, overwrite_output=True)
            )
            print(f"音频提取完成: {self.audio_path}")
            return True
        except Exception as e:
            print(f"音频提取失败: {str(e)}")
            return False
    
    def transcribe_audio(self):
        """使用Whisper将音频转为文字"""
        print("正在进行语音识别...")
        try:
            # 仅在加载和使用模型时抑制警告
            with warnings.catch_warnings():
                warnings.filterwarnings("ignore", message="FP16 is not supported on CPU; using FP32 instead")
                model = whisper.load_model(self.whisper_model)
                result = model.transcribe(self.audio_path, language="zh")
            
            # 保存完整转写结果
            with open(self.transcript_path, 'w', encoding='utf-8') as f:
                f.write(result["text"])
            
            # 保存带时间戳的转写结果
            timestamp_path = os.path.join(self.output_dir, "transcript_with_timestamps.txt")
            with open(timestamp_path, 'w', encoding='utf-8') as f:
                for segment in result["segments"]:
                    start_time = self.format_timestamp(segment["start"])
                    text = segment["text"]
                    f.write(f"[{start_time}] {text}\n")
            
            print(f"语音识别完成，文本已保存至: {self.transcript_path}")
            return result["text"]
        except Exception as e:
            print(f"语音识别失败: {str(e)}")
            return None
    
    def format_timestamp(self, seconds):
        """将秒数格式化为时:分:秒格式"""
        hours = int(seconds // 3600)
        minutes = int((seconds % 3600) // 60)
        secs = int(seconds % 60)
        return f"{hours:02d}:{minutes:02d}:{secs:02d}"
    
    def clean_transcript(self, text):
        """清理文本，去除口语化表达和重复内容"""
        print("正在清理转写文本...")
        # 去除语气词
        filler_words = ["嗯", "啊", "那个", "就是", "然后", "所以", "这个", "我觉得", "你知道"]
        for word in filler_words:
            text = text.replace(word + " ", " ")
        
        # 去除重复词
        text = re.sub(r'(\b\w+\b)(\s+\1\b)+', r'\1', text, flags=re.IGNORECASE)
        
        return text
    
    def extract_key_info(self, text):
        """提取关键信息（人名、时间、任务等）"""
        print("正在提取关键信息...")
        
        # 使用jieba提取关键词
        keywords = jieba.analyse.extract_tags(text, topK=15, withWeight=True)
        
        # 提取可能的人名（这里是简化实现，实际应用中应使用NER模型）
        name_pattern = r'@(\w+)'
        possible_names = re.findall(name_pattern, text)
        
        # 提取可能的任务
        task_pattern = r'(需要|应该|必须|要)([^。，！？\n]+)'
        possible_tasks = re.findall(task_pattern, text)
        
        # 识别议题（简化实现）
        topic_markers = ["关于", "议题", "讨论", "主题"]
        possible_topics = []
        for marker in topic_markers:
            pattern = f"{marker}([^。，！？\n]+)"
            topics = re.findall(pattern, text)
            possible_topics.extend(topics)
        
        return {
            "keywords": keywords,
            "possible_names": possible_names,
            "possible_tasks": [task[1].strip() for task in possible_tasks],
            "possible_topics": possible_topics
        }
    
    def generate_summary_with_ai(self, text):
        """使用DeepSeek-r1模型生成会议摘要"""
        print("正在使用DeepSeek API生成会议摘要...")
        try:
            # 使用OpenAI客户端连接DeepSeek API
            client = OpenAI(
                api_key=os.getenv("DEEPSEEK_API_KEY"),
                base_url="https://api.deepseek.com"
            )

            # 调用DeepSeek API
            response = client.chat.completions.create(
                model="deepseek-reasoner",  # 使用DeepSeek聊天&推理模型
                messages=[
                    {"role": "system", "content": "你是一个专业的会议纪要生成助手。请根据提供的会议记录，生成一份结构化的会议纪要，包括会议主题、参会人员、主要议题、讨论内容、结论和待办事项。"},
                    {"role": "user", "content": f"请根据以下会议记录生成一份详细的会议纪要:\n\n{text}"}
                ],
                temperature=0.7,
                max_tokens=2000,
                stream=False
            )
            
            # 获取生成的摘要
            summary = response.choices[0].message.content
            
            # 保存摘要
            with open(self.summary_path, 'w', encoding='utf-8') as f:
                f.write(summary)
            
            print(f"AI摘要生成完成，已保存至: {self.summary_path}")
            return summary
            
        except Exception as e:
            print(f"AI摘要生成失败: {str(e)}")
            return None
    
    def process(self):
        """处理完整的会议录制分析流程"""
        # 第1步：提取音频
        if not self.extract_audio():
            return False
        
        # 第2步：语音转文字
        transcript = self.transcribe_audio()
        if not transcript:
            return False
        
        # 第3步：清理文本
        clean_text = self.clean_transcript(transcript)
        
        # 第4步：提取关键信息
        key_info = self.extract_key_info(clean_text)
        
        # 第5步：生成摘要
        summary = self.generate_summary_with_ai(clean_text)
        if not summary:
            return False
        
        print("\n完成! 会议记录分析与摘要生成已完成。")
        print(f"摘要文件位置: {self.summary_path}")
        return True

def main():
    parser = argparse.ArgumentParser(description='会议录制分析与纪要生成工具')
    parser.add_argument('video_path', help='会议录制视频的路径')
    parser.add_argument('--output', default='output', help='输出目录')
    parser.add_argument('--model', default='medium', choices=['tiny', 'base', 'small', 'medium', 'large'], 
                        help='Whisper模型大小 (默认: medium)')
    
    args = parser.parse_args()
    
    summarizer = MeetingSummarizer(
        video_path=args.video_path,
        output_dir=args.output,
        whisper_model=args.model
    )
    
    summarizer.process()

if __name__ == "__main__":
    main() 