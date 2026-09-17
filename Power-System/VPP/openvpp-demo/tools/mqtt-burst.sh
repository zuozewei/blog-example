#!/usr/bin/env bash
# MQTT 上行突发压测脚本（专栏第 18 篇配套）
# 用法：./mqtt-burst.sh [broker_host] [消息数] [设备数]
# 依赖：mosquitto_pub（https://mosquitto.org/download/）
set -euo pipefail

BROKER="${1:-broker-cn.emqx.io}"
COUNT="${2:-1000}"
DEVICES="${3:-100}"

echo "[BURST] broker=$BROKER messages=$COUNT devices=$DEVICES"
start=$(date +%s%N)

for i in $(seq 1 "$COUNT"); do
  dev=$(( (i - 1) % DEVICES + 1 ))
  ts=$(( $(date +%s) * 1000 + i % 1000 ))
  payload="{\"ts\":$ts,\"seq\":$i,\"payload\":{\"power\":$(( 100 + i % 50 ))}}"
  mosquitto_pub -h "$BROKER" -t "openvpp/bench-dev-$dev/telemetry" -m "$payload" -q 0 &
  # 每 100 条收敛一次并发，防本地进程爆炸
  if (( i % 100 == 0 )); then wait; fi
done
wait

end=$(date +%s%N)
elapsed_ms=$(( (end - start) / 1000000 ))
echo "[BURST] sent $COUNT messages in ${elapsed_ms}ms ($(( COUNT * 1000 / (elapsed_ms + 1) )) msg/s)"
