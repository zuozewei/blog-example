#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
INT8 对称量化演示 —— 专栏第 29 篇《云训边推全链路》配套代码。
零第三方依赖（纯标准库），用模拟权重复现"训练机 FP32 → 边缘 INT8"的数值实验：
  1. 生成模拟全连接层权重（正态分布，模拟负荷预测模型的一层）；
  2. 对称 INT8 量化：scale = max|w| / 127，量化 w_q = round(w/scale)，反量化 w' = w_q * scale；
  3. 前向推理对比：FP32 vs 反量化权重的输出误差与余弦相似度；
  4. 模型体积对比：FP32 4 字节/参数 vs INT8 1 字节/参数。

运行：python quantize_demo.py
"""
import math
import random


def gen_weights(rows, cols, seed=42):
    """模拟一层全连接权重（He 初始化风格），并生成一批模拟输入。"""
    rng = random.Random(seed)
    std = math.sqrt(2.0 / cols)
    w = [[rng.gauss(0.0, std) for _ in range(cols)] for _ in range(rows)]
    x = [rng.gauss(0.0, 1.0) for _ in range(cols)]
    return w, x


def matvec(w, x):
    return [sum(wr[j] * x[j] for j in range(len(x))) for wr in w]


def quantize_int8(w):
    """对称量化：整层共享一个 scale（per-tensor，TFLite 全整数量化的基础形态）。"""
    scale = max(abs(v) for row in w for v in row) / 127.0
    q = [[round(v / scale) for v in row] for row in w]
    return q, scale


def dequantize(q, scale):
    return [[v * scale for v in row] for row in q]


def cosine(a, b):
    dot = sum(x * y for x, y in zip(a, b))
    na = math.sqrt(sum(x * x for x in a))
    nb = math.sqrt(sum(y * y for y in b))
    return dot / (na * nb)


def main():
    rows, cols = 64, 128          # 模拟负荷预测模型的一层
    w, x = gen_weights(rows, cols)

    # 基准：FP32 前向
    y_fp32 = matvec(w, x)

    # 量化 → 反量化 → 前向
    q, scale = quantize_int8(w)
    w_hat = dequantize(q, scale)
    y_int8 = matvec(w_hat, x)

    # 误差统计
    max_abs_err = max(abs(a - b) for a, b in zip(y_fp32, y_int8))
    mean_abs = sum(abs(a - b) for a, b in zip(y_fp32, y_int8)) / len(y_fp32)
    fp32_range = max(y_fp32) - min(y_fp32)
    cos = cosine(y_fp32, y_int8)

    # 权重逐点误差
    w_err = max(abs(a - b) for ra, rb in zip(w, w_hat) for a, b in zip(ra, rb))

    fp32_bytes = rows * cols * 4
    int8_bytes = rows * cols * 1

    print(f"层形状: {rows}x{cols}（{rows*cols} 参数）")
    print(f"量化 scale: {scale:.6f}")
    print(f"权重最大逐点误差: {w_err:.6f}")
    print(f"输出最大绝对误差: {max_abs_err:.6f}（输出量级 {fp32_range:.2f}，占比 {max_abs_err/fp32_range*100:.2f}%）")
    print(f"输出平均绝对误差: {mean_abs:.6f}")
    print(f"输出余弦相似度: {cos:.6f}")
    print(f"体积: FP32 {fp32_bytes} B → INT8 {int8_bytes} B（压缩比 {fp32_bytes/int8_bytes:.0f}:1）")


if __name__ == "__main__":
    main()
