package com.openvpp.assessment.ev;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EV 六边形可行域 —— 把「功率约束 + 电量约束 + 出行约束」统一成几何问题（专栏第 35 篇）。
 *
 * 六边形顶点（时间-SOC 平面，A→B→C→F→E→D 顺序）：
 * <ul>
 *   <li>A：接入点 (0, Ss)</li>
 *   <li>B：满功率充电线 × SOC 上限 100% 交点（最快充电轨迹触顶）</li>
 *   <li>C：离网时刻 × SOC 上限</li>
 *   <li>F：离网时刻 × 离网要求 SOC Se</li>
 *   <li>E：强制充电线起点——出行前必须充到 Se，此段无调控弹性</li>
 *   <li>D：满功率放电线 × SOC 下限 20% 交点</li>
 * </ul>
 *
 * 任意时刻调控能力 = 从当前运行点出发的射线在六边形内能延伸多远；
 * 时间越紧、能量越少，能力越小——越接近离网时刻，可行域收缩越厉害。
 *
 * 教学简化：只处理 Ss > Smin（接入即可调控）情形；Ss ≤ Smin 的过放车
 * 须先强制充到 20% 才有调控能力，本实现直接拒绝并提示。
 */
public final class EvFeasibleRegion {

    /**
     * 可行域顶点：时间-SOC 平面上的点。
     */
    public static final class Vertex {
        private final double hoursFromPlugin;
        private final double socPct;

        public Vertex(double hoursFromPlugin, double socPct) {
            this.hoursFromPlugin = hoursFromPlugin;
            this.socPct = socPct;
        }

        public double hoursFromPlugin() { return hoursFromPlugin; }
        public double socPct() { return socPct; }

        @Override
        public String toString() {
            return String.format("(%.3fh, %.1f%%)", hoursFromPlugin, socPct);
        }
    }

    /**
     * 计算某 EV 在当前接入会话内的六边形可行域顶点（A→B→C→F→E→D）。
     *
     * @param session 接入会话参数
     * @return 6 个顶点的不可变列表
     * @throws IllegalArgumentException 过放车（Ss ≤ 20%）或接入时间内充不到 Se 时拒绝计算
     */
    public List<Vertex> computeVertices(EvSession session) {
        double ss = session.arrivalSocPct();
        double se = session.departRequiredSocPct();
        double t = session.durationHours();
        double chargeRate = session.chargeRatePctPerHour();
        double dischargeRate = session.dischargeRatePctPerHour();

        // 过放状态的车是「废电池」：先强制充到 20% 才有调控能力（教学简化：拒绝计算）
        if (ss <= EvSession.SOC_FLOOR_PCT) {
            throw new IllegalArgumentException(
                    "接入 SOC " + ss + "% 不高于下限 20%，过放车须先强制充电，暂无调控能力");
        }
        if (se > EvSession.SOC_CEILING_PCT || se < EvSession.SOC_FLOOR_PCT) {
            throw new IllegalArgumentException("离网要求 SOC 须在 20%~100% 区间: " + se);
        }

        // A：接入点
        Vertex a = new Vertex(0.0, ss);
        // B：满功率充电触顶时刻
        double tB = (EvSession.SOC_CEILING_PCT - ss) / chargeRate;
        if (tB > t) {
            throw new IllegalArgumentException(
                    "接入窗口内无法充满（需 " + tB + "h > " + t + "h），"
                            + "可行域退化为梯形，教学版未覆盖");
        }
        Vertex b = new Vertex(tB, EvSession.SOC_CEILING_PCT);
        // C：离网时刻 × SOC 上限
        Vertex c = new Vertex(t, EvSession.SOC_CEILING_PCT);
        // F：离网时刻 × 离网要求 SOC
        Vertex f = new Vertex(t, se);
        // E：强制充电线起点——从 SOC 下限满功率充到 Se 所需时间，倒推到离网时刻
        double mandatoryChargeHours = (se - EvSession.SOC_FLOOR_PCT) / chargeRate;
        double tE = t - mandatoryChargeHours;
        Vertex e = new Vertex(tE, EvSession.SOC_FLOOR_PCT);
        // D：满功率放电触底时刻
        double tD = (ss - EvSession.SOC_FLOOR_PCT) / dischargeRate;
        Vertex d = new Vertex(tD, EvSession.SOC_FLOOR_PCT);

        // 可行性校验：接入时间内必须能从下限充到 Se（E 不得早于 D，且不早于接入点）
        if (tE < 0 || tE < tD) {
            throw new IllegalArgumentException(
                    "接入时长不足：满功率充电需 " + mandatoryChargeHours + "h 才能从 20% 充到 "
                            + se + "%，超过接入窗口 " + t + "h");
        }
        List<Vertex> vertices = new ArrayList<>();
        Collections.addAll(vertices, a, b, c, f, e, d);
        return Collections.unmodifiableList(vertices);
    }

    /**
     * 任意时刻的可调功率上限：从当前运行点出发、在可行域内延伸到离网的射线长度。
     *
     * P_max(t) = min{ 额定放电功率, 剩余可用储能容量 × 效率 / 剩余时间 }
     *
     * @param session        接入会话
     * @param currentSocPct  当前 SOC（%）
     * @param hoursToDepart  距离网剩余时间（h）
     * @return 可调放电功率上限（kW）
     */
    public double maxAdjustableKw(EvSession session, double currentSocPct, double hoursToDepart) {
        if (hoursToDepart <= 0) {
            return 0.0;
        }
        // 剩余可用能量：当前 SOC 到下限的部分才可用于放电
        double usableKwh = Math.max(0.0, currentSocPct - EvSession.SOC_FLOOR_PCT)
                / 100.0 * session.capacityKwh() * session.efficiency();
        double energyLimitedKw = usableKwh / hoursToDepart;
        return Math.min(session.ratedKw(), energyLimitedKw);
    }
}
