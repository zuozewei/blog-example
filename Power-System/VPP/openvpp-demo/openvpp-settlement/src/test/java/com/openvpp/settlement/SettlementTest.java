package com.openvpp.settlement;

import com.openvpp.settlement.assessment.DeviationAssessor;
import com.openvpp.settlement.baseline.BaselineCalculator;
import com.openvpp.settlement.baseline.BaselineRule;
import com.openvpp.settlement.baseline.MeteringResult;
import com.openvpp.settlement.baseline.ResponseMetering;
import com.openvpp.settlement.baseline.SamplePoint;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基线核算与偏差考核全场景单测。
 * 基线：相似日平均 / 响应日及未来日期剔除 / 高异常剔除 / 缺失与零值 / 样本不足拒绝；
 * 响应量：正偏差积分 / 缺失点 / 有效零值 / 跨日切账期 / 响应期实际不反噬基线（反例）；
 * 考核：合格率 / 分档 / 超发封顶。
 */
class SettlementTest {

    private final BaselineCalculator baseline = new BaselineCalculator(BaselineRule.teachingDefault());
    private final ResponseMetering metering = new ResponseMetering();
    private final DeviationAssessor assessor = DeviationAssessor.provincialDefault();

    private static final LocalDate RESPONSE_DAY = LocalDate.of(2026, 9, 16);

