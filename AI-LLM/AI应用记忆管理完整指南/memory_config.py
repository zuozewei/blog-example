# -*- coding: utf-8 -*-
"""
@Time    : 2025/12/14 04:33
@Author  : zuozewei
@File    : memory_config.py
@Desc    : mem0长期记忆配置 - 基于向量存储的持久化记忆系统
"""
import os
import warnings
from dotenv import load_dotenv

load_dotenv()

api_base = os.getenv("OPENAI_BASE_URL") or os.getenv("OPENAI_API_BASE")
os.environ["OPENAI_API_KEY"] = os.getenv("OPENAI_API_KEY")
os.environ["OPENAI_BASE_URL"] = api_base

from mem0 import Memory


def create_memory():
    """
    创建Memory实例
    :return: Memory实例
    """
    return Memory.from_config({
        "version": "v1.1",
        "llm": {
            "provider": "openai",
            "config": {
                "model": os.getenv("AI_MODEL", "deepseek-v3.2"),
                "temperature": 0,
                "max_tokens": 1500,
            }
        },
        "embedder": {
            "provider": "openai",
            "config": {
                "model": "text-embedding-v4"
            }
        },
        "vector_store": {
            "provider": "chroma",
            "config": {
                "collection_name": "mem0db",
                "path": "mem0db",
            }
        },
        "history_db_path": "history.db",
    })
