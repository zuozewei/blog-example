# 会议转录与摘要生成系统

## 项目概述

本项目是一个自动化会议转录与摘要生成系统，通过分析会议视频/音频内容，自动生成结构化的会议纪要。系统结合了语音识别、文本处理以及AI摘要生成技术，实现从会议录制到最终摘要的完整流程。

## 主要功能特性

- **音频提取**：从会议视频文件中提取音频内容
- **语音转文字**：使用Whisper模型将会议音频转换为文本
- **文本清理与处理**：优化转录文本，提高可读性和摘要生成质量
- **关键信息提取**：从会议内容中识别关键词、人名、任务和议题
- **AI摘要生成**：调用DeepSeek API自动生成结构化会议纪要
- **简单命令行接口**：提供用户友好的命令行操作方式

## 技术栈

- **核心语言**：Python
- **音频处理**：ffmpeg-python
- **语音识别**：OpenAI Whisper
- **文本处理**：jieba分词、正则表达式
- **API集成**：DeepSeek-r1大语言模型

## 安装指南

### 前置要求

- Python 3.8+
- FFmpeg（系统依赖）

### 安装步骤

1. 克隆仓库

```bash
git clone <仓库地址>
cd transcribe
```

2. 安装依赖

```bash
pip install ffmpeg-python openai-whisper jieba python-dotenv openai
```

3. 配置API密钥

创建一个`.env`文件，添加以下内容：

```
DEEPSEEK_API_KEY=你的DeepSeek_API密钥
DEEPSEEK_API_BASE=https://api.deepseek.com/v1
```

## 使用方法

基本用法：

```bash
python meeting_summary.py 会议视频路径 --output 输出目录 --model medium
```

参数说明：
- `会议视频路径`：必需，要处理的会议视频文件路径
- `--output`：可选，输出目录，默认为"output"
- `--model`：可选，Whisper模型大小，可选值为tiny/base/small/medium/large，默认为"medium"

## 输出结果

处理完成后，在输出目录中将生成以下文件：

- **audio.m4a**：从视频中提取的音频文件
- **transcript.txt**：完整的会议转录文本（无时间戳）
- **transcript_with_timestamps.txt**：带时间戳的会议转录文本
- **summary.md**：最终生成的会议纪要（Markdown格式）

## 目录结构

```
├── meeting_summary.py   # 主程序文件
├── .env                 # 环境变量配置文件（需自行创建）
├── meeting_01.mp4       # 示例会议视频文件
└── output/              # 输出目录
    ├── audio.m4a
    ├── transcript.txt
    ├── transcript_with_timestamps.txt
    └── summary.md
```

## 处理流程

1. 从会议视频文件中提取音频
2. 使用Whisper模型进行语音转文字
3. 清理和优化转录文本
4. 提取关键信息（关键词、人名、任务等）
5. 利用DeepSeek大语言模型生成结构化会议纪要
6. 将结果保存为Markdown格式

## 注意事项

- Whisper模型首次运行时会自动下载，请确保网络连接正常
- 较大的视频文件处理可能需要较长时间
- 摘要质量依赖于语音识别的准确性和DeepSeek模型的性能 