package com.openvpp.resource.ledger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 档案审计日志 —— 只追加，不可改。
 * 演示版内存列表；生产形态为独立审计表（禁 UPDATE/DELETE 权限，
 * 或接入日志审计服务），按资源 ID 检索回放变更历史。
 */
public class AuditTrail {

    private final Map<String, List<AuditRecord>> records = new ConcurrentHashMap<>();

    public void append(AuditRecord record) {
        records.computeIfAbsent(record.getResourceId(), k -> new ArrayList<>()).add(record);
    }

    /** 按资源回放全部变更历史，按时间升序 */
    public List<AuditRecord> historyOf(String resourceId) {
        return records.getOrDefault(resourceId, Collections.emptyList())
                .stream().collect(Collectors.toUnmodifiableList());
    }

    public int totalCount() {
        return records.values().stream().mapToInt(List::size).sum();
    }
}
