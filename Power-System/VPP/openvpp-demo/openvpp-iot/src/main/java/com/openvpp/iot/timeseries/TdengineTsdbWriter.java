package com.openvpp.iot.timeseries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TDengine 时序写入器 —— 走 taosAdapter REST（/rest/sql），无需本地驱动。
 * 存储模型：一库一超级表 telemetry(ts, val)，tag 为 device/prop；
 * 每对 (device, property) 自动建子表 —— TDengine 的"一设备一表"思想，
 * 千万级子表是其设计目标场景，与 VPP 海量设备点位天然匹配。
 */
public class TdengineTsdbWriter implements TsdbWriter {

    private final String baseUrl;
    private final String database;
    private final String authHeader;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TdengineTsdbWriter(String host, int port, String database, String user, String password) {
        this.baseUrl = "http://" + host + ":" + port;
        this.database = database;
        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void initSchema() {
        exec(null, "CREATE DATABASE IF NOT EXISTS " + database);
        exec("CREATE STABLE IF NOT EXISTS telemetry (ts TIMESTAMP, val DOUBLE) " +
                "TAGS (device NCHAR(64), prop NCHAR(32))");
    }

    @Override
    public void writeBatch(List<TelemetryPoint> points) {
        if (points.isEmpty()) {
            return;
        }
        // 按 (device, property) 分组拼多表插入：一条 SQL 写入多张子表
        String sql = points.stream()
                .collect(Collectors.groupingBy(p -> subTable(p.getDeviceId(), p.getProperty())))
                .entrySet().stream()
                .map(e -> {
                    String values = e.getValue().stream()
                            .map(p -> "(" + p.getTsMs() + ", " + p.getValue() + ")")
                            .collect(Collectors.joining(" "));
                    TelemetryPoint first = e.getValue().get(0);
                    return e.getKey() + " USING telemetry TAGS ('" + first.getDeviceId() + "', '"
                            + first.getProperty() + "') VALUES " + values;
                })
                .collect(Collectors.joining(" "));
        exec("INSERT INTO " + sql);
    }

    @Override
    public List<TelemetryPoint> queryRange(String deviceId, String property, long fromMs, long toMs) {
        JsonNode result = exec("SELECT ts, val FROM telemetry WHERE device = '" + deviceId
                + "' AND prop = '" + property + "' AND ts >= " + fromMs + " AND ts <= " + toMs
                + " ORDER BY ts");
        List<TelemetryPoint> points = new ArrayList<>();
        for (JsonNode row : result.path("data")) {
            // REST 返回的 ts 为 ISO 字符串，此处演示按毫秒数存储时用数值比较，
            // 故 SELECT 时用 UNIX 时间戳列读取
            long ts = row.get(0).isNumber() ? row.get(0).asLong()
                    : parseIsoToMs(row.get(0).asText());
            points.add(new TelemetryPoint(deviceId, property, ts, row.get(1).asDouble()));
        }
        return points;
    }

    /** 子表名必须字母开头且只含字母数字下划线 */
    static String subTable(String deviceId, String property) {
        String raw = (deviceId + "_" + property).replaceAll("[^A-Za-z0-9_]", "_");
        return "t_" + raw;
    }

    private JsonNode exec(String sql) {
        return exec(database, sql);
    }

    private JsonNode exec(String db, String sql) {
        try {
            String url = baseUrl + "/rest/sql" + (db == null ? "" : "/" + db);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(sql))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = objectMapper.readTree(response.body());
            if (body.path("code").asInt() != 0) {
                throw new IllegalStateException("TDengine SQL 失败: " + body.path("desc").asText() + " | " + sql);
            }
            return body;
        } catch (Exception e) {
            throw new IllegalStateException("TDengine 请求失败: " + e.getMessage(), e);
        }
    }

    private static long parseIsoToMs(String iso) {
        return java.time.Instant.from(
                java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(
                        iso.replace(' ', 'T'))).toEpochMilli();
    }

    @Override
    public String name() {
        return "TDENGINE";
    }
}
