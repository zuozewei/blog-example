package com.openvpp.settlement;

import com.openvpp.settlement.allocation.ProfitAllocator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 收益分摊单测：保底按容量 / 分成按贡献 / 平台抽成 / 总和守恒。
 */
class ProfitAllocatorTest {

    private final ProfitAllocator allocator = new ProfitAllocator(new BigDecimal("0.10"));

    @Test
    void 保底分成两层分配且总和守恒() {
        // 平台实收 1380 元，保底 600 元，超额 780 元可分配（抽成 10% 后 702）
        Map<String, BigDecimal> declared = new LinkedHashMap<>();
        declared.put("user-A", new BigDecimal("300"));
        declared.put("user-B", new BigDecimal("200"));

        Map<String, BigDecimal> actual = new LinkedHashMap<>();
        actual.put("user-A", new BigDecimal("276"));   // 贡献 60%
        actual.put("user-B", new BigDecimal("184"));   // 贡献 40%

        Map<String, BigDecimal> result = allocator.allocate(
                new BigDecimal("1380"), new BigDecimal("600"), declared, actual);

        // 保底：A 360 / B 240
        // 分成 702：A 421.20 / B 280.80
        BigDecimal sum = result.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertTrue(sum.compareTo(new BigDecimal("1302")) == 0
                        || sum.compareTo(new BigDecimal("1301.99")) == 0
                        || sum.compareTo(new BigDecimal("1302.00")) == 0,
                "分配总和须守恒（保底600+可分成702）: " + sum);
        assertTrue(result.get("user-A").compareTo(result.get("user-B")) > 0,
                "贡献大者应分更多");
    }

    @Test
    void 平台抽成从分成部分计提() {
        ProfitAllocator noCut = new ProfitAllocator(BigDecimal.ZERO);
        Map<String, BigDecimal> declared = Map.of("u1", new BigDecimal("100"));
        Map<String, BigDecimal> actual = Map.of("u1", new BigDecimal("100"));

        Map<String, BigDecimal> withCut = allocator.allocate(
                new BigDecimal("1000"), new BigDecimal("400"), declared, actual);
        Map<String, BigDecimal> without = noCut.allocate(
                new BigDecimal("1000"), new BigDecimal("400"), declared, actual);

        assertTrue(withCut.get("u1").compareTo(without.get("u1")) < 0,
                "平台抽成后用户所得应减少");
        // 抽成 10%：差额 = 600 超额 × 10% = 60
        assertEquals(0, without.get("u1").subtract(withCut.get("u1"))
                .compareTo(new BigDecimal("60.00")), "抽成金额口径错误");
    }

    @Test
    void 贡献为零的用户只拿保底() {
        Map<String, BigDecimal> declared = new LinkedHashMap<>();
        declared.put("active", new BigDecimal("100"));
        declared.put("dormant", new BigDecimal("100"));
        Map<String, BigDecimal> actual = new LinkedHashMap<>();
        actual.put("active", new BigDecimal("100"));
        actual.put("dormant", BigDecimal.ZERO);

        Map<String, BigDecimal> result = allocator.allocate(
                new BigDecimal("800"), new BigDecimal("400"), declared, actual);

        assertEquals(0, result.get("dormant").compareTo(new BigDecimal("200.00")),
                "零贡献用户只应得保底份额");
    }
}
