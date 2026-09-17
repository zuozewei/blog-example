package com.openvpp.aggregator.engine;

import com.openvpp.aggregator.unit.AdmissionThreshold;
import com.openvpp.aggregator.unit.VppUnit;
import com.openvpp.common.enums.Scenario;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单元分组器 —— 按 47241 第 11.3 条同节点约束把资源聚成 VPP 单元。
 * 三条铁律：
 * 1. 同一单元内资源必须同属一个出清节点（否则结算无法统一）；
 * 2. 代理期内才准入（44260 期限校验的调度侧复查）;
 * 3. 单元调节容量宜 ≥1MW 准入门槛（AdmissionThreshold）。
 */
public class UnitGrouper {

    /**
     * 按出清节点分组，每组生成一个 VppUnit；不达准入门槛的单元被标记但保留（供诊断）。
     */
    public List<VppUnit> group(List<AssessedResource> pool, Scenario scenario) {
        return pool.stream()
                .filter(AssessedResource::isContractValid)
                .collect(Collectors.groupingBy(AssessedResource::getClearingNodeId))
                .entrySet().stream()
                .map(e -> buildUnit(e.getKey(), e.getValue(), scenario))
                .collect(Collectors.toList());
    }

    private VppUnit buildUnit(String nodeId, List<AssessedResource> members, Scenario scenario) {
        VppUnit unit = new VppUnit();
        unit.setUnitId("unit-" + nodeId.replaceAll("[^A-Za-z0-9]", "-"));
        unit.setClearingNodeId(nodeId);
        unit.setMarketScenario(scenario);
        unit.setResourceIds(members.stream()
                .map(AssessedResource::getResourceId)
                .collect(Collectors.toList()));

        BigDecimal unitAdjustKw = members.stream()
                .map(AssessedResource::getAdjustCapacityKw)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 准入校验挂在单元上，申报前统一检查
        if (!unit.passAdmission(unitAdjustKw)) {
            unit.setUnitId(unit.getUnitId() + "-below-threshold");
        }
        return unit;
    }
}
