package com.openvpp.assessment;

import com.openvpp.assessment.ev.EvFeasibleRegion;
import com.openvpp.assessment.ev.EvSession;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 第 35 篇 EV 六边形可行域单测：顶点坐标锚定手算算例（A→B→C→F→E→D），
 * 过放拒绝 / 接入时长不足拒绝 / 可调功率取额定与能量约束较小值。
 */
class EvFeasibleRegionTest {

    private final EvFeasibleRegion region = new EvFeasibleRegion();

    // 基准算例：60kWh 电池、60kW 额定、效率 1.0 → 充放电速率 100%/h；
    // 接入 8h、接入 SOC 30%、离网要求 80%
    private final EvSession session = new EvSession(8.0, 30.0, 80.0, 60.0, 60.0, 1.0);

    @Test
    void 六边形顶点坐标与手算一致() {
        List<EvFeasibleRegion.Vertex> v = region.computeVertices(session);
        assertEquals(6, v.size(), "ABCFED 六边形");
        // A 接入点 (0, 30)
        assertVertex(v.get(0), 0.0, 30.0);
        // B 满功率充电触顶：(100-30)/100 = 0.7h
        assertVertex(v.get(1), 0.7, 100.0);
        // C 离网×上限 (8, 100)
        assertVertex(v.get(2), 8.0, 100.0);
        // F 离网×Se (8, 80)
        assertVertex(v.get(3), 8.0, 80.0);
        // E 强制充电线起点：充 20%→80% 需 0.6h，倒推 t=7.4h
        assertVertex(v.get(4), 7.4, 20.0);
        // D 满功率放电触底：(30-20)/100 = 0.1h
        assertVertex(v.get(5), 0.1, 20.0);
    }

    private static void assertVertex(EvFeasibleRegion.Vertex v, double hours, double soc) {
        assertEquals(hours, v.hoursFromPlugin(), 1e-6, "顶点时间坐标: " + v);
        assertEquals(soc, v.socPct(), 1e-6, "顶点 SOC 坐标: " + v);
    }

    @Test
    void 接入SOC不高于下限的过放车拒绝计算() {
        EvSession depleted = new EvSession(8.0, 15.0, 80.0, 60.0, 60.0, 1.0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> region.computeVertices(depleted));
        assertTrue(ex.getMessage().contains("强制充电"), "过放车须先强制充到 20%");
        // 恰好等于下限也拒绝（Ss ≤ Smin 无调控弹性）
        assertThrows(IllegalArgumentException.class,
                () -> region.computeVertices(new EvSession(8.0, 20.0, 80.0, 60.0, 60.0, 1.0)));
    }

    @Test
    void 接入时长不足充到离网要求拒绝计算() {
        // 0.5h 窗口内从 20% 充到 80% 需 0.6h，物理不可行
        EvSession tooShort = new EvSession(0.5, 50.0, 80.0, 60.0, 60.0, 1.0);
        assertThrows(IllegalArgumentException.class, () -> region.computeVertices(tooShort));
    }

    @Test
    void 接入窗口内充不满则退化拒绝() {
        // 从 30% 充满需 0.7h，窗口仅 0.65h → 六边形退化为梯形（教学版未覆盖）
        EvSession cannotFull = new EvSession(0.65, 30.0, 40.0, 60.0, 60.0, 1.0);
        assertThrows(IllegalArgumentException.class, () -> region.computeVertices(cannotFull));
    }

    @Test
    void 可调功率取额定与能量约束较小值() {
        // 能量约束生效：SOC 50% 到下限可用 30%×60×1.0=18kWh，剩 4h → 4.5kW < 60kW 额定
        assertEquals(4.5, region.maxAdjustableKw(session, 50.0, 4.0), 0.01,
                "P_max = 剩余可用能量×效率/剩余时间");
        // 额定约束生效：剩 0.25h，能量口径 42kWh/0.25h=168kW → 封顶 60kW
        assertEquals(60.0, region.maxAdjustableKw(session, 90.0, 0.25), 0.01);
        // 离网时刻可调能力归零：越接近离网可行域收缩越厉害
        assertEquals(0.0, region.maxAdjustableKw(session, 90.0, 0.0));
    }

    @Test
    void 效率打折降低可调功率() {
        EvSession lossy = new EvSession(8.0, 30.0, 80.0, 60.0, 60.0, 0.9);
        // 可用能量 30%×60×0.9=16.2kWh，4h → 4.05kW
        assertEquals(4.05, region.maxAdjustableKw(lossy, 50.0, 4.0), 0.01);
        // 充电速率 90%/h：B 点 (100-30)/90 ≈ 0.7778h
        List<EvFeasibleRegion.Vertex> v = region.computeVertices(lossy);
        assertEquals(70.0 / 90.0, v.get(1).hoursFromPlugin(), 1e-6);
    }

    @Test
    void 会话参数校验() {
        assertThrows(IllegalArgumentException.class,
                () -> new EvSession(0, 30.0, 80.0, 60.0, 60.0, 1.0), "接入时长必须为正");
        assertThrows(IllegalArgumentException.class,
                () -> new EvSession(8.0, 30.0, 80.0, 60.0, 60.0, 1.2), "效率不能超过 1");
        assertThrows(IllegalArgumentException.class,
                () -> new EvSession(8.0, 30.0, 80.0, 0, 60.0, 1.0), "容量必须为正");
    }
}