    private static List<SamplePoint> pool(double... loads) {
        // 依次落在响应日前的连续工作日：9/9、9/10、9/11、9/14、9/15……
        List<LocalDate> days = List.of(
                LocalDate.of(2026, 9, 9), LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 11), LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 8),
                LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 4));
        List<SamplePoint> samples = new ArrayList<>();
        for (int i = 0; i < loads.length; i++) {
            samples.add(new SamplePoint(days.get(i), loads[i]));
        }
        return samples;
    }

    @Test
    void 相似日平均法核算基线() {
        double value = baseline.calculate(pool(1000.0, 1100.0, 1050.0, 950.0, 1025.0), RESPONSE_DAY);
        assertEquals(1025.0, value, 0.01, "五日平均基线口径错误");
    }

    @Test
    void 响应日当天及未来日期不得入池() {
        // 响应日突击高负荷 5000、管道故障混入的未来数据 8000，都必须剔除
        List<SamplePoint> samples = pool(1000.0, 1000.0, 1000.0);
        samples.add(new SamplePoint(RESPONSE_DAY, 5000.0));
        samples.add(new SamplePoint(RESPONSE_DAY.plusDays(1), 8000.0));
        double value = baseline.calculate(samples, RESPONSE_DAY);
        assertEquals(1000.0, value, 0.01, "响应日及未来日期数据必须被剔除");
    }

    @Test
    void 高异常样本按中位数偏离剔除() {
        // 某样本日 3000 高出中位数 1000 的 1.5 倍上限，剔除后基线 1000
        double value = baseline.calculate(pool(1000.0, 1000.0, 1000.0, 1000.0, 3000.0), RESPONSE_DAY);
        assertEquals(1000.0, value, 0.01, "高异常样本日未被剔除");
    }

    @Test
    void 有效零值是计量数据而非缺失() {
        // 零负荷样本日真实存在（如停产日），应计入池而不是当缺失跳过
        double value = baseline.calculate(pool(0.0, 10.0, 10.0, 10.0, 10.0), RESPONSE_DAY);
        assertEquals(8.0, value, 0.01, "有效零值必须计入基线");
    }

    @Test
    void 缺失点跳过且保留其余样本() {
        List<SamplePoint> samples = pool(1000.0, 1100.0, 900.0);
        samples.add(new SamplePoint(LocalDate.of(2026, 9, 15), null)); // 计量缺失
        double value = baseline.calculate(samples, RESPONSE_DAY);
        assertEquals(1000.0, value, 0.01, "缺失点应跳过而不污染均值");
    }

    @Test
    void 有效样本不足拒绝核算() {
        assertThrows(IllegalStateException.class, () ->
                baseline.calculate(pool(1000.0, 1000.0), RESPONSE_DAY),
                "有效样本不足 3 天必须拒绝核算");
    }

    @Test
    void 响应期实际负荷不反噬基线_反例() {
        // 反例：历史基线 1000kW，响应期实际 500kW。
        // 旧口径"基线 ≤ 实际×1.2"会把基线压到 600，响应量从 500 缩到 100——
        // 响应越成功惩罚越重。修正后：响应期数据不进入基线，响应量全额承认。
        double bl = baseline.calculate(pool(1000.0, 1000.0, 1000.0), RESPONSE_DAY);
        assertEquals(1000.0, bl, 0.01, "基线不得受响应期实际负荷影响");
        double[] actual = {500, 500, 500, 500};
        double responseKwh = metering.meter(new double[]{bl, bl, bl, bl}, actual);
        assertEquals(500.0, responseKwh, 0.01, "响应量 = (1000-500)×0.25×4 = 500 kWh");
    }

    @Test
    void 响应量按正偏差积分() {
        // 4 个 15 分钟点：基线 1000，实际 [400, 400, 1200, 400]，第三点超基线不倒扣
        double[] bl = {1000, 1000, 1000, 1000};
        double[] act = {400, 400, 1200, 400};
        assertEquals(450.0, metering.meter(bl, act), 0.01);
    }

    @Test
    void 缺失点不计入响应量且显式记录() {
        Double[] bl = {1000.0, 1000.0, 1000.0, 1000.0};
        Double[] act = {400.0, null, 400.0, 400.0};
        MeteringResult result = metering.meterWithGapCheck(bl, act);
        assertEquals(450.0, result.responseKwh(), 0.01, "缺失点不计入响应量");
        assertEquals(1, result.missingPoints());
        assertEquals(0.25, result.missingRatio(), 0.001, "缺失率 1/4 须显式可查");
    }

    @Test
    void 实际零负荷是全额响应() {
        Double[] bl = {1000.0, 1000.0};
        Double[] act = {0.0, 0.0};   // 真实停了，不是数据缺失
        MeteringResult result = metering.meterWithGapCheck(bl, act);
        assertEquals(500.0, result.responseKwh(), 0.01, "有效零值按全额响应计入");
        assertEquals(0, result.missingPoints());
    }

    @Test
    void 跨日响应按结算日切分入账() {
        // 跨零点事件：23:45-00:15 共 4 点，按两个结算日各 2 点切分，合计不变
        double[] bl = {1000, 1000, 1000, 1000};
        double[] act = {400, 400, 400, 400};
        double whole = metering.meter(bl, act);
        double day1 = metering.meter(new double[]{1000, 1000}, new double[]{400, 400});
        double day2 = metering.meter(new double[]{1000, 1000}, new double[]{400, 400});
        assertEquals(whole, day1 + day2, 0.01, "跨日切分后总响应量守恒");
    }

    @Test
    void 响应合格率计算() {
        assertEquals(90.0, metering.passRate(450, 500), 0.01);
        assertEquals(0, metering.passRate(450, 0), "申报为零时合格率安全返回");
    }

    @Test
    void 偏差考核分档() {
        // 达标（90%）：不考核
        assertEquals(0, assessor.assess(92.0, 1000, 3.0), 0.01);
        // 一般不足（80%）：欠额 200kWh × 3 元 × 1.0 = 600 元
        assertEquals(600.0, assessor.assess(80.0, 1000, 3.0), 0.01);
        // 严重不足（60%）：欠额 400kWh × 3 元 × 1.5 = 1800 元
        assertEquals(1800.0, assessor.assess(60.0, 1000, 3.0), 0.01);
    }

    @Test
    void 超发按申报封顶() {
        assertEquals(1000.0, assessor.settleKwh(110.0, 1000, 1100), 0.01,
                "超发部分不给钱，按申报封顶");
        assertEquals(800.0, assessor.settleKwh(80.0, 1000, 800), 0.01,
                "不达标按实际结算");
    }
}
