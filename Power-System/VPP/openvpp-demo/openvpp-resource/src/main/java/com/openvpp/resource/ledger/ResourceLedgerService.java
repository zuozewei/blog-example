package com.openvpp.resource.ledger;

import com.openvpp.common.enums.ResourceType;
import com.openvpp.resource.profile.ResourceProfile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 资源台账服务 —— 平台资源档案的唯一权威入口。
 *
 * 承载三条硬规则：
 * 1. 44260 排他性：同一（户号/并网点）同一时期只能接入一个运营商 —— 建档强制校验；
 * 2. 44260 代理期限：代理协议宜不小于 1 个月 —— 建档与调度前校验；
 * 3. 47241 运行管理：档案变更必须留审计 —— 建档/变更/注销全留痕。
 */
public class ResourceLedgerService {

    private final Map<String, ResourceProfile> profiles = new ConcurrentHashMap<>();
    private final AuditTrail auditTrail;

    public ResourceLedgerService(AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    /**
     * 建档入库。排他性与期限校验不过即抛异常，不产生任何记录。
     */
    public synchronized ResourceProfile enroll(ResourceProfile profile, String gridAccountId) {
        assertExclusive(gridAccountId);
        assertContractTerm(profile);

        String resourceId = profile.getResourceId();
        if (profiles.containsKey(resourceId)) {
            throw new IllegalStateException("资源已建档: " + resourceId);
        }
        profile.setGridAccountId(gridAccountId);
        profiles.put(resourceId, profile);
        auditTrail.append(new AuditRecord(resourceId, AuditRecord.Operation.CREATE,
                "*", null, "enroll"));
        return profile;
    }

    /**
     * 档案变更（容量/位置等关键字段），前后值留审计。
     */
    public synchronized void updateCapacity(String resourceId, BigDecimal newCapacityKw) {
        ResourceProfile profile = require(resourceId);
        String before = profile.getCapacity() == null ? null : profile.getCapacity().toPlainString();
        profile.setCapacity(newCapacityKw);
        auditTrail.append(new AuditRecord(resourceId, AuditRecord.Operation.UPDATE,
                "capacity", before, newCapacityKw.toPlainString()));
    }

    /** 注销（代理到期/客户解约），档案转注销态而非删除 */
    public synchronized void retire(String resourceId) {
        require(resourceId);
        auditTrail.append(new AuditRecord(resourceId, AuditRecord.Operation.RETIRE,
                "*", "active", "retired"));
        profiles.remove(resourceId);
    }

    public ResourceProfile require(String resourceId) {
        return Optional.ofNullable(profiles.get(resourceId))
                .orElseThrow(() -> new IllegalArgumentException("未建档资源: " + resourceId));
    }

    /** 台账查询：按类型 + 电气位置过滤（聚合引擎的取数入口） */
    public List<ResourceProfile> query(ResourceType type, String electricalNodePrefix) {
        return profiles.values().stream()
                .filter(p -> type == null || p.getType() == type)
                .filter(p -> electricalNodePrefix == null
                        || (p.getElectricalNode() != null
                        && p.getElectricalNode().startsWith(electricalNodePrefix)))
                .collect(Collectors.toList());
    }

    /** 台账总容量统计（47241 总聚合容量看板的数据源） */
    public BigDecimal totalCapacityKw(ResourceType type) {
        return query(type, null).stream()
                .map(p -> p.getCapacity() == null ? BigDecimal.ZERO : p.getCapacity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertExclusive(String gridAccountId) {
        boolean occupied = profiles.values().stream()
                .anyMatch(p -> gridAccountId.equals(p.getGridAccountId()));
        if (occupied) {
            throw new IllegalStateException(
                    "44260 排他性冲突：户号/并网点已被其他资源占用: " + gridAccountId);
        }
    }

    private void assertContractTerm(ResourceProfile profile) {
        LocalDate end = profile.getContractEnd();
        if (end == null || end.isBefore(LocalDate.now().plusMonths(1))) {
            throw new IllegalArgumentException(
                    "44260 代理期限不满足：合同到期日须不早于 1 个月后, actual=" + end);
        }
    }
}
