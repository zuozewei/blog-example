#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
RAG 国标知识库检索演示 —— 专栏第 30 篇配套代码。
零第三方依赖（纯标准库），完整复现 RAG 的"检索"半链路：
  1. 语料：本仓库的 VPP 国标解析 Markdown 文档（真实语料，非玩具）；
  2. 分块：按 ## 标题切块（RAG 分块策略的最小形态）；
  3. 向量化：中文 bigram 分词 + TF-IDF 加权（生产替换为 embedding 模型）；
  4. 检索：查询向量化 → 余弦相似度 → Top-K 命中块。

运行：python rag_demo.py "查询问题"
"""
import math
import os
import re
import sys
from collections import Counter

CORPUS_DIRS = [
    # 相对本脚本的真实语料（tools/ai → 上溯 6 级到仓库根 zuozewei/）
    "../../../../../../blog-post/12、电力系统/1、基础认知与国标规范",
    "../../../../../../blog-post/12、电力系统/4、行业总结笔记",
]
TOP_K = 3


def bigrams(text):
    """中文 bigram 分词：对 CJK 字符取相邻双字，对英文/数字取连续片段。"""
    text = re.sub(r"\s+", " ", text)
    tokens = re.findall(r"[A-Za-z0-9]+(?:\.[0-9]+)?|[\u4e00-\u9fff]", text)
    grams = []
    for i in range(len(tokens) - 1):
        if re.match(r"[\u4e00-\u9fff]", tokens[i]) and re.match(r"[\u4e00-\u9fff]", tokens[i + 1]):
            grams.append(tokens[i] + tokens[i + 1])
    grams.extend(t for t in tokens if re.match(r"[A-Za-z0-9]", t))
    return grams


def load_chunks():
    """扫描语料目录，按 ## 标题切块。每块 = {doc, heading, text}。"""
    chunks = []
    for d in CORPUS_DIRS:
        base = os.path.normpath(os.path.join(os.path.dirname(__file__), d))
        if not os.path.isdir(base):
            continue
        for fn in sorted(os.listdir(base)):
            if not fn.endswith(".md"):
                continue
            with open(os.path.join(base, fn), encoding="utf-8", errors="ignore") as f:
                text = f.read()
            # 按 ## 切块；首个块为文首到第一个 ##
            parts = re.split(r"\n(?=## )", text)
            for p in parts:
                heading = p.split("\n", 1)[0].lstrip("# ").strip()[:60]
                body = re.sub(r"[#*|>\-\[\]()`\s]+", " ", p)[:2000]
                if len(body.strip()) > 50:
                    chunks.append({"doc": fn, "heading": heading, "text": body})
    return chunks


def tfidf_vectors(chunks):
    """构建 TF-IDF 索引：chunk 向量 + 全局 IDF。"""
    doc_freq = Counter()
    chunk_tokens = []
    for c in chunks:
        toks = bigrams(c["text"])
        chunk_tokens.append(toks)
        doc_freq.update(set(toks))
    n = len(chunks)
    idf = {t: math.log(n / df) + 1.0 for t, df in doc_freq.items()}
    vectors = []
    for toks in chunk_tokens:
        tf = Counter(toks)
        vec = {t: cnt * idf[t] for t, cnt in tf.items()}
        norm = math.sqrt(sum(v * v for v in vec.values())) or 1.0
        vectors.append({t: v / norm for t, v in vec.items()})
    return vectors, idf


def query_vector(query, idf):
    tf = Counter(bigrams(query))
    vec = {t: cnt * idf.get(t, math.log(len(idf)) + 1) for t, cnt in tf.items()}
    norm = math.sqrt(sum(v * v for v in vec.values())) or 1.0
    return {t: v / norm for t, v in vec.items()}


def search(query, chunks, vectors, idf, top_k=TOP_K):
    qv = query_vector(query, idf)
    scored = []
    for i, cv in enumerate(vectors):
        score = sum(w * cv.get(t, 0.0) for t, w in qv.items())
        scored.append((score, i))
    scored.sort(reverse=True)
    return [(s, chunks[i]) for s, i in scored[:top_k] if s > 0]


def main():
    query = sys.argv[1] if len(sys.argv) > 1 else "虚拟电厂调节容量不低于多少"
    chunks = load_chunks()
    if not chunks:
        print("未找到语料，请检查 CORPUS_DIRS 路径")
        return
    vectors, idf = tfidf_vectors(chunks)
    print(f"语料块数: {len(chunks)}  |  查询: {query}\n")
    for score, c in search(query, chunks, vectors, idf):
        print(f"[{score:.4f}] {c['doc']} :: {c['heading']}")
        print(f"        {c['text'][:110]}...")
        print()


if __name__ == "__main__":
    main()
