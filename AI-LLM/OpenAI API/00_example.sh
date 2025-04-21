curl -L -X POST 'https://api.deepseek.com/chat/completions' \
-H 'Content-Type: application/json' \
-H 'Accept: application/json' \
-H 'Authorization: Bearer <TOKEN>' \
--data-raw '{
  "messages": [      # 核心参数：对话消息上下文
    {
      "content": "You are a helpful assistant",
      "role": "system"
    },
    {
      "content": "Hi",
      "role": "user"
    }
  ],
  "model": "deepseek-chat",  # 核心参数：指定使用的模型
  "seed": 0,                 # 模型参数：随机种子（控制输出确定性）
  "frequency_penalty": 0,    # 模型参数：频率惩罚（-2~2）
  "max_tokens": 2048,        # 核心参数：最大输出token数
  "presence_penalty": 0,     # 模型参数：存在惩罚（-2~2）
  "response_format": {       # 工程参数：响应格式控制
    "type": "text"
  },
  "stop": null,              # 工程参数：停止生成的条件（字符串或数组）
  "stream": false,           # 工程参数：是否流式输出
  "stream_options": null,    # 工程参数：流式传输配置
  "temperature": 1,          # 核心参数：温度参数（0~2）
  "top_p": 1,                # 核心参数：Top-p采样（0~1）
  "tools": null,             # 工具参数：可用工具列表
  "tool_choice": "none",     # 工具参数：工具选择策略
  "logit_bias": null,        # 模型参数：logit偏置（-100~100）
  "logprobs": false,         # 模型参数：是否返回log概率
  "top_logprobs": null       # 模型参数：返回top log概率数量
}'