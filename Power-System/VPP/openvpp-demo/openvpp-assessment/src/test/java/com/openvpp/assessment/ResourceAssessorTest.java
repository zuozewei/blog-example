package com.openvpp.assessment;

import com.openvpp.assessment.ac.AcLoadAssessor;
import com.openvpp.assessment.ev.EvChargerAssessor;
import com.openvpp.assessment.storage.StorageAssessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 储能/空调/充电桩三类评估器单测，全部锚定素材物理与行为算例。
 */
class ResourceAssessorTest {

    // 基准算例：2MW/4MWh 磷酸铁锂储能，SOC 10-90%，效率 95%，站用电 4%
    private final StorageAssessor storage = new StorageAssessor(
            2000, 4000, 10.0, 90.0, 95.0, 4.0);

    @Test
    void 储能中位SOC双向潜力对称() {
        StorageAssessor.StorageCapability cap = storage.assess(50.0, 1000);
        // SOC 50%：放/充潜力都应接近净功率 1920kW
        assertTrue(cap.getDischargeKw() > 1800 && cap.getDischargeKw() <= 1920);
        assertEquals(cap.getDischargeKw(), cap.getChargeKw(), 0.01);
        // SOC 50% 的可用能量 = (50-10)% × 4000 × 0.95 = 1520kWh，
        // 满功率 1920kW 可持续 1520/1920 ≈ 2850s（0.79h）。
        // 注意：素材表格的 1.52h 是满量程（90%→10%）口径，从 50% 放起必须按当前 SOC 求解
        assertTrue(cap.getSustainSeconds() > 2700 && cap.getSustainSeconds() < 3000,
                "中位 SOC 持续时长应约 0.79h: " + cap.getSustainSeconds());
    }

    @Test
    void 储能深度放电后潜力枯竭() {
        // 第 04 篇算例：50% 放电 2h 后 SOC 跌至 ~10%，放电潜力从 2000 崩到 110
        double socAfter = storage.socAfterDischarge(50.0, 2000, 2.0);
        assertTrue(socAfter < 15.0, "深度放电后 SOC 应接近下限: " + socAfter);

        StorageAssessor.StorageCapability cap = storage.assess(socAfter, 1000);
        assertTrue(cap.getDischargeKw() < 200, "SOC 枯竭后放电潜力应急剧下跌");
        assertTrue(cap.getChargeKw() > 1800, "SOC 低位时充电潜力应充足");
    }

    @Test
    void 储能循环寿命预算耗尽则禁报() {
        StorageAssessor.StorageCapability cap = storage.assess(50.0, 0);
        assertFalse(cap.isCycleBudgetLeft());
        assertEquals(0, cap.getDischargeKw(), "寿命预算耗尽必须禁报");
    }

    // 基准算例：2000kW 额定制冷，热容 5×10⁶ kJ/°C，温升 1.5°C
    private final AcLoadAssessor ac = new AcLoadAssessor(2000, 5_000_000, 1.5);

    @Test
    void 空调完全关停ETP时长约125分钟() {
        // 素材算例：净得热 1000kW 时 t = 5e6 × 1.5 / 1000 = 7500s ≈ 125min
        AcLoadAssessor.AcCapability cap = ac.assessFullShutdown(1000, 1200);
        assertEquals(7500, cap.getEndureSeconds());
        assertEquals(1200 * 0.9, cap.getShedKw(), 0.01, "承诺削减须打回弹系数");
        assertTrue(cap.getRecoverySeconds() > 0, "恢复期必须进模型");
    }

    @Test
    void 空调预冷模式时长近似翻倍() {
        AcLoadAssessor.AcCapability normal = ac.assessFullShutdown(1000, 1200);
        AcLoadAssessor.AcCapability precool = ac.assessWithPrecool(1000, 1200);
        assertTrue(precool.getEndureSeconds() > normal.getEndureSeconds() * 2,
                "预冷 2°C 应显著拉长响应窗口");
    }

    @Test
    void 空调轮停周期必须大于重启保护() {
        assertTrue(ac.rotationCycleValid(300, 240));
        assertFalse(ac.rotationCycleValid(240, 240), "等于保护延时物理上损伤压缩机");
        assertFalse(ac.rotationCycleValid(120, 240));
    }

    // 基准算例：20 桩 × 120kW 快充站
    private final EvChargerAssessor ev = new EvChargerAssessor(20, 120);

    @Test
    void 充电桩参与率随补贴饱和() {
        assertEquals(0.09, ev.joinRateOf(0), 0.01);
        assertEquals(0.42, ev.joinRateOf(0.5), 0.05);
        assertEquals(0.76, ev.joinRateOf(1.0), 0.05);
        assertEquals(0.89, ev.joinRateOf(2.0), 0.01, "1 元/kWh 后参与率饱和");
    }

    @Test
    void 充电桩可承诺容量是物理乘参与率() {
        EvChargerAssessor.EvCapability cap = ev.assess(12, 1.0, 40,
                EvChargerAssessor.VehicleMix.PRIVATE_DOMINANT);
        // 物理层 12 车 × 40kW = 480kW；参与率 ~76%；折扣 0.9
        assertTrue(cap.getCommittedKw() > 300 && cap.getCommittedKw() < 400,
                "可承诺容量口径错误: " + cap.getCommittedKw());
        assertEquals(20 * 60, cap.getTolerableSeconds(), "私家车容忍 20 分钟");
    }

    @Test
    void 充电桩中等繁忙站最优() {
        double busy = ev.utilizationScore(0.95);
        double medium = ev.utilizationScore(0.60);
        double idle = ev.utilizationScore(0.30);
        assertTrue(medium > busy && medium > idle,
                "中等繁忙站的可调潜力应高于满负荷站与闲置站");
    }
}
