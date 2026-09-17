package com.openvpp.iot.timeseries;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClickHouse 时序写入器 —— 走 HTTP 接口，无需本地驱动。
 * 存储模型：单宽表 telemetry(device, prop, ts, val)，
 * MergeTree 按 (device, prop, ts) 排序 —— 列式引擎的长处是聚合分析，
 * 基线核算、报表统计这类"按时间段扫描聚合"的场景是它的主场。
 */
public class ClickHouseTsdbWriter implements TsdbWriter {

    private final String baseUrl;
    private final String database;
    private final String user;
    private final String password;
    private final HttpClient http = HttpClient.newHttpClient();

    public ClickHouseTsdbWriter(String host, int port, String database, String user, String password) {
        this.baseUrl = "http://" + host + ":" + port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    @Override
    public void initSchema() {
        exec("CREATE DATABASE IF NOT EXISTS " + database);
        exec("CREATE TABLE IF NOT EXISTS " + database + ".telemetry (" +
                "device String, prop String, ts DateTime64(3), val Float64) " +
                "ENGINE = MergeTree ORDER BY (device, prop, ts)");
    }

    @Override
    public void writeBatch(List<TelemetryPoint> points) {
        if (points.isEmpty()) {
            return;
        }
        // TSV 批量插入：ClickHouse 的推荐姿势，比逐行 INSERT 快几个数量级
        String tsv = points.stream()
                .map(p -> p.getDeviceId() + "\t" + p.getProperty() + "\t"
                        + formatTs(p.getTsMs()) + "\t" + p.getValue())
                .collect(Collectors.joining("\n"));
        exec("INSERT INTO " + database + ".telemetry (device, prop, ts, val) FORMAT TabSeparated\n" + tsv);
    }

    @Override
    public List<TelemetryPoint> queryRange(String deviceId, String property, long fromMs, long toMs) {
        String body = exec("SELECT device, prop, toUnixTimestamp64Milli(ts), val FROM " + database + ".telemetry"
                + " WHERE device = '" + deviceId + "' AND prop = '" + property + "'"
                + " AND ts >= fromUnixTimestamp64Milli(" + fromMs + ")"
                + " AND ts <= fromUnixTimestamp64Milli(" + toMs + ")"
                + " ORDER BY ts FORMAT TabSeparated");
        List<TelemetryPoint> points = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            String[] cols = line.split("\t");
            points.add(new TelemetryPoint(cols[0], cols[1],
                    Long.parseLong(cols[2]), Double.parseDouble(cols[3])));
        }
        return points;
    }

    private String exec(String sql) {
        try {
            String url = baseUrl + "/?user=" + enc(user) + "&password=" + enc(password);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(sql))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("ClickHouse SQL 失败: " + response.body());
            }
            return response.body();
        } catch (Exception e) {
            throw new IllegalStateException("ClickHouse 请求失败: " + e.getMessage(), e);
        }
    }

    private static String formatTs(long ms) {
        // 关键：必须与 ClickHouse 服务端时区（UTC）对齐，
        // 用客户端本地时区格式化会整体偏移，导致时间窗查询失配
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                .withZone(java.time.ZoneOffset.UTC)
                .format(java.time.Instant.ofEpochMilli(ms));
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Override
    public String name() {
        return "CLICKHOUSE";
    }
}
